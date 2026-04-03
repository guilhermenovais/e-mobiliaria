package com.guilherme.emobiliaria.property.ui.controller;

import com.guilherme.emobiliaria.person.application.usecase.EditAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.ui.component.AddressFormPane;
import com.guilherme.emobiliaria.property.application.input.FindPropertyByIdInput;
import com.guilherme.emobiliaria.property.application.usecase.EditPropertyInteractor;
import com.guilherme.emobiliaria.property.application.usecase.FindPropertyByIdInteractor;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.ui.component.PropertyFormPane;
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

public class PropertyEditController {

  // ── Injected dependencies ──────────────────────────────────────────────────

  private final FindPropertyByIdInteractor findById;
  private final EditPropertyInteractor editProperty;
  private final EditAddressInteractor editAddress;
  private final SearchAddressByCepInteractor searchByCep;
  private final NavigationService navigationService;

  @Inject
  public PropertyEditController(
      FindPropertyByIdInteractor findById,
      EditPropertyInteractor editProperty,
      EditAddressInteractor editAddress,
      SearchAddressByCepInteractor searchByCep,
      NavigationService navigationService) {
    this.findById = findById;
    this.editProperty = editProperty;
    this.editAddress = editAddress;
    this.searchByCep = searchByCep;
    this.navigationService = navigationService;
  }

  // ── State ──────────────────────────────────────────────────────────────────

  private long propertyId;
  private ResourceBundle bundle;
  private Property loadedProperty;

  // ── UI components (package-private for tests) ──────────────────────────────

  StackPane propertyFormContainer;
  StackPane addressFormContainer;
  Button cancelButton;
  Button saveButton;

  private PropertyFormPane propertyForm;
  private AddressFormPane addressForm;
  private VBox root;

  // ── Setters ────────────────────────────────────────────────────────────────

  public void setPropertyId(long propertyId) {
    this.propertyId = propertyId;
  }

  // ── Initialization ─────────────────────────────────────────────────────────

  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    propertyForm = new PropertyFormPane(bundle);
    addressForm = new AddressFormPane(searchByCep, bundle);

    propertyFormContainer = new StackPane(propertyForm);
    addressFormContainer = new StackPane(addressForm);

    cancelButton = new Button(bundle.getString("property.form.button.cancel"));
    cancelButton.getStyleClass().add("btn-secondary");
    cancelButton.setOnAction(e -> navigationService.goBack());

    saveButton = new Button(bundle.getString("property.form.button.save"));
    saveButton.getStyleClass().add("btn-primary");
    saveButton.setOnAction(e -> handleSubmit());

    addressForm.setDisableNextCallback(
        () -> saveButton.setDisable(true),
        () -> saveButton.setDisable(false));

    root = buildLayout();

    if (propertyId != 0) {
      loadPropertyData();
    }
  }

  public Node buildView() {
    initialize();
    return root;
  }

  // ── Load property ──────────────────────────────────────────────────────────

  private void loadPropertyData() {
    Task<Property> task = new Task<>() {
      @Override
      protected Property call() {
        return findById.execute(new FindPropertyByIdInput(propertyId)).property();
      }
    };

    task.setOnSucceeded(e -> {
      loadedProperty = task.getValue();
      Platform.runLater(() -> {
        propertyForm.populate(loadedProperty);
        addressForm.populate(loadedProperty.getAddress());
      });
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));

    new Thread(task).start();
  }

  // ── Submit ─────────────────────────────────────────────────────────────────

  public void handleSubmit() {
    if (!propertyForm.validate() || !addressForm.validate()) return;
    if (loadedProperty == null) return;

    saveButton.setDisable(true);

    var addressInput = addressForm.buildEditInput(loadedProperty.getAddress().getId());
    var propertyInput = propertyForm.buildEditInput(propertyId, loadedProperty.getAddress().getId());

    Task<Void> addressTask = new Task<>() {
      @Override
      protected Void call() {
        editAddress.execute(addressInput);
        return null;
      }
    };

    addressTask.setOnSucceeded(e -> {
      Task<Void> propertyTask = new Task<>() {
        @Override
        protected Void call() {
          editProperty.execute(propertyInput);
          return null;
        }
      };

      propertyTask.setOnSucceeded(pe -> {
        saveButton.setDisable(false);
        Platform.runLater(navigationService::goBack);
      });
      propertyTask.setOnFailed(pe -> handleError(propertyTask.getException()));
      new Thread(propertyTask).start();
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
    Label title = new Label(bundle.getString("property.form.title.edit"));
    title.getStyleClass().add("form-section-label");

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    HBox header = new HBox(title, spacer, cancelButton, saveButton);
    header.setSpacing(8);
    header.setPadding(new Insets(16));

    VBox content = new VBox(16, header, propertyFormContainer, addressFormContainer);
    content.setPadding(new Insets(16));
    VBox.setVgrow(content, Priority.ALWAYS);

    VBox layout = new VBox(content);
    layout.getStyleClass().add("list-view-root");
    return layout;
  }
}
