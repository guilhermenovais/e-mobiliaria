package com.guilherme.emobiliaria.backup.infrastructure.service;

import com.guilherme.emobiliaria.backup.domain.service.BackupCreationService;
import com.guilherme.emobiliaria.shared.exception.BackupException;
import com.guilherme.emobiliaria.shared.exception.InsufficientSpaceException;
import com.guilherme.emobiliaria.shared.persistence.AppDataPaths;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.Statement;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

public class ZipBackupCreationService implements BackupCreationService {

  private static final Logger log = LoggerFactory.getLogger(ZipBackupCreationService.class);
  private static final String BACKUP_DIR_NAME = "e-mobiliaria-backup";
  private static final DateTimeFormatter TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");

  private final DataSource dataSource;

  @Inject
  public ZipBackupCreationService(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public void createBackup(Path targetDrivePath)
      throws InsufficientSpaceException, BackupException {
    Path dbDir = AppDataPaths.databaseDirectory();
    Path proofsDir = AppDataPaths.proofStorageDirectory();
    Path dbFile = dbDir.resolve("emobiliaria.mv.db");

    long requiredSpace = estimateRequiredSpace(dbFile, proofsDir);
    checkDiskSpace(targetDrivePath, requiredSpace);

    Path backupDir = targetDrivePath.resolve(BACKUP_DIR_NAME);
    try {
      Files.createDirectories(backupDir);
    } catch (IOException e) {
      throw new BackupException("Failed to create backup directory", e);
    }

    String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
    Path finalZip = backupDir.resolve("backup-" + timestamp + ".zip");
    Path tempH2Zip = null;

    try {
      tempH2Zip = Files.createTempFile("emobiliaria-h2-backup-", ".zip");
      runH2Backup(tempH2Zip);

      Path tempDbFile = extractDbFromH2Zip(tempH2Zip);
      try {
        writeFinalZip(finalZip, tempDbFile, proofsDir);
      } finally {
        Files.deleteIfExists(tempDbFile);
      }

      verifyZip(finalZip);
      log.info("Backup created successfully at {}", finalZip);

    } catch (InsufficientSpaceException | BackupException e) {
      throw e;
    } catch (Exception e) {
      try {
        Files.deleteIfExists(finalZip);
      } catch (IOException ignored) {
      }
      throw new BackupException("Backup failed", e);
    } finally {
      if (tempH2Zip != null) {
        try {
          Files.deleteIfExists(tempH2Zip);
        } catch (IOException ignored) {
        }
      }
    }
  }

  private long estimateRequiredSpace(Path dbFile, Path proofsDir) throws BackupException {
    try {
      long dbSize = Files.exists(dbFile) ? Files.size(dbFile) : 0L;
      long proofsSize = 0L;
      if (Files.isDirectory(proofsDir)) {
        try (var stream = Files.list(proofsDir)) {
          proofsSize = stream.mapToLong(p -> {
            try {
              return Files.size(p);
            } catch (IOException e) {
              return 0L;
            }
          }).sum();
        }
      }
      return dbSize + proofsSize;
    } catch (IOException e) {
      throw new BackupException("Failed to estimate required space", e);
    }
  }

  private void checkDiskSpace(Path drivePath, long requiredSpace)
      throws InsufficientSpaceException, BackupException {
    try {
      long usableSpace = Files.getFileStore(drivePath).getUsableSpace();
      if (usableSpace < requiredSpace) {
        throw new InsufficientSpaceException();
      }
    } catch (InsufficientSpaceException e) {
      throw e;
    } catch (IOException e) {
      throw new BackupException("Failed to check disk space", e);
    }
  }

  private void runH2Backup(Path targetZip) throws BackupException {
    String zipPath = targetZip.toAbsolutePath().toString().replace('\\', '/');
    try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
      stmt.execute("BACKUP TO '" + zipPath + "'");
    } catch (Exception e) {
      throw new BackupException("H2 BACKUP TO command failed", e);
    }
  }

  private Path extractDbFromH2Zip(Path h2Zip) throws BackupException {
    try (ZipFile zipFile = new ZipFile(h2Zip.toFile())) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();
        if (name.endsWith(".mv.db")) {
          Path tempFile = Files.createTempFile("emobiliaria-db-", ".mv.db");
          try (InputStream is = zipFile.getInputStream(entry)) {
            Files.copy(is, tempFile, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
          }
          return tempFile;
        }
      }
      throw new BackupException("emobiliaria.mv.db not found in H2 backup archive");
    } catch (BackupException e) {
      throw e;
    } catch (IOException e) {
      throw new BackupException("Failed to extract database from H2 backup", e);
    }
  }

  private void writeFinalZip(Path finalZip, Path dbFile, Path proofsDir) throws BackupException {
    try (ZipOutputStream zos = new ZipOutputStream(Files.newOutputStream(finalZip))) {
      addZipEntry(zos, dbFile, "database/emobiliaria.mv.db");

      if (Files.isDirectory(proofsDir)) {
        try (var stream = Files.list(proofsDir)) {
          for (Path proof : stream.toList()) {
            if (Files.isRegularFile(proof)) {
              addZipEntry(zos, proof, "proofs/" + proof.getFileName());
            }
          }
        }
      }
    } catch (IOException e) {
      throw new BackupException("Failed to write backup ZIP", e);
    }
  }

  private void addZipEntry(ZipOutputStream zos, Path file, String entryName) throws IOException {
    zos.putNextEntry(new ZipEntry(entryName));
    Files.copy(file, zos);
    zos.closeEntry();
  }

  private void verifyZip(Path zipPath) throws BackupException {
    try (ZipFile zipFile = new ZipFile(zipPath.toFile())) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        try (InputStream is = zipFile.getInputStream(entry)) {
          is.transferTo(OutputStream.nullOutputStream());
        }
      }
    } catch (IOException e) {
      throw new BackupException("Backup ZIP integrity check failed", e);
    }
  }
}
