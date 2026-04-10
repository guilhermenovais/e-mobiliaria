package com.guilherme.emobiliaria.shared.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Base64;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateService {

  private static final Logger log = LoggerFactory.getLogger(UpdateService.class);
  private static final String REPO = "guilhermenovais/e-mobiliaria";
  private static final String CURRENT_VERSION = readCurrentVersion();

  private final UpdateProgressWindow progressWindow;

  public UpdateService(UpdateProgressWindow progressWindow) {
    this.progressWindow = progressWindow;
  }

  private final HttpClient httpClient =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(10))
          .followRedirects(HttpClient.Redirect.ALWAYS)
          .build();

  private static String readCurrentVersion() {
    try (InputStream is = UpdateService.class.getResourceAsStream("/version.txt")) {
      if (is == null)
        return "0.0.0";
      return new String(is.readAllBytes()).trim();
    } catch (IOException e) {
      return "0.0.0";
    }
  }

  public void checkAndUpdate() {
    if (isLinux()) {
      log.info("Skipping auto-update on Linux");
      return;
    }

    try {
      Release latest = fetchLatest();
      if (!isNewer(CURRENT_VERSION, latest.version())) {
        log.info("App is up to date ({})", CURRENT_VERSION);
        return;
      }

      log.info("Update available: {} -> {}", CURRENT_VERSION, latest.version());
      boolean confirmed = progressWindow.confirmUpdate(CURRENT_VERSION, latest.version());
      if (!confirmed) {
        log.info("User declined the update");
        return;
      }

      progressWindow.showProgress();
      progressWindow.setStatus("update.status.downloading");
      log.info("Downloading update...");
      Path zip = download(latest.downloadUrl());

      progressWindow.setStatus("update.status.extracting");
      Path newDir = Files.createTempDirectory("emobiliaria-update");
      unzip(zip, newDir);
      log.info("Unzip complete: {}", newDir);

      progressWindow.setStatus("update.status.applying");
      applyUpdate(newDir);

    } catch (Exception e) {
      log.warn("Auto-update failed: {}", e.getMessage());
      progressWindow.showError(e.getMessage());
    }
  }

  private Release fetchLatest() throws Exception {
    String url = "https://api.github.com/repos/" + REPO + "/releases/latest";

    HttpRequest req =
        HttpRequest.newBuilder(URI.create(url)).header("Accept", "application/vnd.github+json")
            .timeout(Duration.ofSeconds(10)).build();

    HttpResponse<String> res = httpClient.send(req, HttpResponse.BodyHandlers.ofString());
    String body = res.body();

    String tagName = extractJsonValue(body, "tag_name");
    if (tagName == null)
      throw new IllegalStateException("tag_name not found in response");
    String version = tagName.startsWith("v") ? tagName.substring(1) : tagName;

    String downloadUrl = findZipAssetUrl(body);
    return new Release(version, downloadUrl);
  }

  private String extractJsonValue(String json, String key) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*?)\"");
    Matcher matcher = pattern.matcher(json);
    return matcher.find() ? matcher.group(1) : null;
  }

  private String findZipAssetUrl(String body) {
    Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*\\.zip)\"");
    Matcher nameMatcher = namePattern.matcher(body);
    if (!nameMatcher.find())
      throw new IllegalStateException("No zip asset found in release");

    String remaining = body.substring(nameMatcher.end());
    Pattern urlPattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"");
    Matcher urlMatcher = urlPattern.matcher(remaining);
    if (!urlMatcher.find())
      throw new IllegalStateException("No browser_download_url found for zip asset");

    return urlMatcher.group(1);
  }

  private boolean isNewer(String current, String latest) {
    return !current.equals(latest);
  }

  private Path download(String url) throws Exception {
    Path target = Files.createTempFile("emobiliaria-update", ".zip");

    HttpResponse<Path> response = httpClient.send(
        HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(5)).build(),
        HttpResponse.BodyHandlers.ofFile(target));

    int status = response.statusCode();
    if (status < 200 || status >= 300) {
      throw new IllegalStateException("Download failed with HTTP " + status);
    }

    log.info("Download complete: {} bytes at {}", Files.size(target), target);
    return target;
  }

  private void unzip(Path zip, Path targetDir) throws IOException {
    try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zip))) {
      ZipEntry entry;
      while ((entry = zis.getNextEntry()) != null) {
        String name = entry.getName();
        if (name.contains(".."))
          continue;

        Path out = targetDir.resolve(name).normalize();
        if (!out.startsWith(targetDir))
          continue;

        if (entry.isDirectory()) {
          Files.createDirectories(out);
        } else {
          Files.createDirectories(out.getParent());
          Files.copy(zis, out, StandardCopyOption.REPLACE_EXISTING);
        }
      }
    }
  }

  private void applyUpdate(Path newDir) throws Exception {
    Path currentAppDir = findCurrentAppDir();

    if (currentAppDir != null) {
      applyPersistentUpdate(newDir.resolve("app"), currentAppDir);
    } else {
      Path launcher = findLauncher(newDir);
      log.info("Launcher found at: {}", launcher);
      launcher.toFile().setExecutable(true);
      new ProcessBuilder(launcher.toString())
          .redirectOutput(ProcessBuilder.Redirect.DISCARD)
          .redirectErrorStream(true)
          .start();
    }

    log.info("Update process started");
    progressWindow.setStatus("update.status.done");
    progressWindow.markComplete();
  }

  private void applyPersistentUpdate(Path newAppDir, Path currentAppDir) throws Exception {
    String newPath = newAppDir.toAbsolutePath().toString();
    String curPath = currentAppDir.toAbsolutePath().toString();
    String launcher = currentAppDir.resolve("bin").resolve("app.bat").toAbsolutePath().toString();

    // Execution policy applies to script files, not inline commands.
    // Encoding as UTF-16LE Base64 and passing via -EncodedCommand bypasses it entirely.
    // Remove destination first to avoid permission issues, then copy new files
    String script =
        "Start-Sleep -Seconds 10; " +
        "try { " +
        "  $dest = '" + curPath + "'; " +
        "  if (Test-Path $dest) { Remove-Item -Path $dest -Recurse -Force -ErrorAction Stop } " +
        "  Copy-Item -Path '" + newPath + "' -Destination $dest -Recurse -Force -ErrorAction Stop; " +
        "  Start-Sleep -Seconds 2; " +
        "  Start-Process -FilePath '" + launcher + "' -WindowStyle Hidden -ErrorAction Stop; " +
        "} catch { " +
        "  [Console]::Error.WriteLine('Update failed: ' + $_.Exception.Message); " +
        "  exit 1; " +
        "}";

    String encodedCommand = Base64.getEncoder()
        .encodeToString(script.getBytes(StandardCharsets.UTF_16LE));

    log.info("Starting persistent update via PowerShell: " + encodedCommand);
    new ProcessBuilder(
        "powershell.exe", "-NoProfile", "-WindowStyle", "Hidden",
        "-NonInteractive", "-EncodedCommand", encodedCommand
    ).start();
  }

  private Path findCurrentAppDir() {
    try {
      URI location = UpdateService.class.getProtectionDomain().getCodeSource().getLocation().toURI();
      // JAR is at <appDir>/lib/e-mobiliaria-*.jar
      return Path.of(location).getParent().getParent();
    } catch (Exception e) {
      log.warn("Could not determine current app directory: {}", e.getMessage());
      return null;
    }
  }

  private Path findLauncher(Path dir) throws IOException {
    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    try (var stream = Files.walk(dir, 3)) {
      return stream.filter(p -> {
            String name = p.getFileName().toString();
            String parentName = p.getParent() != null ? p.getParent().getFileName().toString() : "";
            if (!"bin".equals(parentName))
              return false;
            return isWindows ? name.equals("app.bat") : name.equals("app");
          }).findFirst()
          .orElseThrow(() -> new IllegalStateException("No launcher found in update package"));
    }
  }

  private boolean isLinux() {
    return System.getProperty("os.name").toLowerCase().contains("linux");
  }


  private record Release(String version, String downloadUrl) {
  }
}
