package com.guilherme.emobiliaria.shared.ui.component;

import javafx.application.Platform;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SidebarPaneTest {

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

  private static ResourceBundle testBundle() {
    return new ListResourceBundle() {
      @Override
      protected Object[][] getContents() {
        return new Object[][]{
            {"sidebar.physical_people", "Pessoas Físicas"},
            {"sidebar.juridical_people", "Pessoas Jurídicas"},
            {"sidebar.properties", "Imóveis"},
            {"sidebar.contracts", "Contratos"},
            {"sidebar.receipts", "Recibos"},
            {"sidebar.config", "Config"},
            {"nav.back", "Voltar"},
            {"nav.forward", "Avançar"},
        };
      }
    };
  }

  @Nested
  class Constructor {

    @Test
    @DisplayName("Should create six entries when constructed")
    void shouldCreateSixEntriesWhenConstructed() throws Exception {
      SidebarPane pane = onFX(() -> new SidebarPane(testBundle()));

      assertEquals(6, pane.getChildren().size(), "SidebarPane should have exactly 6 button children");
    }

    @Test
    @DisplayName("Should disable all entries except physical people when constructed")
    void shouldDisableAllEntriesExceptPhysicalPeopleWhenConstructed() throws Exception {
      SidebarPane pane = onFX(() -> new SidebarPane(testBundle()));

      onFX(() -> {
        long disabledCount = pane.getChildren().stream()
            .filter(n -> n instanceof Button)
            .filter(n -> ((Button) n).isDisable())
            .count();
        long enabledCount = pane.getChildren().stream()
            .filter(n -> n instanceof Button)
            .filter(n -> !((Button) n).isDisable())
            .count();
        assertEquals(5, disabledCount, "Five buttons should be disabled");
        assertEquals(1, enabledCount, "One button should be enabled");
        return null;
      });
    }
  }

  @Nested
  class SetActiveItem {

    @Test
    @DisplayName("Should highlight physical people entry when set as active")
    void shouldHighlightPhysicalPeopleEntryWhenSetAsActive() throws Exception {
      SidebarPane[] pane = new SidebarPane[1];
      runOnFX(() -> {
        pane[0] = new SidebarPane(testBundle());
        pane[0].setActiveItem("sidebar.physical_people");
      });

      onFX(() -> {
        Button physicalBtn = pane[0].getChildren().stream()
            .filter(n -> n instanceof Button)
            .map(n -> (Button) n)
            .filter(b -> "Pessoas Físicas".equals(b.getText()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Physical people button not found"));
        assertTrue(physicalBtn.getStyleClass().contains("sidebar-item-active"),
            "Physical people button should have sidebar-item-active CSS class");
        return null;
      });
    }

    @Test
    @DisplayName("Should remove previous active highlight when new active item set")
    void shouldRemovePreviousActiveHighlightWhenNewActiveItemSet() throws Exception {
      SidebarPane[] pane = new SidebarPane[1];
      runOnFX(() -> {
        pane[0] = new SidebarPane(testBundle());
        pane[0].setActiveItem("sidebar.physical_people");
        // Enable config button to be able to set it as active for testing
        pane[0].getChildren().stream()
            .filter(n -> n instanceof Button)
            .map(n -> (Button) n)
            .filter(b -> "Config".equals(b.getText()))
            .findFirst()
            .ifPresent(b -> b.setDisable(false));
        pane[0].setActiveItem("sidebar.config");
      });

      onFX(() -> {
        Button physicalBtn = pane[0].getChildren().stream()
            .filter(n -> n instanceof Button)
            .map(n -> (Button) n)
            .filter(b -> "Pessoas Físicas".equals(b.getText()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Physical people button not found"));
        assertTrue(!physicalBtn.getStyleClass().contains("sidebar-item-active"),
            "Physical people button should no longer have sidebar-item-active CSS class");
        return null;
      });
    }
  }
}
