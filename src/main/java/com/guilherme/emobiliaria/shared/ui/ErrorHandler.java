package com.guilherme.emobiliaria.shared.ui;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ResourceBundle;

public final class ErrorHandler {
  private static final Logger log = LoggerFactory.getLogger(ErrorHandler.class);

  private ErrorHandler() {
  }

  /**
   * Logs the error and shows a localized Alert dialog on the FX thread. For BusinessException, uses
   * the translation key. For all others, falls back to "error.generic".
   */
  public static void handle(Throwable t, ResourceBundle bundle) {
    log.error(t.getMessage(), t);
    Platform.runLater(() -> {
      String message =
          t instanceof BusinessException be ? bundle.getString(be.getErrorMessage().getTranslationKey()) :
          t instanceof PersistenceException pe ? bundle.getString(pe.getErrorMessage().getTranslationKey()) :
          bundle.getString("error.generic");

      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Erro");
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
    });
  }
}
