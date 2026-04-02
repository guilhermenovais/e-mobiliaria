package com.guilherme.emobiliaria.person.ui.component;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.EditAddressInput;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.service.FakeAddressSearchService;
import javafx.application.Platform;
import javafx.scene.control.ComboBox;
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

class AddressFormPaneTest {

  private static ResourceBundle bundle;
  private static SearchAddressByCepInteractor stubSearch;

  private static SearchAddressByCepInteractor noopSearch() {
    return new SearchAddressByCepInteractor(new FakeAddressSearchService());
  }

  @BeforeAll
  static void setup() throws InterruptedException {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault());
    stubSearch = noopSearch();
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

  @SuppressWarnings("unchecked")
  private static void fillAllFields(AddressFormPane pane) {
    List<TextField> fields =
        pane.lookupAll(".form-input").stream().filter(n -> n instanceof TextField)
            .map(n -> (TextField) n).toList();

    // CEP field
    fields.get(0).setText("01001-000");
    // street (was readonly, need to enable editing first)
    fields.get(1).setEditable(true);
    fields.get(1).setText("Praça da Sé");
    // number
    fields.get(2).setText("1");
    // complement
    fields.get(3).setText("");
    // neighborhood
    fields.get(4).setEditable(true);
    fields.get(4).setText("Sé");
    // city
    fields.get(5).setEditable(true);
    fields.get(5).setText("São Paulo");

    // state combo
    pane.lookupAll(".form-combo").stream().filter(n -> n instanceof ComboBox)
        .map(n -> (ComboBox<BrazilianState>) n).findFirst().ifPresent(combo -> {
          combo.setDisable(false);
          combo.setValue(BrazilianState.SP);
        });
  }


  @Nested
  class Validate {

    @Test
    @DisplayName("When all fields are empty, should return false")
    void shouldReturnFalseWhenAllFieldsAreEmpty() throws Exception {
      Boolean result = onFX(() -> {
        AddressFormPane pane = new AddressFormPane(stubSearch, bundle);
        return pane.validate();
      });

      assertFalse(result);
    }

    @Test
    @DisplayName("When all required fields are filled, should return true")
    void shouldReturnTrueWhenAllFieldsAreFilled() throws Exception {
      Boolean result = onFX(() -> {
        AddressFormPane pane = new AddressFormPane(stubSearch, bundle);
        fillAllFields(pane);
        return pane.validate();
      });

      assertTrue(result);
    }
  }

  // ── Helpers ────────────────────────────────────────────────────────────────


  @Nested
  class BuildInput {

    @Test
    @DisplayName("When fields are filled, should build input with digits-only CEP")
    void shouldBuildInputWithDigitsOnlyCep() throws Exception {
      CreateAddressInput input = onFX(() -> {
        AddressFormPane pane = new AddressFormPane(stubSearch, bundle);
        fillAllFields(pane);
        return pane.buildInput();
      });

      assertEquals("01001000", input.cep());
      assertEquals("Praça da Sé", input.address());
      assertEquals("1", input.number());
      assertEquals("Sé", input.neighborhood());
      assertEquals("São Paulo", input.city());
      assertEquals(BrazilianState.SP, input.state());
    }
  }

  @Nested
  class Populate {

    @Test
    @DisplayName("Should fill all fields when populated with an Address")
    void shouldFillAllFieldsWhenPopulatedWithAddress() throws Exception {
      Address address = Address.restore(3L, "01001000", "Praça da Sé", "S/N", "Apto 1",
          "Sé", "São Paulo", BrazilianState.SP);

      onFX(() -> {
        AddressFormPane pane = new AddressFormPane(stubSearch, bundle);
        pane.populate(address);

        List<TextField> fields = pane.lookupAll(".form-input").stream()
            .filter(n -> n instanceof TextField).map(n -> (TextField) n).toList();
        // street
        assertEquals("Praça da Sé", fields.get(1).getText());
        // number
        assertEquals("S/N", fields.get(2).getText());
        // complement
        assertEquals("Apto 1", fields.get(3).getText());
        // neighborhood
        assertEquals("Sé", fields.get(4).getText());
        // city
        assertEquals("São Paulo", fields.get(5).getText());

        @SuppressWarnings("unchecked")
        ComboBox<BrazilianState> stateCombo = (ComboBox<BrazilianState>) pane.lookupAll(".form-combo")
            .stream().filter(n -> n instanceof ComboBox).findFirst().orElseThrow();
        assertEquals(BrazilianState.SP, stateCombo.getValue());

        // readonly fields should still be non-editable after populate
        assertFalse(fields.get(1).isEditable());
        assertFalse(fields.get(4).isEditable());
        assertFalse(fields.get(5).isEditable());
        assertTrue(stateCombo.isDisable());

        return null;
      });
    }
  }

  @Nested
  class BuildEditInput {

    @Test
    @DisplayName("Should return EditAddressInput with digits-only CEP when populated")
    void shouldReturnEditAddressInputWithDigitsOnlyCepWhenPopulated() throws Exception {
      Address address = Address.restore(5L, "01001000", "Praça da Sé", "S/N", null,
          "Sé", "São Paulo", BrazilianState.SP);

      EditAddressInput input = onFX(() -> {
        AddressFormPane pane = new AddressFormPane(stubSearch, bundle);
        pane.populate(address);
        return pane.buildEditInput(5L);
      });

      assertEquals(5L, input.id());
      assertEquals("01001000", input.cep());
      assertEquals("Praça da Sé", input.address());
      assertEquals("S/N", input.number());
      assertEquals("Sé", input.neighborhood());
      assertEquals("São Paulo", input.city());
      assertEquals(BrazilianState.SP, input.state());
    }
  }
}
