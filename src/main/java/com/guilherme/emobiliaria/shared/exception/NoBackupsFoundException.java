package com.guilherme.emobiliaria.shared.exception;

public class NoBackupsFoundException extends BusinessException {

  public NoBackupsFoundException() {
    super(ErrorMessage.Backup.NO_BACKUPS_FOUND);
  }
}
