package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.usecase.EditAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditPhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindPhysicalPersonByIdInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCpfInteractor;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.service.CpfValidationService;
import com.guilherme.emobiliaria.person.domain.service.FakeAddressSearchService;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import javafx.application.Platform;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhysicalPersonEditControllerTest {

  @BeforeAll
  static void startJavaFx() throws InterruptedException {
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
    return future.get(10, TimeUnit.SECONDS);
  }

  private static void runOnFX(Runnable action) throws Exception {
    onFX(() -> {
      action.run();
      return null;
    });
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private static Address sampleAddress() {
    return Address.restore(null, "01001000", "Praça da Sé", "S/N", null,
        "Sé", "São Paulo", BrazilianState.SP);
  }

  private static PhysicalPerson samplePerson(Address address) {
    return PhysicalPerson.restore(null, "João Silva", "Brasileiro",
        CivilState.SINGLE, "Engenheiro", "52998224725", "MG-1234567", address);
  }

  record Fixture(
      PhysicalPersonEditController controller,
      FakePhysicalPersonRepository personRepo,
      FakeAddressRepository addressRepo,
      NavigationService navigationService) {}

  private static Fixture buildFixture() {
    FakePhysicalPersonRepository personRepo = new FakePhysicalPersonRepository();
    FakeAddressRepository addressRepo = new FakeAddressRepository();

    Address address = sampleAddress();
    addressRepo.create(address);
    PhysicalPerson person = samplePerson(addressRepo.findById(address.getId()).orElseThrow());
    personRepo.create(person);

    FindPhysicalPersonByIdInteractor findById = new FindPhysicalPersonByIdInteractor(personRepo);
    EditPhysicalPersonInteractor editPerson =
        new EditPhysicalPersonInteractor(personRepo, addressRepo);
    EditAddressInteractor editAddress = new EditAddressInteractor(addressRepo);
    SearchAddressByCepInteractor searchByCep =
        new SearchAddressByCepInteractor(new FakeAddressSearchService());
    ValidateCpfInteractor validateCpf = new ValidateCpfInteractor(new CpfValidationService());
    NavigationService navigationService = new NavigationService();

    PhysicalPersonEditController controller = new PhysicalPersonEditController(
        findById, editPerson, editAddress, searchByCep, validateCpf, navigationService);

    return new Fixture(controller, personRepo, addressRepo, navigationService);
  }

  // ─────────────────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("Initialize")
  class Initialize {

    @Test
    @DisplayName("Should populate form fields when person ID is set")
    void shouldPopulateFormFieldsWhenPersonIdIsSet() throws Exception {
      Fixture fixture = buildFixture();
      PhysicalPersonEditController controller = fixture.controller();
      long personId = fixture.personRepo().findAll(
          new com.guilherme.emobiliaria.shared.persistence.PaginationInput(1, 0)).items()
          .get(0).getId();

      onFX(() -> {
        StackPane root = new StackPane();
        fixture.navigationService().setContentPane(root);
        fixture.navigationService().navigate(() -> new javafx.scene.control.Label("list"));
        controller.setPersonId(personId);
        controller.initialize();
        return null;
      });

      // Wait for background task to complete
      Thread.sleep(500);

      onFX(() -> {
        List<TextField> nameFields = controller.personalFormContainer.lookupAll(".form-input")
            .stream()
            .filter(n -> n instanceof TextField)
            .map(n -> (TextField) n)
            .toList();
        assertEquals("João Silva", nameFields.get(0).getText());
        return null;
      });
    }

    @Test
    @DisplayName("Should do nothing when person ID is zero")
    void shouldDoNothingWhenPersonIdIsZero() throws Exception {
      Fixture fixture = buildFixture();
      PhysicalPersonEditController controller = fixture.controller();

      assertDoesNotThrow(() -> onFX(() -> {
        StackPane root = new StackPane();
        fixture.navigationService().setContentPane(root);
        controller.setPersonId(0L);
        controller.initialize();
        return null;
      }));

      // Form containers are initialized but no data is loaded when id is 0
      onFX(() -> {
        List<TextField> nameFields = controller.personalFormContainer.lookupAll(".form-input")
            .stream()
            .filter(n -> n instanceof TextField)
            .map(n -> (TextField) n)
            .toList();
        assertTrue(nameFields.isEmpty()
            || nameFields.get(0).getText() == null
            || nameFields.get(0).getText().isBlank());
        return null;
      });
    }
  }

  @Nested
  @DisplayName("HandleSubmit")
  class HandleSubmit {

    @Test
    @DisplayName("Should update person when form is valid and submitted")
    void shouldUpdatePersonWhenFormIsValidAndSubmitted() throws Exception {
      Fixture fixture = buildFixture();
      PhysicalPersonEditController controller = fixture.controller();
      long personId = fixture.personRepo().findAll(
          new com.guilherme.emobiliaria.shared.persistence.PaginationInput(1, 0)).items()
          .get(0).getId();

      onFX(() -> {
        StackPane root = new StackPane();
        fixture.navigationService().setContentPane(root);
        fixture.navigationService().navigate(() -> new javafx.scene.control.Label("list"));
        controller.setPersonId(personId);
        controller.initialize();
        return null;
      });

      // Wait for background load to complete
      Thread.sleep(500);

      // Modify the name field
      onFX(() -> {
        List<TextField> nameFields = controller.personalFormContainer.lookupAll(".form-input")
            .stream()
            .filter(n -> n instanceof TextField)
            .map(n -> (TextField) n)
            .toList();
        nameFields.get(0).setText("Maria Souza");
        return null;
      });

      runOnFX(controller::handleSubmit);

      // Wait for background task to complete
      Thread.sleep(500);

      onFX(() -> {
        PhysicalPerson updated = fixture.personRepo().findById(personId).orElseThrow();
        assertEquals("Maria Souza", updated.getName());
        return null;
      });
    }

    @Test
    @DisplayName("Should not submit when form is invalid")
    void shouldNotSubmitWhenFormIsInvalid() throws Exception {
      Fixture fixture = buildFixture();
      PhysicalPersonEditController controller = fixture.controller();
      long personId = fixture.personRepo().findAll(
          new com.guilherme.emobiliaria.shared.persistence.PaginationInput(1, 0)).items()
          .get(0).getId();

      onFX(() -> {
        StackPane root = new StackPane();
        fixture.navigationService().setContentPane(root);
        fixture.navigationService().navigate(() -> new javafx.scene.control.Label("list"));
        controller.setPersonId(personId);
        controller.initialize();
        return null;
      });

      // Wait for background load
      Thread.sleep(500);

      // Clear the required name field to make form invalid
      onFX(() -> {
        List<TextField> nameFields = controller.personalFormContainer.lookupAll(".form-input")
            .stream()
            .filter(n -> n instanceof TextField)
            .map(n -> (TextField) n)
            .toList();
        nameFields.get(0).setText("");
        return null;
      });

      runOnFX(controller::handleSubmit);

      // Wait briefly to ensure any async task would have completed
      Thread.sleep(200);

      onFX(() -> {
        PhysicalPerson unchanged = fixture.personRepo().findById(personId).orElseThrow();
        assertEquals("João Silva", unchanged.getName());
        return null;
      });
    }
  }
}
