package com.guilherme.emobiliaria.backup.infrastructure.service;

import com.guilherme.emobiliaria.backup.domain.service.RestoreService;
import com.guilherme.emobiliaria.shared.exception.RestoreException;
import com.guilherme.emobiliaria.shared.persistence.AppDataPaths;
import jakarta.inject.Inject;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class ProcessRestoreService implements RestoreService {

  private static final Logger log = LoggerFactory.getLogger(ProcessRestoreService.class);

  @Inject
  public ProcessRestoreService() {
  }

  @Override
  public void restore(Path backupFilePath) throws RestoreException {
    Path tempDir;
    try {
      tempDir = Files.createTempDirectory("emobiliaria-restore-");
      extractBackup(backupFilePath, tempDir);
    } catch (IOException e) {
      throw new RestoreException("Failed to extract backup archive", e);
    }

    long pid = ProcessHandle.current().pid();
    String exePath = ProcessHandle.current().info().command()
        .orElseThrow(() -> new RestoreException("Cannot determine current executable path"));

    Path dbDir = AppDataPaths.databaseDirectory();
    Path proofsDir = AppDataPaths.proofStorageDirectory();

    String script = buildRestoreScript(pid, tempDir, dbDir, proofsDir, exePath);
    String encodedScript =
        Base64.getEncoder().encodeToString(script.getBytes(StandardCharsets.UTF_16LE));

    try {
      new ProcessBuilder("powershell.exe", "-NoProfile", "-NonInteractive", "-EncodedCommand",
          encodedScript).start();
    } catch (IOException e) {
      throw new RestoreException("Failed to launch restore script", e);
    }

    log.info("Restore script launched, exiting application (PID {})", pid);
    Platform.runLater(Platform::exit);
  }

  private void extractBackup(Path zipPath, Path targetDir) throws IOException {
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        Path dest = targetDir.resolve(entry.getName()).normalize();
        if (!dest.startsWith(targetDir)) {
          throw new IOException("Zip entry outside target directory: " + entry.getName());
        }
        if (entry.isDirectory()) {
          Files.createDirectories(dest);
        } else {
          Files.createDirectories(dest.getParent());
          try (InputStream is = zipFile.getInputStream(entry)) {
            Files.copy(is, dest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
          }
        }
      }
    }
  }

  private String buildRestoreScript(long pid, Path tempDir, Path dbDir, Path proofsDir,
      String exePath) {
    String tempDirStr = tempDir.toAbsolutePath().toString();
    String dbDirStr = dbDir.toAbsolutePath().toString();
    String proofsDirStr = proofsDir.toAbsolutePath().toString();

    return "$appPid = " + pid + "\r\n" + "$tempDir = '" + escape(
        tempDirStr) + "'\r\n" + "$dbDir = '" + escape(
        dbDirStr) + "'\r\n" + "$proofsDir = '" + escape(
        proofsDirStr) + "'\r\n" + "$exePath = '" + escape(
        exePath) + "'\r\n" + "\r\n" + "do { Start-Sleep -Milliseconds 500 } while (Get-Process -Id $appPid -ErrorAction SilentlyContinue)\r\n" + "\r\n" + "Remove-Item -Path \"$dbDir\\*\" -Recurse -Force -ErrorAction SilentlyContinue\r\n" + "Copy-Item -Path \"$tempDir\\database\\emobiliaria.mv.db\" -Destination $dbDir -Force\r\n" + "\r\n" + "Remove-Item -Path \"$proofsDir\\*\" -Recurse -Force -ErrorAction SilentlyContinue\r\n" + "$proofsSource = Join-Path $tempDir 'proofs'\r\n" + "if (Test-Path $proofsSource) {\r\n" + "    Copy-Item -Path \"$proofsSource\\*\" -Destination $proofsDir -Recurse -Force -ErrorAction SilentlyContinue\r\n" + "}\r\n" + "\r\n" + "Start-Process -FilePath $exePath\r\n";
  }

  private String escape(String s) {
    return s.replace("'", "''");
  }
}
