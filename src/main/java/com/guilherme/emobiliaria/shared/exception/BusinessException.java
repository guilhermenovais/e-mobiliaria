package com.guilherme.emobiliaria.shared.exception;

public class BusinessException extends RuntimeException {
  private final ErrorMessage errorMessage;

  public BusinessException(ErrorMessage errorMessage) {
    super(errorMessage.getLogMessage());
    this.errorMessage = errorMessage;
  }

  public ErrorMessage getErrorMessage() {
    return errorMessage;
  }
}
