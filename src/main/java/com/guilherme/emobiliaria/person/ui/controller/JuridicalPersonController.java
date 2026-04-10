package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.input.EditJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.FindAllPhysicalPeopleInput;
import com.guilherme.emobiliaria.person.application.input.FindJuridicalPersonByIdInput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreateJuridicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditJuridicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindAllPhysicalPeopleInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindJuridicalPersonByIdInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCnpjInteractor;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.ui.component.AddressFormPane;
import com.guilherme.emobiliaria.person.ui.component.CompanyDataFormPane;
import com.guilherme.emobiliaria.person.ui.component.JuridicalPersonRepresentativesPane;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
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
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class JuridicalPersonController {

  private static final Logger log = LoggerFactory.getLogger(JuridicalPersonController.class);

  private static final String VIEW_FXML =
      "/com/guilherme/emobiliaria/person/ui/view/juridical-person-view.fxml";

  private final FindJuridicalPersonByIdInteractor findJuridicalById;
  private final CreateJuridicalPersonInteractor createJuridicalPerson;
  private final EditJuridicalPersonInteractor editJuridicalPerson;
  private final FindAllPhysicalPeopleInteractor findAllPhysicalPeople;
  private final CreateAddressInteractor createAddress;
  private final EditAddressInteractor editAddress;
  private final SearchAddressByCepInteractor searchByCep;
  private final ValidateCnpjInteractor validateCnpj;
  private final NavigationService navigationService;
  private final GuiceFxmlLoader fxmlLoader;

  @Inject
  public JuridicalPersonController(
      FindJuridicalPersonByIdInteractor findJuridicalById,
      CreateJuridicalPersonInteractor createJuridicalPerson,
      EditJuridicalPersonInteractor editJuridicalPerson,
      FindAllPhysicalPeopleInteractor findAllPhysicalPeople,
      CreateAddressInteractor createAddress,
      EditAddressInteractor editAddress,
      SearchAddressByCepInteractor searchByCep,
      ValidateCnpjInteractor validateCnpj,
      NavigationService navigationService,
      GuiceFxmlLoader fxmlLoader) {
    this.findJuridicalById = findJuridicalById;
    this.createJuridicalPerson = createJuridicalPerson;
    this.editJuridicalPerson = editJuridicalPerson;
    this.findAllPhysicalPeople = findAllPhysicalPeople;
    this.createAddress = createAddress;
    this.editAddress = editAddress;
    this.searchByCep = searchByCep;
    this.validateCnpj = validateCnpj;
    this.navigationService = navigationService;
    this.fxmlLoader = fxmlLoader;
  }

  @FXML Label titleLabel;
  @FXML Label subtitleLabel;
  @FXML Label companySectionLabel;
  @FXML Label companyAddressSectionLabel;
  @FXML Label representativesSectionLabel;
  @FXML StackPane companyFormContainer;
  @FXML StackPane companyAddressFormContainer;
  @FXML StackPane representativesContainer;
  @FXML Button cancelButton;
  @FXML Button saveButton;

  private ResourceBundle bundle;
  private CompanyDataFormPane companyForm;
  private AddressFormPane companyAddressForm;
  JuridicalPersonRepresentativesPane representativesPane;

  private Long juridicalPersonId;
  private JuridicalPerson loadedPerson;

  public void setJuridicalPersonId(Long juridicalPersonId) {
    this.juridicalPersonId = juridicalPersonId;
  }

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    boolean editMode = juridicalPersonId != null;
    titleLabel.setText(bundle.getString(editMode
        ? "juridical_person.form.title.edit"
        : "juridical_person.form.title.create"));
    subtitleLabel.setText(bundle.getString(editMode
        ? "juridical_person.form.subtitle.edit"
        : "juridical_person.form.subtitle.create"));
    companySectionLabel.setText(bundle.getString("juridical_person.form.section.company"));
    companyAddressSectionLabel.setText(bundle.getString("juridical_person.form.section.company_address"));
    representativesSectionLabel.setText(bundle.getString("juridical_person.form.section.representatives"));

    companyForm = new CompanyDataFormPane(bundle, validateCnpj);
    companyAddressForm = new AddressFormPane(searchByCep, bundle);
    representativesPane = new JuridicalPersonRepresentativesPane(bundle);

    companyAddressForm.setDisableNextCallback(
        () -> saveButton.setDisable(true),
        () -> saveButton.setDisable(false));

    companyFormContainer.getChildren().setAll(companyForm);
    companyAddressFormContainer.getChildren().setAll(companyAddressForm);
    representativesContainer.getChildren().setAll(representativesPane);

    cancelButton.setText(bundle.getString("juridical_person.form.button.cancel"));
    saveButton.setText(bundle.getString("juridical_person.form.button.save"));

    cancelButton.setOnAction(e -> navigationService.goBack());
    saveButton.setOnAction(e -> handleSubmit());

    loadAllPhysicalPersons(editMode);
  }

  public Node buildView() {
    URL resource = getClass().getResource(VIEW_FXML);
    if (resource == null) {
      log.error("juridical-person-view.fxml not found at {}", VIEW_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load juridical person view", e);
      return new StackPane();
    }
  }

  private void loadAllPhysicalPersons(boolean thenLoadForEdit) {
    Task<List<PhysicalPerson>> task = new Task<>() {
      @Override
      protected List<PhysicalPerson> call() {
        return findAllPhysicalPeople.execute(
            new FindAllPhysicalPeopleInput(new PaginationInput(null, null))).result().items();
      }
    };

    task.setOnSucceeded(e -> {
      List<PhysicalPerson> all = task.getValue();
      Platform.runLater(() -> representativesPane.setAllPersons(all));
      if (thenLoadForEdit) {
        loadForEdit();
      }
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  private void loadForEdit() {
    if (juridicalPersonId == null) {
      return;
    }

    Task<JuridicalPerson> task = new Task<>() {
      @Override
      protected JuridicalPerson call() {
        return findJuridicalById.execute(
            new FindJuridicalPersonByIdInput(juridicalPersonId)).juridicalPerson();
      }
    };

    task.setOnSucceeded(e -> {
      loadedPerson = task.getValue();
      Platform.runLater(() -> {
        companyForm.setCorporateName(loadedPerson.getCorporateName());
        companyForm.setCnpj(loadedPerson.getCnpj());
        companyAddressForm.populate(loadedPerson.getAddress());
        representativesPane.populate(loadedPerson.getRepresentatives());
      });
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  void handleSubmit() {
    boolean valid = companyForm.validate()
        & companyAddressForm.validate()
        & representativesPane.validate();
    if (!valid) {
      return;
    }

    saveButton.setDisable(true);
    if (juridicalPersonId == null) {
      createFlow();
    } else {
      editFlow();
    }
  }

  private void createFlow() {
    var companyAddressInput = companyAddressForm.buildInput();
    String corporateName = companyForm.getCorporateName();
    String cnpj = companyForm.getCnpj();
    List<Long> representativeIds = representativesPane.getSelectedRepresentatives()
        .stream().map(PhysicalPerson::getId).toList();

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        long companyAddressId = createAddress.execute(companyAddressInput).address().getId();
        createJuridicalPerson.execute(new com.guilherme.emobiliaria.person.application.input.CreateJuridicalPersonInput(
            corporateName,
            cnpj,
            representativeIds,
            companyAddressId
        ));
        return null;
      }
    };

    task.setOnSucceeded(e -> onSuccess());
    task.setOnFailed(e -> onFailure(task.getException()));
    new Thread(task).start();
  }

  private void editFlow() {
    if (loadedPerson == null) {
      onFailure(new IllegalStateException("Juridical person not loaded for edit"));
      return;
    }

    var companyAddressInput = companyAddressForm.buildEditInput(loadedPerson.getAddress().getId());
    String corporateName = companyForm.getCorporateName();
    String cnpj = companyForm.getCnpj();
    List<Long> representativeIds = representativesPane.getSelectedRepresentatives()
        .stream().map(PhysicalPerson::getId).toList();

    Task<Void> editTask = new Task<>() {
      @Override
      protected Void call() {
        editAddress.execute(companyAddressInput);
        editJuridicalPerson.execute(new EditJuridicalPersonInput(
            loadedPerson.getId(),
            corporateName,
            cnpj,
            representativeIds,
            loadedPerson.getAddress().getId()
        ));
        return null;
      }
    };

    editTask.setOnSucceeded(e -> onSuccess());
    editTask.setOnFailed(e -> onFailure(editTask.getException()));
    new Thread(editTask).start();
  }

  private void onSuccess() {
    Platform.runLater(() -> {
      saveButton.setDisable(false);
      navigationService.goBack();
    });
  }

  private void onFailure(Throwable t) {
    Platform.runLater(() -> saveButton.setDisable(false));
    ErrorHandler.handle(t, bundle);
  }
}
