package com.guilherme.emobiliaria.shared.ui;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.guilherme.emobiliaria.shared.exception.UserFacingException;
import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ErrorHandlerTest {

  private final ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.ENGLISH);

  @Test
  void shouldResolveBusinessExceptionMessage() {
    BusinessException exception =
        new BusinessException(ErrorMessage.Receipt.INTERVAL_END_BEFORE_INTERVAL_START);

    String message = ErrorHandler.resolveMessage(exception, bundle);

    assertEquals(bundle.getString("receipt.interval_end_before_interval_start"), message);
  }

  @Test
  void shouldResolveUserFacingExceptionMessage() {
    UserFacingException exception =
        new UserFacingException("receipt.form.error.interval_invalid", "Validation failure");

    String message = ErrorHandler.resolveMessage(exception, bundle);

    assertEquals(bundle.getString("receipt.form.error.interval_invalid"), message);
  }

  @Test
  void shouldResolveWrappedBusinessExceptionMessage() {
    RuntimeException wrapped = new RuntimeException(
        new BusinessException(ErrorMessage.Receipt.INTERVAL_END_BEFORE_INTERVAL_START));

    String message = ErrorHandler.resolveMessage(wrapped, bundle);

    assertEquals(bundle.getString("receipt.interval_end_before_interval_start"), message);
  }

  @Test
  void shouldFallbackToGenericMessageForUnhandledException() {
    IllegalStateException exception = new IllegalStateException("Boom");

    String message = ErrorHandler.resolveMessage(exception, bundle);

    assertEquals(bundle.getString("error.generic"), message);
  }

  @Test
  void shouldFallbackToGenericMessageWhenBusinessExceptionTranslationKeyIsMissing() {
    ErrorMessage missingKeyMessage = new ErrorMessage() {
      @Override
      public String getTranslationKey() {
        return "test.missing.business.key";
      }

      @Override
      public String getLogMessage() {
        return "Missing business translation key";
      }
    };
    BusinessException exception = new BusinessException(missingKeyMessage);

    String message = assertDoesNotThrow(() -> ErrorHandler.resolveMessage(exception, bundle));

    assertEquals(bundle.getString("error.generic"), message);
  }

  @Test
  void shouldFallbackToGenericMessageWhenPersistenceExceptionTranslationKeyIsMissing() {
    ErrorMessage missingKeyMessage = new ErrorMessage() {
      @Override
      public String getTranslationKey() {
        return "test.missing.persistence.key";
      }

      @Override
      public String getLogMessage() {
        return "Missing persistence translation key";
      }
    };
    PersistenceException exception =
        new PersistenceException(missingKeyMessage, new IllegalStateException("DB error"));

    String message = assertDoesNotThrow(() -> ErrorHandler.resolveMessage(exception, bundle));

    assertEquals(bundle.getString("error.generic"), message);
  }

  @Test
  void shouldFallbackToGenericMessageWhenUserFacingExceptionTranslationKeyIsMissing() {
    UserFacingException exception =
        new UserFacingException("test.missing.user_facing.key", "Validation failure");

    String message = assertDoesNotThrow(() -> ErrorHandler.resolveMessage(exception, bundle));

    assertEquals(bundle.getString("error.generic"), message);
  }

  @Test
  void shouldFallbackToHardcodedMessageWhenGenericTranslationKeyIsUnavailable() {
    ResourceBundle withoutGenericBundle = new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[0][0];
      }
    };
    UserFacingException exception =
        new UserFacingException("test.missing.user_facing.key", "Validation failure");

    String message =
        assertDoesNotThrow(() -> ErrorHandler.resolveMessage(exception, withoutGenericBundle));

    assertEquals("An unexpected error occurred.", message);
  }
}
