package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.input.FindJuridicalPersonByIdInput;
import com.guilherme.emobiliaria.person.application.output.CreateAddressOutput;
import com.guilherme.emobiliaria.person.application.output.CreatePhysicalPersonOutput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreateJuridicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreatePhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditJuridicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.EditPhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindJuridicalPersonByIdInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCnpjInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCpfInteractor;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.ui.component.AddressFormPane;
import com.guilherme.emobiliaria.person.ui.component.CompanyDataFormPane;
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

public class JuridicalPersonController {

  private static final Logger log = LoggerFactory.getLogger(JuridicalPersonController.class);

  private static final String VIEW_FXML =
      "/com/guilherme/emobiliaria/person/ui/view/juridical-person-view.fxml";

  private final FindJuridicalPersonByIdInteractor findJuridicalById;
  private final CreateJuridicalPersonInteractor createJuridicalPerson;
  private final EditJuridicalPersonInteractor editJuridicalPerson;
  private final CreatePhysicalPersonInteractor createPhysicalPerson;
  private final EditPhysicalPersonInteractor editPhysicalPerson;
  private final CreateAddressInteractor createAddress;
  private final EditAddressInteractor editAddress;
  private final SearchAddressByCepInteractor searchByCep;
  private final ValidateCnpjInteractor validateCnpj;
  private final ValidateCpfInteractor validateCpf;
  private final NavigationService navigationService;
  private final GuiceFxmlLoader fxmlLoader;

  @Inject
  public JuridicalPersonController(
      FindJuridicalPersonByIdInteractor findJuridicalById,
      CreateJuridicalPersonInteractor createJuridicalPerson,
      EditJuridicalPersonInteractor editJuridicalPerson,
      CreatePhysicalPersonInteractor createPhysicalPerson,
      EditPhysicalPersonInteractor editPhysicalPerson,
      CreateAddressInteractor createAddress,
      EditAddressInteractor editAddress,
      SearchAddressByCepInteractor searchByCep,
      ValidateCnpjInteractor validateCnpj,
      ValidateCpfInteractor validateCpf,
      NavigationService navigationService,
      GuiceFxmlLoader fxmlLoader) {
    this.findJuridicalById = findJuridicalById;
    this.createJuridicalPerson = createJuridicalPerson;
    this.editJuridicalPerson = editJuridicalPerson;
    this.createPhysicalPerson = createPhysicalPerson;
    this.editPhysicalPerson = editPhysicalPerson;
    this.createAddress = createAddress;
    this.editAddress = editAddress;
    this.searchByCep = searchByCep;
    this.validateCnpj = validateCnpj;
    this.validateCpf = validateCpf;
    this.navigationService = navigationService;
    this.fxmlLoader = fxmlLoader;
  }

  @FXML Label titleLabel;
  @FXML Label subtitleLabel;
  @FXML Label companySectionLabel;
  @FXML Label companyAddressSectionLabel;
  @FXML Label representativeSectionLabel;
  @FXML Label representativeAddressSectionLabel;
  @FXML StackPane companyFormContainer;
  @FXML StackPane companyAddressFormContainer;
  @FXML StackPane representativeFormContainer;
  @FXML StackPane representativeAddressFormContainer;
  @FXML Button cancelButton;
  @FXML Button saveButton;

  private ResourceBundle bundle;
  private CompanyDataFormPane companyForm;
  private AddressFormPane companyAddressForm;
  private PhysicalPersonFormPane representativeForm;
  private AddressFormPane representativeAddressForm;

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
    representativeSectionLabel.setText(bundle.getString("juridical_person.form.section.representative"));
    representativeAddressSectionLabel.setText(
        bundle.getString("juridical_person.form.section.representative_address"));

    companyForm = new CompanyDataFormPane(bundle, validateCnpj);
    companyAddressForm = new AddressFormPane(searchByCep, bundle);
    representativeForm = new PhysicalPersonFormPane(bundle, validateCpf);
    representativeAddressForm = new AddressFormPane(searchByCep, bundle);

    companyAddressForm.setDisableNextCallback(
        () -> saveButton.setDisable(true),
        () -> saveButton.setDisable(false));
    representativeAddressForm.setDisableNextCallback(
        () -> saveButton.setDisable(true),
        () -> saveButton.setDisable(false));

    companyFormContainer.getChildren().setAll(companyForm);
    companyAddressFormContainer.getChildren().setAll(companyAddressForm);
    representativeFormContainer.getChildren().setAll(representativeForm);
    representativeAddressFormContainer.getChildren().setAll(representativeAddressForm);

    cancelButton.setText(bundle.getString("juridical_person.form.button.cancel"));
    saveButton.setText(bundle.getString("juridical_person.form.button.save"));

    cancelButton.setOnAction(e -> navigationService.goBack());
    saveButton.setOnAction(e -> handleSubmit());

