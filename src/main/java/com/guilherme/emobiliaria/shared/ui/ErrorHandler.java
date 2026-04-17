package com.guilherme.emobiliaria.shared.ui;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.guilherme.emobiliaria.shared.exception.UserFacingException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.MissingResourceException;
import java.util.ResourceBundle;

public final class ErrorHandler {
  private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);
  private static final String GENERIC_ERROR_KEY = "error.generic";
  private static final String FALLBACK_ERROR_MESSAGE = "An unexpected error occurred.";

  private ErrorHandler() {
  }

  /**
   * Logs the error and shows a localized Alert dialog on the FX thread. Supports domain,
   * persistence, and user-facing exceptions via translation keys; all other errors fall back to
   * "error.generic".
   */
  public static void handle(Throwable t, ResourceBundle bundle) {
    log.error(t != null ? t.getMessage() : "Unexpected null throwable", t);
    Platform.runLater(() -> {
      String message = resolveMessage(t, bundle);

      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Erro");
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
    });
  }

  static String resolveMessage(Throwable t, ResourceBundle bundle) {
    if (t == null) {
      return resolveGenericMessage(bundle);
    }

    Throwable resolved = unwrapRelevantCause(t);
    return switch (resolved) {
      case BusinessException be -> resolveMappedMessage(
          be.getErrorMessage() != null ? be.getErrorMessage().getTranslationKey() : null, bundle,
          "BusinessException");
      case PersistenceException pe -> resolveMappedMessage(
          pe.getErrorMessage() != null ? pe.getErrorMessage().getTranslationKey() : null, bundle,
          "PersistenceException");
      case UserFacingException ufe ->
          resolveMappedMessage(ufe.getTranslationKey(), bundle, "UserFacingException");
      default -> resolveGenericMessage(bundle);
    };
  }

  private static String resolveMappedMessage(String translationKey, ResourceBundle bundle,
      String exceptionType) {
    String localizedMessage = tryGetString(bundle, translationKey);
    if (localizedMessage != null) {
      return localizedMessage;
    }

    log.warn("Missing or invalid translation key '{}' for {}. Falling back to '{}'.",
        translationKey, exceptionType, GENERIC_ERROR_KEY);
    return resolveGenericMessage(bundle);
  }

  private static String resolveGenericMessage(ResourceBundle bundle) {
    String genericMessage = tryGetString(bundle, GENERIC_ERROR_KEY);
    if (genericMessage != null) {
      return genericMessage;
    }

    log.warn("Missing generic translation key '{}'. Falling back to hardcoded message.",
        GENERIC_ERROR_KEY);
    return FALLBACK_ERROR_MESSAGE;
  }

  private static String tryGetString(ResourceBundle bundle, String translationKey) {
    if (bundle == null || translationKey == null || translationKey.isBlank()) {
      return null;
    }

    try {
      return bundle.getString(translationKey);
    } catch (MissingResourceException | ClassCastException e) {
      return null;
    }
  }

  private static Throwable unwrapRelevantCause(Throwable t) {
    Throwable current = t;
    while (current != null) {
      if (current instanceof BusinessException || current instanceof PersistenceException || current instanceof UserFacingException) {
        return current;
      }
      Throwable cause = current.getCause();
      if (cause == null || cause == current) {
        break;
      }
      current = cause;
    }
    return t;
  }
}
