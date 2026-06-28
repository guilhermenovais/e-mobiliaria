package com.guilherme.emobiliaria.shared.exception;

public class RestoreException extends BusinessException {

  public RestoreException(String message, Throwable cause) {
    super(ErrorMessage.Backup.RESTORE_FAILED);
    initCause(cause);
  }

  public RestoreException(String message) {
    super(ErrorMessage.Backup.RESTORE_FAILED);
  }
}
