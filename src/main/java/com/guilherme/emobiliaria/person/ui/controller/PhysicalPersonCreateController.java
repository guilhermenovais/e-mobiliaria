package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.output.CreateAddressOutput;
import com.guilherme.emobiliaria.person.application.output.CreatePhysicalPersonOutput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreatePhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCpfInteractor;
import com.guilherme.emobiliaria.person.ui.component.AddressFormPane;
import com.guilherme.emobiliaria.person.ui.component.PhysicalPersonFormPane;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class PhysicalPersonCreateController {

  private static final Logger log = LoggerFactory.getLogger(PhysicalPersonCreateController.class);

  // ── Injected use cases ─────────────────────────────────────────────────────

  private final CreatePhysicalPersonInteractor createPhysicalPerson;
  private final CreateAddressInteractor createAddress;
  private final SearchAddressByCepInteractor searchByCep;
  private final ValidateCpfInteractor validateCpf;
  private final NavigationService navigationService;
  private final GuiceFxmlLoader fxmlLoader;

  private static final String CREATE_FXML =
      "/com/guilherme/emobiliaria/person/ui/view/physical-person-create-view.fxml";

  @Inject
  public PhysicalPersonCreateController(
      CreatePhysicalPersonInteractor createPhysicalPerson,
      CreateAddressInteractor createAddress,
      SearchAddressByCepInteractor searchByCep,
      ValidateCpfInteractor validateCpf,
      NavigationService navigationService,
      GuiceFxmlLoader fxmlLoader) {
    this.createPhysicalPerson = createPhysicalPerson;
    this.createAddress = createAddress;
    this.searchByCep = searchByCep;
    this.validateCpf = validateCpf;
    this.navigationService = navigationService;
    this.fxmlLoader = fxmlLoader;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML Label titleLabel;
  @FXML Label subtitleLabel;
  @FXML Label personalSectionLabel;
  @FXML Label addressSectionLabel;
  @FXML StackPane personalFormContainer;
  @FXML StackPane addressFormContainer;
  @FXML Button cancelButton;
  @FXML Button saveButton;

  // ── State ──────────────────────────────────────────────────────────────────

  private ResourceBundle bundle;
  private PhysicalPersonFormPane physicalForm;
  private AddressFormPane addressForm;

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    titleLabel.setText(bundle.getString("physical_person.create.title"));
    subtitleLabel.setText(bundle.getString("physical_person.create.subtitle"));
    personalSectionLabel.setText(bundle.getString("physical_person.create.section.personal"));
    addressSectionLabel.setText(bundle.getString("physical_person.create.section.address"));

    physicalForm = new PhysicalPersonFormPane(bundle, validateCpf);
    addressForm = new AddressFormPane(searchByCep, bundle);

    addressForm.setDisableNextCallback(
        () -> saveButton.setDisable(true),
        () -> saveButton.setDisable(false));

    personalFormContainer.getChildren().add(physicalForm);
    addressFormContainer.getChildren().add(addressForm);

    cancelButton.setText(bundle.getString("physical_person.create.button.cancel"));
    saveButton.setText(bundle.getString("physical_person.create.button.save"));

    cancelButton.setOnAction(e -> navigationService.goBack());
    saveButton.setOnAction(e -> handleSubmit());
  }

  // ── Submission ─────────────────────────────────────────────────────────────

  void handleSubmit() {
    physicalForm.clearErrors();

    boolean formValid = physicalForm.validate() & addressForm.validate();
    if (!formValid) {
      return;
    }

    saveButton.setDisable(true);

    Task<CreateAddressOutput> addressTask = new Task<>() {
      @Override
      protected CreateAddressOutput call() {
        return createAddress.execute(addressForm.buildInput());
      }
    };

    addressTask.setOnSucceeded(e -> {
      long addressId = addressTask.getValue().address().getId();

      Task<CreatePhysicalPersonOutput> personTask = new Task<>() {
        @Override
        protected CreatePhysicalPersonOutput call() {
          return createPhysicalPerson.execute(physicalForm.buildInput(addressId));
        }
      };

      personTask.setOnSucceeded(pe -> Platform.runLater(() -> navigationService.goBack()));
      personTask.setOnFailed(pe -> handleError(personTask.getException()));
      new Thread(personTask).start();
    });

    addressTask.setOnFailed(e -> handleError(addressTask.getException()));
    new Thread(addressTask).start();
  }

  /**
   * Builds and returns the view node for navigation. Loads the FXML view.
   * This method is used by PhysicalPersonListController via Provider.
   */
  public Node buildView() {
    URL resource = getClass().getResource(CREATE_FXML);
    if (resource == null) {
      log.error("physical-person-create-view.fxml not found at {}", CREATE_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load physical person create view", e);
      return new StackPane();
    }
  }

  private void handleError(Throwable t) {
    Platform.runLater(() -> saveButton.setDisable(false));
    ErrorHandler.handle(t, bundle);
  }
}
