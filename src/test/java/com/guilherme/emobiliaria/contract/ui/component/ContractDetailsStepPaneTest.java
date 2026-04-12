package com.guilherme.emobiliaria.contract.ui.component;

import javafx.application.Platform;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractDetailsStepPaneTest {

  @BeforeAll
  static void startFx() throws InterruptedException {
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

  private static ContractDetailsStepPane createPane() {
    ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        ContractDetailsStepPane.class.getModule());
    return new ContractDetailsStepPane(bundle);
  }

  private static TextField rentField(ContractDetailsStepPane pane) {
    try {
      Field field = ContractDetailsStepPane.class.getDeclaredField("rentField");
      field.setAccessible(true);
      return (TextField) field.get(pane);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Nested
  class Populate {

    @Test
    @DisplayName("When pane is populated with rent cents, should format rent using currency mask")
    void shouldFormatRentUsingCurrencyMaskWhenPaneIsPopulatedWithRentCents() throws Exception {
      String rentText = onFX(() -> {
        ContractDetailsStepPane pane = createPane();
        pane.populate(LocalDate.of(2026, 4, 10), 12, 123456, 10, "Residencial");
        return rentField(pane).getText();
      });

      assertEquals("1.234,56", rentText);
    }
  }

  @Nested
  class GetRentCents {

    @Test
    @DisplayName("When only digits are typed, should apply mask and return rent in cents")
    void shouldApplyMaskAndReturnRentInCentsWhenOnlyDigitsAreTyped() throws Exception {
      int cents = onFX(() -> {
        ContractDetailsStepPane pane = createPane();
        rentField(pane).setText("1234");
        return pane.getRentCents();
      });

      assertEquals(1234, cents);
    }

    @Test
    @DisplayName("When mixed text is typed, should ignore non-digits and return rent in cents")
    void shouldIgnoreNonDigitsAndReturnRentInCentsWhenMixedTextIsTyped() throws Exception {
      int cents = onFX(() -> {
        ContractDetailsStepPane pane = createPane();
        rentField(pane).setText("ab1c2");
        return pane.getRentCents();
      });

      assertEquals(12, cents);
    }

    @Test
    @DisplayName("When rent field is empty, should return invalid sentinel value")
    void shouldReturnInvalidSentinelValueWhenRentFieldIsEmpty() throws Exception {
      int cents = onFX(() -> {
        ContractDetailsStepPane pane = createPane();
        rentField(pane).setText("");
        return pane.getRentCents();
      });

      assertEquals(-1, cents);
    }
  }
}