    if (editMode) {
      loadForEdit();
    }
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
        representativeForm.populate(loadedPerson.getRepresentative());
        representativeAddressForm.populate(loadedPerson.getRepresentative().getAddress());
      });
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  void handleSubmit() {
    boolean valid = companyForm.validate()
        & companyAddressForm.validate()
        & representativeForm.validate()
        & representativeAddressForm.validate();
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
    Task<CreateAddressOutput> companyAddressTask = new Task<>() {
      @Override
      protected CreateAddressOutput call() {
        return createAddress.execute(companyAddressForm.buildInput());
      }
    };

    companyAddressTask.setOnSucceeded(e -> {
      long companyAddressId = companyAddressTask.getValue().address().getId();

      Task<CreateAddressOutput> representativeAddressTask = new Task<>() {
        @Override
        protected CreateAddressOutput call() {
          return createAddress.execute(representativeAddressForm.buildInput());
        }
      };

      representativeAddressTask.setOnSucceeded(re -> {
        long representativeAddressId = representativeAddressTask.getValue().address().getId();

        Task<CreatePhysicalPersonOutput> representativeTask = new Task<>() {
          @Override
          protected CreatePhysicalPersonOutput call() {
            return createPhysicalPerson.execute(representativeForm.buildInput(representativeAddressId));
          }
        };

        representativeTask.setOnSucceeded(rp -> {
          long representativeId = representativeTask.getValue().physicalPerson().getId();

          Task<Void> juridicalTask = new Task<>() {
            @Override
            protected Void call() {
              createJuridicalPerson.execute(new com.guilherme.emobiliaria.person.application.input.CreateJuridicalPersonInput(
                  companyForm.getCorporateName(),
                  companyForm.getCnpj(),
                  representativeId,
                  companyAddressId
              ));
              return null;
            }
          };

          juridicalTask.setOnSucceeded(j -> onSuccess());
          juridicalTask.setOnFailed(j -> onFailure(juridicalTask.getException()));
          new Thread(juridicalTask).start();
        });

        representativeTask.setOnFailed(rp -> onFailure(representativeTask.getException()));
        new Thread(representativeTask).start();
      });

      representativeAddressTask.setOnFailed(re -> onFailure(representativeAddressTask.getException()));
      new Thread(representativeAddressTask).start();
    });

    companyAddressTask.setOnFailed(e -> onFailure(companyAddressTask.getException()));
    new Thread(companyAddressTask).start();
  }

  private void editFlow() {
    if (loadedPerson == null) {
      onFailure(new IllegalStateException("Juridical person not loaded for edit"));
      return;
    }

    var companyAddressInput = companyAddressForm.buildEditInput(loadedPerson.getAddress().getId());
    var representativeAddressInput =
        representativeAddressForm.buildEditInput(loadedPerson.getRepresentative().getAddress().getId());
    var representativeInput = representativeForm.buildEditInput(
        loadedPerson.getRepresentative().getId(),
        loadedPerson.getRepresentative().getAddress().getId());

    Task<Void> editCompanyAddressTask = new Task<>() {
      @Override
      protected Void call() {
        editAddress.execute(companyAddressInput);
        return null;
      }
    };

    editCompanyAddressTask.setOnSucceeded(e -> {
      Task<Void> editRepresentativeAddressTask = new Task<>() {
        @Override
        protected Void call() {
          editAddress.execute(representativeAddressInput);
          return null;
        }
      };

      editRepresentativeAddressTask.setOnSucceeded(re -> {
        Task<Void> editRepresentativeTask = new Task<>() {
          @Override
          protected Void call() {
            editPhysicalPerson.execute(representativeInput);
            return null;
          }
        };

        editRepresentativeTask.setOnSucceeded(rp -> {
          Task<Void> editJuridicalTask = new Task<>() {
            @Override
            protected Void call() {
              editJuridicalPerson.execute(new com.guilherme.emobiliaria.person.application.input.EditJuridicalPersonInput(
                  loadedPerson.getId(),
                  companyForm.getCorporateName(),
                  companyForm.getCnpj(),
                  loadedPerson.getRepresentative().getId(),
                  loadedPerson.getAddress().getId()
              ));
              return null;
            }
          };

          editJuridicalTask.setOnSucceeded(j -> onSuccess());
          editJuridicalTask.setOnFailed(j -> onFailure(editJuridicalTask.getException()));
          new Thread(editJuridicalTask).start();
        });

        editRepresentativeTask.setOnFailed(rp -> onFailure(editRepresentativeTask.getException()));
        new Thread(editRepresentativeTask).start();
      });

      editRepresentativeAddressTask.setOnFailed(re -> onFailure(editRepresentativeAddressTask.getException()));
      new Thread(editRepresentativeAddressTask).start();
    });

    editCompanyAddressTask.setOnFailed(e -> onFailure(editCompanyAddressTask.getException()));
    new Thread(editCompanyAddressTask).start();
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
