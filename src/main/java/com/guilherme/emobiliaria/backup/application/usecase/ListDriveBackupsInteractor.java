package com.guilherme.emobiliaria.backup.application.usecase;

import com.guilherme.emobiliaria.backup.application.input.ListDriveBackupsInput;
import com.guilherme.emobiliaria.backup.application.output.ListDriveBackupsOutput;
import com.guilherme.emobiliaria.backup.domain.entity.BackupFile;
import com.guilherme.emobiliaria.shared.exception.NoBackupsFoundException;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

public class ListDriveBackupsInteractor {

  private static final String BACKUP_DIR = "e-mobiliaria-backup";
  private static final DateTimeFormatter FILENAME_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd-HHmm");

  @Inject
  public ListDriveBackupsInteractor() {
  }

  public ListDriveBackupsOutput execute(ListDriveBackupsInput input) {
    Path backupDir = input.drivePath().resolve(BACKUP_DIR);
    if (!Files.isDirectory(backupDir)) {
      throw new NoBackupsFoundException();
    }

    List<BackupFile> backups;
    try (Stream<Path> files = Files.list(backupDir)) {
      backups =
          files.filter(p -> p.getFileName().toString().endsWith(".zip")).map(this::parseBackupFile)
              .filter(bf -> bf != null).sorted().toList();
    } catch (IOException e) {
      throw new NoBackupsFoundException();
    }

    if (backups.isEmpty()) {
      throw new NoBackupsFoundException();
    }
    return new ListDriveBackupsOutput(Collections.unmodifiableList(backups));
  }

  private BackupFile parseBackupFile(Path path) {
    String filename = path.getFileName().toString();
    if (!filename.startsWith("backup-") || !filename.endsWith(".zip")) {
      return null;
    }
    String datePart = filename.substring("backup-".length(), filename.length() - ".zip".length());
    try {
      LocalDateTime timestamp = LocalDateTime.parse(datePart, FILENAME_FORMAT);
      return new BackupFile(filename, path, timestamp);
    } catch (DateTimeParseException e) {
      return null;
    }
  }
}
