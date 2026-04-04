package com.guilherme.emobiliaria.config.ui.controller;

import com.guilherme.emobiliaria.config.application.input.SetConfigInput;
import com.guilherme.emobiliaria.config.application.usecase.SetConfigInteractor;
import com.guilherme.emobiliaria.config.ui.component.PersonType;
import com.guilherme.emobiliaria.config.ui.component.PersonTypeSelectionPane;
import com.guilherme.emobiliaria.person.application.input.CreateJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.CreateAddressOutput;
import com.guilherme.emobiliaria.person.application.output.CreateJuridicalPersonOutput;
import com.guilherme.emobiliaria.person.application.output.CreatePhysicalPersonOutput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreateJuridicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreatePhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCnpjInteractor;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCpfInteractor;
import com.guilherme.emobiliaria.person.ui.component.AddressFormPane;
import com.guilherme.emobiliaria.person.ui.component.CompanyDataFormPane;
import com.guilherme.emobiliaria.person.ui.component.PhysicalPersonFormPane;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.component.WizardStepperBar;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class InitialSetupController {

  private static final Logger log = LoggerFactory.getLogger(InitialSetupController.class);

  // ── Injected use cases ─────────────────────────────────────────────────────

  private final CreateAddressInteractor createAddress;
  private final SearchAddressByCepInteractor searchByCep;
  private final CreatePhysicalPersonInteractor createPhysicalPerson;
  private final CreateJuridicalPersonInteractor createJuridicalPerson;
  private final SetConfigInteractor setConfig;
  private final ValidateCpfInteractor validateCpf;
  private final ValidateCnpjInteractor validateCnpj;
  private final GuiceFxmlLoader fxmlLoader;

  @Inject
  public InitialSetupController(
      CreateAddressInteractor createAddress,
      SearchAddressByCepInteractor searchByCep,
      CreatePhysicalPersonInteractor createPhysicalPerson,
      CreateJuridicalPersonInteractor createJuridicalPerson, SetConfigInteractor setConfig,
      ValidateCpfInteractor validateCpf, ValidateCnpjInteractor validateCnpj,
      GuiceFxmlLoader fxmlLoader) {
    this.createAddress = createAddress;
    this.searchByCep = searchByCep;
    this.createPhysicalPerson = createPhysicalPerson;
    this.createJuridicalPerson = createJuridicalPerson;
    this.setConfig = setConfig;
    this.validateCpf = validateCpf;
    this.validateCnpj = validateCnpj;
    this.fxmlLoader = fxmlLoader;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML private Label stepTitleLabel;
  @FXML private Label stepSubtitleLabel;
  @FXML private HBox stepperContainer;
  @FXML private StackPane contentPane;
  @FXML private Button backButton;
  @FXML private Button nextButton;

  // ── Wizard state ───────────────────────────────────────────────────────────

  private int currentStep = 1;
  private PersonType selectedType = null;
  private ResourceBundle bundle;

  // ── UI components ──────────────────────────────────────────────────────────

  private static final String[][] STEP_LABELS_PHYSICAL =
      {{"stepper.label.tipo"}, {"stepper.label.dados"}, {"stepper.label.endereco"}};
  private static final String[][] STEP_LABELS_JURIDICAL =
      {{"stepper.label.tipo"}, {"stepper.label.empresa"}, {"stepper.label.end_empresa"},
          {"stepper.label.representante"}, {"stepper.label.end_repres"}};
  private final AddressFormPane[] addressPane = new AddressFormPane[2];
  private PersonTypeSelectionPane typeSelectionPane;
  private PhysicalPersonFormPane physicalPersonForm;

  // ── Step label keys ────────────────────────────────────────────────────────
  private CompanyDataFormPane companyDataForm;
  private WizardStepperBar stepperBar;

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    typeSelectionPane = new PersonTypeSelectionPane(bundle);
    physicalPersonForm = new PhysicalPersonFormPane(bundle, validateCpf);
    companyDataForm = new CompanyDataFormPane(bundle, validateCnpj);
    addressPane[0] = new AddressFormPane(searchByCep, bundle);
    addressPane[1] = new AddressFormPane(searchByCep, bundle);

    addressPane[0].setDisableNextCallback(() -> nextButton.setDisable(true),
        () -> nextButton.setDisable(false));
    addressPane[1].setDisableNextCallback(() -> nextButton.setDisable(true),
        () -> nextButton.setDisable(false));

    buildStepper();
    showStep(1);
    backButton.setOnAction(e -> handleBack());
    nextButton.setOnAction(e -> handleNext());
  }

  // ── Stepper ────────────────────────────────────────────────────────────────

  private void buildStepper() {
    List<String> labelTexts = getStepperLabelTexts();
    stepperBar = new WizardStepperBar(labelTexts);
    HBox.setHgrow(stepperBar, Priority.ALWAYS);
    stepperContainer.getChildren().setAll(stepperBar);
    stepperBar.setCurrentStep(currentStep);
  }

  private List<String> getStepperLabelTexts() {
    String[][] keys = (selectedType == PersonType.JURIDICAL)
        ? STEP_LABELS_JURIDICAL : STEP_LABELS_PHYSICAL;
    List<String> texts = new ArrayList<>();
    for (String[] key : keys) {
      texts.add(bundle.getString(key[0]));
    }
    return texts;
  }

  // ── Step navigation ────────────────────────────────────────────────────────

  private void showStep(int step) {
    contentPane.getChildren().clear();

    stepTitleLabel.setText(bundle.getString(titleKeyForStep(step)));
    stepSubtitleLabel.setText(bundle.getString(subtitleKeyForStep(step)));

    backButton.setVisible(step > 1);
    backButton.setManaged(step > 1);
    backButton.setText(bundle.getString("setup.button.back"));

    boolean isLastStep = isLastStep(step);
    nextButton.setText(isLastStep
        ? bundle.getString("setup.button.finish")
        : bundle.getString("setup.button.next"));

    contentPane.getChildren().add(buildStepContent(step));
    stepperBar.setCurrentStep(step);
  }

  private String titleKeyForStep(int step) {
    if (step == 1) return "setup.step.type.title";
    if (selectedType == PersonType.JURIDICAL) {
      return switch (step) {
        case 2 -> "setup.step.company.title";
        case 3 -> "setup.step.company_address.title";
        case 4 -> "setup.step.representative.title";
        case 5 -> "setup.step.representative_address.title";
        default -> "setup.step.type.title";
      };
    }
    return switch (step) {
      case 2 -> "setup.step.personal.title";
      case 3 -> "setup.step.address.title";
      default -> "setup.step.type.title";
    };
  }

  private String subtitleKeyForStep(int step) {
    if (step == 1) return "setup.step.type.subtitle";
    if (selectedType == PersonType.JURIDICAL) {
      return switch (step) {
        case 2 -> "setup.step.company.subtitle";
        case 3 -> "setup.step.company_address.subtitle";
        case 4 -> "setup.step.representative.subtitle";
        case 5 -> "setup.step.representative_address.subtitle";
        default -> "setup.step.type.subtitle";
      };
    }
    return switch (step) {
      case 2 -> "setup.step.personal.subtitle";
      case 3 -> "setup.step.address.subtitle";
      default -> "setup.step.type.subtitle";
    };
  }

  private boolean isLastStep(int step) {
    if (selectedType == PersonType.PHYSICAL) return step == 3;
    if (selectedType == PersonType.JURIDICAL) return step == 5;
    return false;
  }

  private Node buildStepContent(int step) {
    if (step == 1)
      return typeSelectionPane;
    if (selectedType == PersonType.JURIDICAL) {
      return switch (step) {
        case 2 -> companyDataForm;
        case 3 -> addressPane[0];
        case 4 -> physicalPersonForm;
        case 5 -> addressPane[1];
        default -> typeSelectionPane;
      };
    }
    return switch (step) {
      case 2 -> physicalPersonForm;
      case 3 -> addressPane[0];
      default -> typeSelectionPane;
    };
  }

  private void handleNext() {
    if (!validateCurrentStep()) return;

    if (currentStep == 1) {
      selectedType = typeSelectionPane.getSelectedType();
      buildStepper();
    }

    if (isLastStep(currentStep)) {
      handleSubmit();
      return;
    }

    currentStep++;
    showStep(currentStep);
  }

  private void handleBack() {
    if (currentStep <= 1) return;
    currentStep--;
    showStep(currentStep);
  }

  // ── Validation ─────────────────────────────────────────────────────────────

  private boolean validateCurrentStep() {
    if (currentStep == 1)
      return typeSelectionPane.validate();

    if (selectedType == PersonType.JURIDICAL) {
      return switch (currentStep) {
        case 2 -> companyDataForm.validate();
        case 3 -> addressPane[0].validate();
        case 4 -> physicalPersonForm.validate();
        case 5 -> addressPane[1].validate();
        default -> true;
      };
    }
    return switch (currentStep) {
      case 2 -> physicalPersonForm.validate();
      case 3 -> addressPane[0].validate();
      default -> true;
    };
  }

  // ── Submission ─────────────────────────────────────────────────────────────

  private void handleSubmit() {
    nextButton.setDisable(true);
    backButton.setDisable(true);

    if (selectedType == PersonType.PHYSICAL) {
      submitPhysicalFlow();
    } else {
      submitJuridicalFlow();
    }
  }

  private void submitPhysicalFlow() {
    Task<CreateAddressOutput> addressTask = new Task<>() {
      @Override protected CreateAddressOutput call() {
        return createAddress.execute(addressPane[0].buildInput());
      }
    };

    addressTask.setOnSucceeded(e -> {
      long addressId = addressTask.getValue().address().getId();

      Task<CreatePhysicalPersonOutput> personTask = new Task<>() {
        @Override protected CreatePhysicalPersonOutput call() {
          return createPhysicalPerson.execute(physicalPersonForm.buildInput(addressId));
        }
      };

      personTask.setOnSucceeded(pe -> {
        long personId = personTask.getValue().physicalPerson().getId();

        Task<Void> configTask = new Task<>() {
          @Override protected Void call() {
            setConfig.execute(new SetConfigInput(personId, "PHYSICAL"));
            return null;
          }
        };

        configTask.setOnSucceeded(ce -> navigateToMain());
        configTask.setOnFailed(ce -> handleError(configTask.getException()));
        new Thread(configTask).start();
      });

      personTask.setOnFailed(pe -> handleError(personTask.getException()));
      new Thread(personTask).start();
    });

    addressTask.setOnFailed(e -> handleError(addressTask.getException()));
    new Thread(addressTask).start();
  }

  private void submitJuridicalFlow() {
    Task<CreateAddressOutput> companyAddrTask = new Task<>() {
      @Override protected CreateAddressOutput call() {
        return createAddress.execute(addressPane[0].buildInput());
      }
    };

    companyAddrTask.setOnSucceeded(e -> {
      long companyAddressId = companyAddrTask.getValue().address().getId();

      Task<CreateAddressOutput> reprAddrTask = new Task<>() {
        @Override protected CreateAddressOutput call() {
          return createAddress.execute(addressPane[1].buildInput());
        }
      };

      reprAddrTask.setOnSucceeded(e2 -> {
        long reprAddressId = reprAddrTask.getValue().address().getId();

        Task<CreatePhysicalPersonOutput> reprPersonTask = new Task<>() {
          @Override protected CreatePhysicalPersonOutput call() {
            return createPhysicalPerson.execute(physicalPersonForm.buildInput(reprAddressId));
          }
        };

        reprPersonTask.setOnSucceeded(e3 -> {
          long reprPersonId = reprPersonTask.getValue().physicalPerson().getId();

          Task<CreateJuridicalPersonOutput> juridicalTask = new Task<>() {
            @Override protected CreateJuridicalPersonOutput call() {
              return createJuridicalPerson.execute(new CreateJuridicalPersonInput(
                  companyDataForm.getCorporateName(), companyDataForm.getCnpj(),
                  reprPersonId,
                  companyAddressId
              ));
            }
          };

          juridicalTask.setOnSucceeded(e4 -> {
            long juridicalId = juridicalTask.getValue().juridicalPerson().getId();

            Task<Void> configTask = new Task<>() {
              @Override protected Void call() {
                setConfig.execute(new SetConfigInput(juridicalId, "JURIDICAL"));
                return null;
              }
            };

            configTask.setOnSucceeded(e5 -> navigateToMain());
            configTask.setOnFailed(e5 -> handleError(configTask.getException()));
            new Thread(configTask).start();
          });

          juridicalTask.setOnFailed(e4 -> handleError(juridicalTask.getException()));
          new Thread(juridicalTask).start();
        });

        reprPersonTask.setOnFailed(e3 -> handleError(reprPersonTask.getException()));
        new Thread(reprPersonTask).start();
      });

      reprAddrTask.setOnFailed(e2 -> handleError(reprAddrTask.getException()));
      new Thread(reprAddrTask).start();
    });

    companyAddrTask.setOnFailed(e -> handleError(companyAddrTask.getException()));
    new Thread(companyAddrTask).start();
  }

  private void navigateToMain() {
    try {
      Stage stage = (Stage) nextButton.getScene().getWindow();
      Parent mainView = fxmlLoader.load(InitialSetupController.class.getResource(
          "/com/guilherme/emobiliaria/shared/ui/layout/view/main-view.fxml"));
      Scene scene = new Scene(mainView);
      stage.setScene(scene);
      stage.setTitle("e-Mobiliária");
      stage.setResizable(true);
    } catch (IOException e) {
      log.error("Failed to load main view", e);
      handleError(e);
    }
  }

  private void handleError(Throwable t) {
    Platform.runLater(() -> {
      nextButton.setDisable(false);
      backButton.setDisable(false);
    });
    ErrorHandler.handle(t, bundle);
  }
}
