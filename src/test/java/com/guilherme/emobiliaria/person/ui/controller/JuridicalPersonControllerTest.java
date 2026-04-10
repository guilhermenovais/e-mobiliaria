package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreateJuridicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditJuridicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindAllPhysicalPeopleInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindJuridicalPersonByIdInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCnpjInteractor;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.service.CnpjValidationService;
import com.guilherme.emobiliaria.person.domain.service.FakeAddressSearchService;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JuridicalPersonControllerTest {

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
    return future.get(10, TimeUnit.SECONDS);
  }

  private static void runOnFX(Runnable action) throws Exception {
    onFX(() -> {
      action.run();
      return null;
    });
  }

  record Fixture(
      JuridicalPersonController controller,
      FakeJuridicalPersonRepository juridicalRepo,
      FakePhysicalPersonRepository physicalRepo,
      NavigationService navigationService) {}

  private static Fixture buildFixture() throws Exception {
    FakePhysicalPersonRepository physicalRepo = new FakePhysicalPersonRepository();
    FakeJuridicalPersonRepository juridicalRepo = new FakeJuridicalPersonRepository();
    FakeAddressRepository addressRepo = new FakeAddressRepository();

    // Pre-populate a physical person to be used as representative
    Address repAddress = Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo", BrazilianState.SP);
    addressRepo.create(repAddress);
    PhysicalPerson rep = PhysicalPerson.create("João Representante", "Brasileiro", CivilState.SINGLE,
        "Administrador", "529.982.247-25", "MG-1234567", repAddress);
    physicalRepo.create(rep);

    NavigationService navigationService = new NavigationService();

    FakeAddressSearchService addressSearchService = new FakeAddressSearchService();
    AddressSearchResult knownAddress = new AddressSearchResult(BrazilianState.SP, "São Paulo",
        "Sé", "Praça da Sé");
    addressSearchService.register("01001000", knownAddress);

    var controller = new JuridicalPersonController(
        new FindJuridicalPersonByIdInteractor(juridicalRepo),
        new CreateJuridicalPersonInteractor(juridicalRepo, physicalRepo, addressRepo),
        new EditJuridicalPersonInteractor(juridicalRepo, physicalRepo, addressRepo),
        new FindAllPhysicalPeopleInteractor(physicalRepo),
        new CreateAddressInteractor(addressRepo),
        new EditAddressInteractor(addressRepo),
        new SearchAddressByCepInteractor(addressSearchService),
        new ValidateCnpjInteractor(new CnpjValidationService()),
        navigationService,
        null
    );

    runOnFX(() -> {
      StackPane root = new StackPane();
      navigationService.setContentPane(root);
      navigationService.navigate(() -> new Label("list"));

      controller.titleLabel = new Label();
      controller.subtitleLabel = new Label();
      controller.companySectionLabel = new Label();
      controller.companyAddressSectionLabel = new Label();
      controller.representativesSectionLabel = new Label();
      controller.companyFormContainer = new StackPane();
      controller.companyAddressFormContainer = new StackPane();
      controller.representativesContainer = new StackPane();
      controller.cancelButton = new Button();
      controller.saveButton = new Button();
      controller.initialize();
    });

    return new Fixture(controller, juridicalRepo, physicalRepo, navigationService);
  }

  private static void fillAddress(StackPane container, String number) {
    List<TextField> fields =
        container.lookupAll(".form-input").stream()
            .filter(n -> n instanceof TextField)
            .map(n -> (TextField) n)
            .toList();
    fields.get(0).setText("01001-000");
    fields.get(1).setEditable(true);
    fields.get(1).setText("Praça da Sé");
    fields.get(2).setText(number);
    fields.get(3).setText("");
    fields.get(4).setEditable(true);
    fields.get(4).setText("Sé");
    fields.get(5).setEditable(true);
    fields.get(5).setText("São Paulo");

    @SuppressWarnings("unchecked")
    ComboBox<BrazilianState> stateCombo = (ComboBox<BrazilianState>)
        container.lookupAll(".form-combo").stream()
            .filter(n -> n instanceof ComboBox)
            .findFirst()
            .orElseThrow();
    stateCombo.setDisable(false);
    stateCombo.setValue(BrazilianState.SP);
  }

  @Test
  @DisplayName("Should create juridical person and go back when form is valid")
  void shouldCreateJuridicalPersonAndGoBackWhenFormIsValid() throws Exception {
    Fixture fixture = buildFixture();
    JuridicalPersonController controller = fixture.controller();

    // Wait for the background task that loads physical persons to complete
    Thread.sleep(1000);

    runOnFX(() -> {
      List<TextField> companyFields =
          controller.companyFormContainer.lookupAll(".form-input").stream()
              .filter(n -> n instanceof TextField)
              .map(n -> (TextField) n)
              .toList();
      companyFields.get(0).setText("Empresa Exemplo Ltda");
      companyFields.get(1).setText("11.222.333/0001-81");

      fillAddress(controller.companyAddressFormContainer, "100");

      // Select the pre-existing representative from the available list
      controller.representativesPane.setAllPersons(
          fixture.physicalRepo().findAll(new PaginationInput(null, null)).items());
      controller.representativesPane.populate(List.of());
      PhysicalPerson rep = fixture.physicalRepo().findAll(new PaginationInput(null, null)).items().get(0);
      controller.representativesPane.setAllPersons(List.of(rep));
      // Move the rep to selected by populating available and then triggering selection
      controller.representativesPane.populate(List.of(rep));

      controller.handleSubmit();
    });

    Thread.sleep(2000);

    long total = fixture.juridicalRepo()
        .findAll(new PaginationInput(100, 0))
        .total();
    assertEquals(1, total);
    assertFalse(fixture.navigationService().canGoBack());
  }
}
