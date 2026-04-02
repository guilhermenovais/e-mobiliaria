package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.usecase.EditAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditPhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindPhysicalPersonByIdInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCpfInteractor;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.ui.component.AddressFormPane;
import com.guilherme.emobiliaria.person.ui.component.PhysicalPersonFormPane;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.util.Locale;
import java.util.ResourceBundle;

public class PhysicalPersonEditController {

  // ── Injected dependencies ──────────────────────────────────────────────────

  private final FindPhysicalPersonByIdInteractor findById;
  private final EditPhysicalPersonInteractor editPhysicalPerson;
  private final EditAddressInteractor editAddress;
  private final SearchAddressByCepInteractor searchByCep;
  private final ValidateCpfInteractor validateCpf;
  private final NavigationService navigationService;

  @Inject
  public PhysicalPersonEditController(
      FindPhysicalPersonByIdInteractor findById,
      EditPhysicalPersonInteractor editPhysicalPerson,
      EditAddressInteractor editAddress,
      SearchAddressByCepInteractor searchByCep,
      ValidateCpfInteractor validateCpf,
      NavigationService navigationService) {
    this.findById = findById;
    this.editPhysicalPerson = editPhysicalPerson;
    this.editAddress = editAddress;
    this.searchByCep = searchByCep;
    this.validateCpf = validateCpf;
    this.navigationService = navigationService;
  }

  // ── State ──────────────────────────────────────────────────────────────────

  private long personId;
  private ResourceBundle bundle;
  private PhysicalPerson loadedPerson;

  // ── UI components (package-private for tests) ──────────────────────────────

  StackPane personalFormContainer;
  StackPane addressFormContainer;
  Button cancelButton;
  Button saveButton;

  private PhysicalPersonFormPane physicalForm;
  private AddressFormPane addressForm;
  private VBox root;

  // ── Setters ────────────────────────────────────────────────────────────────

  public void setPersonId(long personId) {
    this.personId = personId;
  }

  // ── Initialization ─────────────────────────────────────────────────────────

  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    physicalForm = new PhysicalPersonFormPane(bundle, validateCpf);
    addressForm = new AddressFormPane(searchByCep, bundle);

    personalFormContainer = new StackPane(physicalForm);
    addressFormContainer = new StackPane(addressForm);

    cancelButton = new Button(bundle.getString("physical_person.edit.button.cancel"));
    cancelButton.getStyleClass().add("btn-secondary");
    cancelButton.setOnAction(e -> navigationService.goBack());

    saveButton = new Button(bundle.getString("physical_person.edit.button.save"));
    saveButton.getStyleClass().add("btn-primary");
    saveButton.setOnAction(e -> handleSubmit());

    addressForm.setDisableNextCallback(
        () -> saveButton.setDisable(true),
        () -> saveButton.setDisable(false));

    root = buildLayout();

    if (personId != 0) {
      loadPersonData();
    }
  }

  public Node buildView() {
    initialize();
    return root;
  }

  // ── Load person ────────────────────────────────────────────────────────────

  private void loadPersonData() {
    Task<PhysicalPerson> task = new Task<>() {
      @Override
      protected PhysicalPerson call() {
        return findById.execute(
            new com.guilherme.emobiliaria.person.application.input.FindPhysicalPersonByIdInput(
                personId)).physicalPerson();
      }
    };

    task.setOnSucceeded(e -> {
      loadedPerson = task.getValue();
      Platform.runLater(() -> {
        physicalForm.populate(loadedPerson);
        addressForm.populate(loadedPerson.getAddress());
      });
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));

    new Thread(task).start();
  }

  // ── Submit ─────────────────────────────────────────────────────────────────

  public void handleSubmit() {
    if (!physicalForm.validate() || !addressForm.validate()) {
      return;
    }
    if (loadedPerson == null) {
      return;
    }

    saveButton.setDisable(true);

    // Read form inputs on the FX thread before handing off to background tasks
    var addressInput = addressForm.buildEditInput(loadedPerson.getAddress().getId());
    var personInput = physicalForm.buildEditInput(personId, loadedPerson.getAddress().getId());

    Task<Void> addressTask = new Task<>() {
      @Override
      protected Void call() {
        editAddress.execute(addressInput);
        return null;
      }
    };

    addressTask.setOnSucceeded(e -> {
      Task<Void> personTask = new Task<>() {
        @Override
        protected Void call() {
          editPhysicalPerson.execute(personInput);
          return null;
        }
      };

      personTask.setOnSucceeded(pe -> {
        saveButton.setDisable(false);
        Platform.runLater(navigationService::goBack);
      });
      personTask.setOnFailed(pe -> handleError(personTask.getException()));
      new Thread(personTask).start();
    });

    addressTask.setOnFailed(e -> handleError(addressTask.getException()));
    new Thread(addressTask).start();
  }

  // ── Error handling ─────────────────────────────────────────────────────────

  private void handleError(Throwable t) {
    Platform.runLater(() -> saveButton.setDisable(false));
    ErrorHandler.handle(t, bundle);
  }

  // ── Layout ─────────────────────────────────────────────────────────────────

  private VBox buildLayout() {
    Label title = new Label(bundle.getString("physical_person.edit.title"));
    title.getStyleClass().add("form-section-label");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox header = new HBox(title, spacer, cancelButton, saveButton);
    header.setSpacing(8);
    header.setPadding(new Insets(16));

    VBox content = new VBox(16, header, personalFormContainer, addressFormContainer);
    content.setPadding(new Insets(16));
    VBox.setVgrow(content, Priority.ALWAYS);

    VBox layout = new VBox(content);
    layout.getStyleClass().add("list-view-root");
    return layout;
  }
}
