package com.guilherme.emobiliaria.shared.ui.component;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.ListResourceBundle;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
            {"sidebar.brand", "e-mobiliaria"},
            {"sidebar.dashboard", "Painel"},
            {"sidebar.physical_people", "Pessoas Físicas"},
            {"sidebar.juridical_people", "Pessoas Jurídicas"},
            {"sidebar.properties", "Imóveis"},
            {"sidebar.contracts", "Contratos"},
            {"sidebar.receipts", "Recibos"},
            {"sidebar.config", "Configurações"},
            {"nav.back", "Voltar"},
            {"nav.forward", "Avançar"},
        };
      }
    };
  }

  private static java.util.List<Button> getAllButtons(Node root) {
    java.util.List<Button> buttons = new ArrayList<>();
    collectButtons(root, buttons);
    return buttons;
  }

  private static void collectButtons(Node node, java.util.List<Button> buttons) {
    if (node instanceof Button button) {
      buttons.add(button);
    }
    if (node instanceof Parent parent) {
      for (Node child : parent.getChildrenUnmodifiable()) {
        collectButtons(child, buttons);
      }
    }
  }

  @Nested
  class Constructor {

    @Test
    @DisplayName("Should create seven entries when constructed")
    void shouldCreateSixEntriesWhenConstructed() throws Exception {
      SidebarPane pane = onFX(() -> new SidebarPane(testBundle()));

      assertEquals(7, getAllButtons(pane).size(), "SidebarPane should have exactly 7 buttons");
    }

    @Test
    @DisplayName("Should disable only config entry when constructed")
    void shouldDisableAllEntriesExceptPhysicalPeopleWhenConstructed() throws Exception {
      SidebarPane pane = onFX(() -> new SidebarPane(testBundle()));

      onFX(() -> {
        long disabledCount = getAllButtons(pane).stream()
            .filter(Button::isDisable)
            .count();
        long enabledCount = getAllButtons(pane).stream()
            .filter(n -> !n.isDisable())
            .count();
        assertEquals(1, disabledCount, "One button should be disabled");
        assertEquals(6, enabledCount, "Six buttons should be enabled");
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
        Button physicalBtn = getAllButtons(pane[0]).stream()
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
        getAllButtons(pane[0]).stream()
            .filter(b -> "Configurações".equals(b.getText()))
            .findFirst()
            .ifPresent(b -> b.setDisable(false));
        pane[0].setActiveItem("sidebar.config");
      });

      onFX(() -> {
        Button physicalBtn = getAllButtons(pane[0]).stream()
            .filter(b -> "Pessoas Físicas".equals(b.getText()))
            .findFirst()
            .orElseThrow(() -> new AssertionError("Physical people button not found"));
        assertFalse(physicalBtn.getStyleClass().contains("sidebar-item-active"),
            "Physical people button should no longer have sidebar-item-active CSS class");
        return null;
      });
    }
  }
}
