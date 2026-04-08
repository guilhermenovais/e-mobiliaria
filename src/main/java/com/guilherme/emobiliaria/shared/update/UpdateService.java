package com.guilherme.emobiliaria.shared.update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class UpdateService {

  private static final Logger log = LoggerFactory.getLogger(UpdateService.class);
  private static final String REPO = "guilhermenovais/e-mobiliaria";
  private static final String CURRENT_VERSION = readCurrentVersion();

  private final HttpClient httpClient =
      HttpClient.newBuilder()
          .connectTimeout(Duration.ofSeconds(10))
          .followRedirects(HttpClient.Redirect.NORMAL)
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

      log.info("Update available: {} -> {}, downloading...", CURRENT_VERSION, latest.version());
      Path zip = download(latest.downloadUrl());
      Path newDir = Files.createTempDirectory("emobiliaria-update");
      unzip(zip, newDir);
      applyUpdate(newDir);

    } catch (Exception e) {
      log.warn("Auto-update check failed: {}", e.getMessage());
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

    httpClient.send(HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofMinutes(5)).build(),
        HttpResponse.BodyHandlers.ofFile(target));

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
    Path launcher = findLauncher(newDir);
    boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");

    ProcessBuilder pb;
    if (isWindows) {
      pb = new ProcessBuilder("cmd.exe", "/c", launcher.toString());
    } else {
      launcher.toFile().setExecutable(true);
      pb = new ProcessBuilder(launcher.toString());
    }

    pb.inheritIO().start();
    System.exit(0);
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
