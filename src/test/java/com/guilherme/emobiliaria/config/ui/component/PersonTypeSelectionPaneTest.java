package com.guilherme.emobiliaria.config.ui.component;

import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PersonTypeSelectionPaneTest {

  private static ResourceBundle bundle;

  @BeforeAll
  static void setup() throws InterruptedException {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
    CountDownLatch latch = new CountDownLatch(1);
    try {
      Platform.startup(latch::countDown);
    } catch (IllegalStateException ignored) {
      latch.countDown();
    }
    assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX platform did not start");
  }

  private static <T> T onFX(java.util.concurrent.Callable<T> action) throws Exception {
    CompletableFuture<T> future = new CompletableFuture<>();
    Platform.runLater(() -> {
      try {
        future.complete(action.call());
      } catch (Throwable t) {
        future.completeExceptionally(t);
      }
    });
    return future.get(5, TimeUnit.SECONDS);
  }

  @Nested
  class GetSelectedType {

    @Test
    @DisplayName("When no card is selected, should return null")
    void shouldReturnNullWhenNoCardSelected() throws Exception {
      PersonType type = onFX(() -> {
        PersonTypeSelectionPane pane = new PersonTypeSelectionPane(bundle);
        return pane.getSelectedType();
      });

      assertNull(type);
    }
  }


  @Nested
  class Validate {

    @Test
    @DisplayName("When no type is selected, should return false")
    void shouldReturnFalseWhenNoTypeSelected() throws Exception {
      Boolean result = onFX(() -> {
        PersonTypeSelectionPane pane = new PersonTypeSelectionPane(bundle);
        return pane.validate();
      });

      assertFalse(result);
    }

    @Test
    @DisplayName("When validate fails twice, error label should remain visible")
    void shouldKeepErrorLabelVisibleOnRepeatedValidationFailure() throws Exception {
      Boolean secondResult = onFX(() -> {
        PersonTypeSelectionPane pane = new PersonTypeSelectionPane(bundle);
        pane.validate();
        return pane.validate();
      });

      assertFalse(secondResult);
    }
  }
}
