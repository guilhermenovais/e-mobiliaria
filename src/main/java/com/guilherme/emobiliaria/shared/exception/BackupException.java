package com.guilherme.emobiliaria.shared.exception;

public class BackupException extends BusinessException {

  public BackupException(String message, Throwable cause) {
    super(ErrorMessage.Backup.BACKUP_FAILED);
    initCause(cause);
  }

  public BackupException(String message) {
    super(ErrorMessage.Backup.BACKUP_FAILED);
  }
}
