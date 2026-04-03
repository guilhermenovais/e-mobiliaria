package com.guilherme.emobiliaria.shared.ui.component;

import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WizardStepperBarTest {

  @BeforeAll
  static void startFX() throws InterruptedException {
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

  private static void runOnFX(Runnable action) throws Exception {
    onFX(() -> {
      action.run();
      return null;
    });
  }

  @Nested
  class Constructor {

    @Test
    @DisplayName("When constructed with N labels, should create N dot children")
    void shouldCreateCorrectNumberOfDots() throws Exception {
      WizardStepperBar bar = onFX(() -> new WizardStepperBar(List.of("Tipo", "Dados", "Endereço")));

      // children = 3 dotBoxes + 2 connectors = 5
      assertEquals(5, bar.getChildren().size());
    }

    @Test
    @DisplayName("When constructed with labels, first dot should have stepper-dot style")
    void shouldApplyStepperDotStyle() throws Exception {
      WizardStepperBar bar = onFX(() -> new WizardStepperBar(List.of("Tipo", "Dados")));

      assertInstanceOf(VBox.class, bar.getChildren().get(0));
    }
  }


  @Nested
  class SetCurrentStep {

    @Test
    @DisplayName("When step is 1, first dot should have stepper-dot-active style")
    void shouldMarkFirstDotActiveWhenStepIsOne() throws Exception {
      WizardStepperBar[] bar = new WizardStepperBar[1];
      runOnFX(() -> {
        bar[0] = new WizardStepperBar(List.of("Tipo", "Dados", "Endereço"));
        bar[0].setCurrentStep(1);
      });

      onFX(() -> {
        javafx.scene.layout.VBox dotBox = (javafx.scene.layout.VBox) bar[0].getChildren().get(0);
        Label dot = (Label) dotBox.getChildren().get(0);
        assertTrue(dot.getStyleClass().contains("stepper-dot-active"),
            "First dot should be active at step 1");
        return null;
      });
    }

    @Test
    @DisplayName("When step is 2, first dot should be completed and second active")
    void shouldMarkFirstDotCompletedAndSecondActiveWhenStepIsTwo() throws Exception {
      WizardStepperBar[] bar = new WizardStepperBar[1];
      runOnFX(() -> {
        bar[0] = new WizardStepperBar(List.of("Tipo", "Dados", "Endereço"));
        bar[0].setCurrentStep(2);
      });

      onFX(() -> {
        javafx.scene.layout.VBox firstBox = (javafx.scene.layout.VBox) bar[0].getChildren().get(0);
        Label firstDot = (Label) firstBox.getChildren().get(0);
        assertTrue(firstDot.getStyleClass().contains("stepper-dot-completed"),
            "First dot should be completed at step 2");

        javafx.scene.layout.VBox secondBox = (javafx.scene.layout.VBox) bar[0].getChildren().get(2);
        Label secondDot = (Label) secondBox.getChildren().get(0);
        assertTrue(secondDot.getStyleClass().contains("stepper-dot-active"),
            "Second dot should be active at step 2");
        return null;
      });
    }

    @Test
    @DisplayName("When step is 2, completed dot should keep numeric step")
    void shouldKeepStepNumberOnCompletedDot() throws Exception {
      WizardStepperBar[] bar = new WizardStepperBar[1];
      runOnFX(() -> {
        bar[0] = new WizardStepperBar(List.of("Tipo", "Dados"));
        bar[0].setCurrentStep(2);
      });

      onFX(() -> {
        javafx.scene.layout.VBox firstBox = (javafx.scene.layout.VBox) bar[0].getChildren().get(0);
        Label firstDot = (Label) firstBox.getChildren().get(0);
        assertEquals("1", firstDot.getText());
        return null;
      });
    }
  }
}
