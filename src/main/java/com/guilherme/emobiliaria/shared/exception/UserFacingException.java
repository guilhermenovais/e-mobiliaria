package com.guilherme.emobiliaria.shared.exception;

/**
 * Exception intended to surface a specific localized message to the user.
 */
public class UserFacingException extends RuntimeException {
  private final String translationKey;

  public UserFacingException(String translationKey, String logMessage) {
    super(logMessage);
    if (translationKey == null || translationKey.isBlank()) {
      throw new IllegalArgumentException("translationKey must not be null or blank");
    }
    this.translationKey = translationKey;
  }

  public String getTranslationKey() {
    return translationKey;
  }
}
