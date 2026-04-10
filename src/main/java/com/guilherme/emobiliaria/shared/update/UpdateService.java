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
      log.info("Downloading update installer...");
      Path installer = download(latest.downloadUrl());

      progressWindow.setStatus("update.status.applying");
      applyUpdate(installer);

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

    String downloadUrl = findInstallerAssetUrl(body);
    return new Release(version, downloadUrl);
  }

  private String extractJsonValue(String json, String key) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*?)\"");
    Matcher matcher = pattern.matcher(json);
    return matcher.find() ? matcher.group(1) : null;
  }

  private String findInstallerAssetUrl(String body) {
    Pattern namePattern = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]*\\.exe)\"");
    Matcher nameMatcher = namePattern.matcher(body);
    if (!nameMatcher.find())
      throw new IllegalStateException("No exe asset found in release");

    String remaining = body.substring(nameMatcher.end());
    Pattern urlPattern = Pattern.compile("\"browser_download_url\"\\s*:\\s*\"([^\"]+)\"");
    Matcher urlMatcher = urlPattern.matcher(remaining);
    if (!urlMatcher.find())
      throw new IllegalStateException("No browser_download_url found for exe asset");

    return urlMatcher.group(1);
  }

  private boolean isNewer(String current, String latest) {
    return !current.equals(latest);
  }

  private Path download(String url) throws Exception {
    Path target = Files.createTempFile("emobiliaria-update", ".exe");

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

  private void applyUpdate(Path installer) throws Exception {
    String installerPath = installer.toAbsolutePath().toString();
    
    // PowerShell script to execute installer with UAC elevation
    String script =
        "Start-Process -FilePath '" + installerPath + "' -Verb RunAs; " +
        "Start-Sleep -Seconds 2; " +
        "exit 0";

    String encodedCommand = Base64.getEncoder()
        .encodeToString(script.getBytes(StandardCharsets.UTF_16LE));

    log.info("Starting installer with UAC elevation via PowerShell");
    new ProcessBuilder(
        "powershell.exe", "-NoProfile", "-WindowStyle", "Hidden",
        "-NonInteractive", "-EncodedCommand", encodedCommand
    ).start();
    
    log.info("Installer process started, exiting application");
    progressWindow.setStatus("update.status.done");
    progressWindow.markComplete();
    
    System.exit(0);
  }

  private boolean isLinux() {
    return System.getProperty("os.name").toLowerCase().contains("linux");
  }


  private record Release(String version, String downloadUrl) {
  }
}
