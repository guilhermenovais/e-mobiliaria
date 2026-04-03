package com.guilherme.emobiliaria.property.ui.controller;

import com.guilherme.emobiliaria.person.application.output.CreateAddressOutput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.ui.component.AddressFormPane;
import com.guilherme.emobiliaria.property.application.output.CreatePropertyOutput;
import com.guilherme.emobiliaria.property.application.usecase.CreatePropertyInteractor;
import com.guilherme.emobiliaria.property.ui.component.PropertyFormPane;
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

public class PropertyCreateController {

  private static final Logger log = LoggerFactory.getLogger(PropertyCreateController.class);

  // ── Injected use cases ─────────────────────────────────────────────────────

  private final CreatePropertyInteractor createProperty;
  private final CreateAddressInteractor createAddress;
  private final SearchAddressByCepInteractor searchByCep;
  private final NavigationService navigationService;
  private final GuiceFxmlLoader fxmlLoader;

  private static final String CREATE_FXML =
      "/com/guilherme/emobiliaria/property/ui/view/property-create-view.fxml";

  @Inject
  public PropertyCreateController(
      CreatePropertyInteractor createProperty,
      CreateAddressInteractor createAddress,
      SearchAddressByCepInteractor searchByCep,
      NavigationService navigationService,
      GuiceFxmlLoader fxmlLoader) {
    this.createProperty = createProperty;
    this.createAddress = createAddress;
    this.searchByCep = searchByCep;
    this.navigationService = navigationService;
    this.fxmlLoader = fxmlLoader;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML Label titleLabel;
  @FXML Label subtitleLabel;
  @FXML Label propertySectionLabel;
  @FXML Label addressSectionLabel;
  @FXML StackPane propertyFormContainer;
  @FXML StackPane addressFormContainer;
  @FXML Button cancelButton;
  @FXML Button saveButton;

  // ── State ──────────────────────────────────────────────────────────────────

  private ResourceBundle bundle;
  private PropertyFormPane propertyForm;
  private AddressFormPane addressForm;

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    titleLabel.setText(bundle.getString("property.form.title.create"));
    subtitleLabel.setText(bundle.getString("property.form.subtitle.create"));
    propertySectionLabel.setText(bundle.getString("property.form.section.property"));
    addressSectionLabel.setText(bundle.getString("property.form.section.address"));

    propertyForm = new PropertyFormPane(bundle);
    addressForm = new AddressFormPane(searchByCep, bundle);

    addressForm.setDisableNextCallback(
        () -> saveButton.setDisable(true),
        () -> saveButton.setDisable(false));

    propertyFormContainer.getChildren().add(propertyForm);
    addressFormContainer.getChildren().add(addressForm);

    cancelButton.setText(bundle.getString("property.form.button.cancel"));
    saveButton.setText(bundle.getString("property.form.button.save"));

    cancelButton.setOnAction(e -> navigationService.goBack());
    saveButton.setOnAction(e -> handleSubmit());
  }

  // ── Submission ─────────────────────────────────────────────────────────────

  void handleSubmit() {
    propertyForm.clearErrors();

    boolean formValid = propertyForm.validate() & addressForm.validate();
    if (!formValid) return;

    saveButton.setDisable(true);

    Task<CreateAddressOutput> addressTask = new Task<>() {
      @Override
      protected CreateAddressOutput call() {
        return createAddress.execute(addressForm.buildInput());
      }
    };

    addressTask.setOnSucceeded(e -> {
      long addressId = addressTask.getValue().address().getId();

      Task<CreatePropertyOutput> propertyTask = new Task<>() {
        @Override
        protected CreatePropertyOutput call() {
          return createProperty.execute(propertyForm.buildInput(addressId));
        }
      };

      propertyTask.setOnSucceeded(pe -> Platform.runLater(() -> navigationService.goBack()));
      propertyTask.setOnFailed(pe -> handleError(propertyTask.getException()));
      new Thread(propertyTask).start();
    });

    addressTask.setOnFailed(e -> handleError(addressTask.getException()));
    new Thread(addressTask).start();
  }

  /**
   * Builds and returns the view node for navigation.
   */
  public Node buildView() {
    URL resource = getClass().getResource(CREATE_FXML);
    if (resource == null) {
      log.error("property-create-view.fxml not found at {}", CREATE_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load property create view", e);
      return new StackPane();
    }
  }

  private void handleError(Throwable t) {
    Platform.runLater(() -> saveButton.setDisable(false));
    ErrorHandler.handle(t, bundle);
  }
}
