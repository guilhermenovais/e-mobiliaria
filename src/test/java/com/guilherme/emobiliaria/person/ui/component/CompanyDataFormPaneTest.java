package com.guilherme.emobiliaria.person.ui.component;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CompanyDataFormPaneTest {

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
  class Validate {

    @Test
    @DisplayName("When all fields are empty, should return false")
    void shouldReturnFalseWhenAllFieldsAreEmpty() throws Exception {
      Boolean result = onFX(() -> {
        CompanyDataFormPane pane = new CompanyDataFormPane(bundle);
        return pane.validate();
      });

      assertFalse(result);
    }

    @Test
    @DisplayName("When all fields are filled, should return true")
    void shouldReturnTrueWhenAllFieldsAreFilled() throws Exception {
      Boolean result = onFX(() -> {
        CompanyDataFormPane pane = new CompanyDataFormPane(bundle);
        List<TextField> fields =
            pane.lookupAll(".form-input").stream().filter(n -> n instanceof TextField)
                .map(n -> (TextField) n).toList();
        fields.get(0).setText("Empresa Ltda");
        fields.get(1).setText("12.345.678/0001-90");
        return pane.validate();
      });

      assertTrue(result);
    }
  }


  @Nested
  class GetCorporateName {

    @Test
    @DisplayName("When field has value with whitespace, should return trimmed value")
    void shouldReturnTrimmedCorporateName() throws Exception {
      String name = onFX(() -> {
        CompanyDataFormPane pane = new CompanyDataFormPane(bundle);
        List<TextField> fields =
            pane.lookupAll(".form-input").stream().filter(n -> n instanceof TextField)
                .map(n -> (TextField) n).toList();
        fields.get(0).setText("  Empresa Ltda  ");
        return pane.getCorporateName();
      });

      assertEquals("Empresa Ltda", name);
    }
  }


  @Nested
  class GetCnpj {

    @Test
    @DisplayName("When field has value with whitespace, should return trimmed value")
    void shouldReturnTrimmedCnpj() throws Exception {
      String cnpj = onFX(() -> {
        CompanyDataFormPane pane = new CompanyDataFormPane(bundle);
        List<TextField> fields =
            pane.lookupAll(".form-input").stream().filter(n -> n instanceof TextField)
                .map(n -> (TextField) n).toList();
        fields.get(1).setText("  12.345.678/0001-90  ");
        return pane.getCnpj();
      });

      assertEquals("12.345.678/0001-90", cnpj);
    }
  }
}
