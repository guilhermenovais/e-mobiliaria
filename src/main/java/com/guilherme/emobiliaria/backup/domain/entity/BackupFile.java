package com.guilherme.emobiliaria.backup.domain.entity;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record BackupFile(String filename, Path path, LocalDateTime timestamp)
    implements Comparable<BackupFile> {

  private static final DateTimeFormatter DISPLAY_FORMAT =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

  @Override
  public int compareTo(BackupFile other) {
    return other.timestamp.compareTo(this.timestamp);
  }

  @Override
  public String toString() {
    return timestamp.format(DISPLAY_FORMAT);
  }
}
