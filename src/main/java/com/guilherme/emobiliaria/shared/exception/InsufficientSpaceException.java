package com.guilherme.emobiliaria.shared.exception;

public class InsufficientSpaceException extends BusinessException {

  public InsufficientSpaceException() {
    super(ErrorMessage.Backup.INSUFFICIENT_SPACE);
  }
}
