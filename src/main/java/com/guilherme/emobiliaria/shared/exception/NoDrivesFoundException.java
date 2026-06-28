package com.guilherme.emobiliaria.shared.exception;

public class NoDrivesFoundException extends BusinessException {

  public NoDrivesFoundException() {
    super(ErrorMessage.Backup.NO_DRIVES_FOUND);
  }
}
