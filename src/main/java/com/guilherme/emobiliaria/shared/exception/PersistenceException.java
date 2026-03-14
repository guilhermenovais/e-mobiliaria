package com.guilherme.emobiliaria.shared.exception;

/**
 * Custom exception class for handling persistence-related errors in the application. This exception
 * wraps an error message and the original exception that caused the error.
 */
public class PersistenceException extends RuntimeException {
  private final ErrorMessage errorMessage;
  private final Throwable originalException;

  /**
   * Constructs a new PersistenceException with the specified error message and original exception.
   *
   * @param errorMessage      The error message associated with this exception
   * @param originalException The original exception that caused this persistence exception
   */
  public PersistenceException(ErrorMessage errorMessage, Throwable originalException) {
    super(errorMessage.getLogMessage());
    this.errorMessage = errorMessage;
    this.originalException = originalException;
  }

  /**
   * Returns the internationalization code associated with this exception.
   *
   * @return The internationalization code for retrieving localized error messages
   */
  public ErrorMessage getErrorMessage() {
    return errorMessage;
  }

  /**
   * Returns the original exception that caused this persistence exception.
   *
   * @return The original exception
   */
  public Throwable getOriginalException() {
    return originalException;
  }
}
