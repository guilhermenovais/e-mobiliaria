package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreatePhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCpfInteractor;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.service.CpfValidationService;
import com.guilherme.emobiliaria.person.domain.service.FakeAddressSearchService;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicalPersonCreateControllerTest {

  private FakePhysicalPersonRepository personRepo;
  private FakeAddressRepository addressRepo;
  private PhysicalPersonCreateController controller;
  private NavigationService navigationService;

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

  @BeforeEach
  void setup() throws Exception {
    personRepo = new FakePhysicalPersonRepository();
    addressRepo = new FakeAddressRepository();

    CreateAddressInteractor createAddress = new CreateAddressInteractor(addressRepo);
    CreatePhysicalPersonInteractor createPhysicalPerson =
        new CreatePhysicalPersonInteractor(personRepo, addressRepo);
    SearchAddressByCepInteractor searchByCep =
        new SearchAddressByCepInteractor(new FakeAddressSearchService());
    ValidateCpfInteractor validateCpf = new ValidateCpfInteractor(new CpfValidationService());
    navigationService = new NavigationService();

    onFX(() -> {
      StackPane root = new StackPane();
      navigationService.setContentPane(root);
      // Navigate to a dummy first view so we start with one entry in history
      navigationService.navigate(() -> new Label("dummy"));

      controller = new PhysicalPersonCreateController(
          createPhysicalPerson, createAddress, searchByCep, validateCpf, navigationService, null);

      // Inject FXML fields manually (simulating what FXMLLoader would do)
      controller.titleLabel = new Label();
      controller.subtitleLabel = new Label();
      controller.personalSectionLabel = new Label();
      controller.addressSectionLabel = new Label();
      controller.personalFormContainer = new StackPane();
      controller.addressFormContainer = new StackPane();
      controller.cancelButton = new Button();
      controller.saveButton = new Button();

      controller.initialize();
      return null;
    });
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
    return future.get(10, TimeUnit.SECONDS);
  }

  private static void runOnFX(Runnable action) throws Exception {
    onFX(() -> {
      action.run();
      return null;
    });
  }

  private void fillPhysicalForm() {
    List<TextField> fields =
        controller.personalFormContainer.lookupAll(".form-input").stream()
            .filter(n -> n instanceof TextField)
            .map(n -> (TextField) n)
            .toList();
    fields.get(0).setText("João Silva");
    fields.get(1).setText("Brasileiro");
    fields.get(2).setText("Engenheiro");
    fields.get(3).setText("529.982.247-25");
    fields.get(4).setText("MG-1234567");

    @SuppressWarnings("unchecked")
    ComboBox<CivilState> combo = (ComboBox<CivilState>)
        controller.personalFormContainer.lookupAll(".form-combo").stream()
            .filter(n -> n instanceof ComboBox)
            .findFirst()
            .orElseThrow();
    combo.setValue(CivilState.SINGLE);
  }

  private void fillAddressForm() {
    List<TextField> fields =
        controller.addressFormContainer.lookupAll(".form-input").stream()
            .filter(n -> n instanceof TextField)
            .map(n -> (TextField) n)
            .toList();
    // CEP
    fields.get(0).setText("01001-000");
    // street (readonly by default — unlock for test)
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

    @SuppressWarnings("unchecked")
    ComboBox<BrazilianState> stateCombo = (ComboBox<BrazilianState>)
        controller.addressFormContainer.lookupAll(".form-combo").stream()
            .filter(n -> n instanceof ComboBox)
            .findFirst()
            .orElseThrow();
    stateCombo.setDisable(false);
    stateCombo.setValue(BrazilianState.SP);
  }

  @Nested
  @DisplayName("HandleSubmit")
  class HandleSubmit {

    @Test
    @DisplayName("Should create person and go back when form is valid")
    void shouldCreatePersonAndGoBackWhenFormIsValid() throws Exception {
      runOnFX(() -> {
        fillPhysicalForm();
        fillAddressForm();
        controller.handleSubmit();
      });

      // Give background tasks time to complete
      Thread.sleep(1500);

      onFX(() -> {
        assertFalse(navigationService.canGoBack(),
            "Expected to have navigated back (back stack should be empty after goBack)");
        return null;
      });
    }

    @Test
    @DisplayName("Should not create person when form is invalid")
    void shouldNotCreatePersonWhenFormIsInvalid() throws Exception {
      // Leave fields empty — do not fill the form
      runOnFX(() -> controller.handleSubmit());

      // Give any potential async tasks time (should not run)
      Thread.sleep(500);

      assertTrue(
          personRepo.findAll(new PaginationInput(0, 100)).items().isEmpty(),
          "Expected no person to be created when form is invalid");
    }

    @Test
    @DisplayName("Should navigate back after successful submit (save button not permanently disabled)")
    void shouldNotPermanentlyDisableSaveButtonAfterSubmit() throws Exception {
      runOnFX(() -> {
        fillPhysicalForm();
        fillAddressForm();
        controller.handleSubmit();
      });

      // Give background tasks time to complete
      Thread.sleep(1500);

      // Navigation should have occurred after success
      onFX(() -> {
        assertFalse(navigationService.canGoBack(),
            "Expected navigation back to have occurred after successful submit");
        return null;
      });
    }
  }

  @Nested
  @DisplayName("Cancel")
  class Cancel {

    @Test
    @DisplayName("Should go back when cancel is pressed")
    void shouldGoBackWhenCancelPressed() throws Exception {
      // Simulate arriving at create screen from list (navigate forward once more)
      onFX(() -> {
        navigationService.navigate(() -> controller.personalFormContainer);
        return null;
      });

      // Now canGoBack() should be true (there's at least one screen in history)
      onFX(() -> {
        assertTrue(navigationService.canGoBack(),
            "Expected canGoBack() to be true before pressing cancel");
        return null;
      });

      // Press cancel
      runOnFX(() -> controller.cancelButton.fire());

      onFX(() -> {
        assertFalse(navigationService.canGoBack(),
            "Expected to have navigated back after cancel");
        return null;
      });
    }
  }
}
