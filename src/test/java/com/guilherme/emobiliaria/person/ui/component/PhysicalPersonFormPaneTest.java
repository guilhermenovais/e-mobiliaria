package com.guilherme.emobiliaria.person.ui.component;

import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicalPersonFormPaneTest {

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

  private static void runOnFX(Runnable action) throws Exception {
    onFX(() -> {
      action.run();
      return null;
    });
  }

  private static void fillAllFields(PhysicalPersonFormPane pane) {
    fillTextField(pane, 0, "João Silva");
    fillTextField(pane, 1, "Brasileiro");
    findCombo(pane).setValue(CivilState.SINGLE);
    fillTextField(pane, 2, "Engenheiro");
    fillTextField(pane, 3, "529.982.247-25");
    fillTextField(pane, 4, "MG-1234567");
  }

  @SuppressWarnings("unchecked")
  private static ComboBox<CivilState> findCombo(PhysicalPersonFormPane pane) {
    return (ComboBox<CivilState>) pane.lookupAll(".form-combo").stream()
        .filter(n -> n instanceof ComboBox).findFirst().orElseThrow();
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private static void fillTextField(PhysicalPersonFormPane pane, int index, String value) {
    var fields = pane.lookupAll(".form-input").stream().filter(n -> n instanceof TextField)
        .map(n -> (TextField) n).toList();
    if (index < fields.size()) {
      fields.get(index).setText(value);
    }
  }


  @Nested
  class Validate {

    @Test
    @DisplayName("When all fields are empty, should return false")
    void shouldReturnFalseWhenAllFieldsAreEmpty() throws Exception {
      Boolean result = onFX(() -> {
        PhysicalPersonFormPane pane = new PhysicalPersonFormPane(bundle);
        return pane.validate();
      });

      assertFalse(result);
    }

    @Test
    @DisplayName("When all required fields are filled, should return true")
    void shouldReturnTrueWhenAllFieldsAreFilled() throws Exception {
      Boolean result = onFX(() -> {
        PhysicalPersonFormPane pane = new PhysicalPersonFormPane(bundle);
        fillAllFields(pane);
        return pane.validate();
      });

      assertTrue(result);
    }

    @Test
    @DisplayName("When civil state is not selected, should return false")
    void shouldReturnFalseWhenCivilStateNotSelected() throws Exception {
      Boolean result = onFX(() -> {
        PhysicalPersonFormPane pane = new PhysicalPersonFormPane(bundle);
        fillTextField(pane, 0, "João Silva");
        fillTextField(pane, 1, "Brasileiro");
        // skip civil state
        fillTextField(pane, 2, "Engenheiro");
        fillTextField(pane, 3, "529.982.247-25");
        fillTextField(pane, 4, "MG-1234567");
        return pane.validate();
      });

      assertFalse(result);
    }
  }


  @Nested
  class BuildInput {

    @Test
    @DisplayName("When fields are filled, should build input with trimmed values")
    void shouldBuildInputWithTrimmedValues() throws Exception {
      CreatePhysicalPersonInput input = onFX(() -> {
        PhysicalPersonFormPane pane = new PhysicalPersonFormPane(bundle);
        fillAllFields(pane);
        return pane.buildInput(42L);
      });

      assertEquals("João Silva", input.name());
      assertEquals("Brasileiro", input.nationality());
      assertEquals(CivilState.SINGLE, input.civilState());
      assertEquals("Engenheiro", input.occupation());
      assertEquals("529.982.247-25", input.cpf());
      assertEquals("MG-1234567", input.idCardNumber());
      assertEquals(42L, input.addressId());
    }
  }
}
