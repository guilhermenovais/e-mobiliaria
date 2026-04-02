package com.guilherme.emobiliaria.shared.ui;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class NavigationServiceTest {

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

  private NavigationService service;
  private StackPane contentPane;

  @BeforeEach
  void setUp() throws Exception {
    runOnFX(() -> {
      service = new NavigationService();
      contentPane = new StackPane();
      service.setContentPane(contentPane);
    });
  }

  @Nested
  class Navigate {

    @Test
    @DisplayName("Should set current view when navigate called for the first time")
    void shouldSetCurrentViewWhenNavigateCalled() throws Exception {
      Supplier<Node> factory = VBox::new;

      runOnFX(() -> service.navigate(factory));

      onFX(() -> {
        assertFalse(service.canGoBack(), "First navigation should have nothing to go back to");
        assertEquals(1, contentPane.getChildren().size(), "Content pane should have one child");
        return null;
      });
    }

    @Test
    @DisplayName("Should push previous view to back stack on second navigate")
    void shouldPushToPreviousBackStackOnSecondNavigate() throws Exception {
      runOnFX(() -> {
        service.navigate(VBox::new);
        service.navigate(VBox::new);
      });

      assertTrue(service.canGoBack(), "Should be able to go back after second navigation");
    }

    @Test
    @DisplayName("Should clear forward stack on new navigate")
    void shouldClearForwardStackOnNewNavigate() throws Exception {
      runOnFX(() -> {
        service.navigate(VBox::new); // A
        service.navigate(VBox::new); // B
        service.goBack();            // back to A
        service.navigate(VBox::new); // C — should clear forward stack
      });

      assertFalse(service.canGoForward(), "Forward stack should be cleared after new navigation");
    }
  }

  @Nested
  class GoBack {

    @Test
    @DisplayName("Should do nothing when back stack is empty")
    void shouldDoNothingWhenBackStackIsEmpty() throws Exception {
      runOnFX(() -> service.goBack());

      assertFalse(service.canGoBack(), "canGoBack should still be false after goBack on empty stack");
    }

    @Test
    @DisplayName("Should restore previous view when going back")
    void shouldRestorePreviousViewWhenGoingBack() throws Exception {
      runOnFX(() -> {
        service.navigate(VBox::new); // A
        service.navigate(VBox::new); // B
        service.goBack();            // back to A
      });

      assertFalse(service.canGoBack(), "Back stack should be empty after returning to root");
      assertTrue(service.canGoForward(), "Forward stack should have B");
    }
  }

  @Nested
  class GoForward {

    @Test
    @DisplayName("Should restore forward view when going forward")
    void shouldRestoreForwardViewWhenGoingForward() throws Exception {
      runOnFX(() -> {
        service.navigate(VBox::new); // A
        service.navigate(VBox::new); // B
        service.goBack();            // back to A
        service.goForward();         // back to B
      });

      assertFalse(service.canGoForward(), "Forward stack should be empty after going forward");
      assertTrue(service.canGoBack(), "Back stack should have A");
    }
  }
}
