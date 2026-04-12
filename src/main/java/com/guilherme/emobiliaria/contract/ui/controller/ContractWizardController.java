package com.guilherme.emobiliaria.contract.ui.controller;

import com.guilherme.emobiliaria.config.application.usecase.GetConfigInteractor;
import com.guilherme.emobiliaria.config.domain.entity.Config;
import com.guilherme.emobiliaria.contract.application.input.CreateContractInput;
import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.EditContractInput;
import com.guilherme.emobiliaria.contract.application.input.FindAllPaymentAccountsInput;
import com.guilherme.emobiliaria.contract.application.input.FindContractByIdInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.output.CreateContractOutput;
import com.guilherme.emobiliaria.contract.application.output.CreatePaymentAccountOutput;
import com.guilherme.emobiliaria.contract.application.usecase.CreateContractInteractor;
import com.guilherme.emobiliaria.contract.application.usecase.CreatePaymentAccountInteractor;
import com.guilherme.emobiliaria.contract.application.usecase.EditContractInteractor;
import com.guilherme.emobiliaria.contract.application.usecase.FindAllPaymentAccountsInteractor;
import com.guilherme.emobiliaria.contract.application.usecase.FindContractByIdInteractor;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.ui.component.ContractDetailsStepPane;
import com.guilherme.emobiliaria.contract.ui.component.ContractGuarantorsStepPane;
import com.guilherme.emobiliaria.contract.ui.component.ContractLandlordStepPane;
import com.guilherme.emobiliaria.contract.ui.component.ContractPaymentAccountStepPane;
import com.guilherme.emobiliaria.contract.ui.component.ContractPropertyStepPane;
import com.guilherme.emobiliaria.contract.ui.component.ContractReviewStepPane;
import com.guilherme.emobiliaria.contract.ui.component.ContractTenantsStepPane;
import com.guilherme.emobiliaria.contract.ui.component.ContractWitnessesStepPane;
import com.guilherme.emobiliaria.person.application.input.FindAllJuridicalPeopleInput;
import com.guilherme.emobiliaria.person.application.input.FindAllPhysicalPeopleInput;
import com.guilherme.emobiliaria.person.application.usecase.FindAllJuridicalPeopleInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindAllPhysicalPeopleInteractor;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.application.input.FindAllPropertiesInput;
import com.guilherme.emobiliaria.property.application.usecase.FindAllPropertiesInteractor;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import com.guilherme.emobiliaria.shared.ui.component.WizardStepperBar;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ContractWizardController {

  private static final Logger log = LoggerFactory.getLogger(ContractWizardController.class);

  private static final String WIZARD_FXML =
      "/com/guilherme/emobiliaria/contract/ui/view/contract-wizard-view.fxml";

  private static final int LOAD_ALL_LIMIT = 10_000;
  private static final int TOTAL_STEPS = 8;

  // ── Injected use cases ─────────────────────────────────────────────────────

  private final FindAllPropertiesInteractor findAllProperties;
  private final FindAllPhysicalPeopleInteractor findAllPhysical;
  private final FindAllJuridicalPeopleInteractor findAllJuridical;
  private final FindAllPaymentAccountsInteractor findAllAccounts;
  private final GetConfigInteractor getConfig;
  private final FindContractByIdInteractor findContractById;
  private final CreateContractInteractor createContract;
  private final EditContractInteractor editContract;
  private final CreatePaymentAccountInteractor createPaymentAccount;
  private final NavigationService navigationService;
  private final GuiceFxmlLoader fxmlLoader;

  @Inject
  public ContractWizardController(
      FindAllPropertiesInteractor findAllProperties,
      FindAllPhysicalPeopleInteractor findAllPhysical,
      FindAllJuridicalPeopleInteractor findAllJuridical,
      FindAllPaymentAccountsInteractor findAllAccounts,
      GetConfigInteractor getConfig,
      FindContractByIdInteractor findContractById,
      CreateContractInteractor createContract,
      EditContractInteractor editContract,
      CreatePaymentAccountInteractor createPaymentAccount,
      NavigationService navigationService,
      GuiceFxmlLoader fxmlLoader) {
    this.findAllProperties = findAllProperties;
    this.findAllPhysical = findAllPhysical;
    this.findAllJuridical = findAllJuridical;
    this.findAllAccounts = findAllAccounts;
    this.getConfig = getConfig;
    this.findContractById = findContractById;
    this.createContract = createContract;
    this.editContract = editContract;
    this.createPaymentAccount = createPaymentAccount;
    this.navigationService = navigationService;
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
  private Long contractId = null;
  private ResourceBundle bundle;
  private WizardStepperBar stepperBar;

  // ── Step pane references ───────────────────────────────────────────────────

  private ContractPropertyStepPane propertyPane;
  private ContractLandlordStepPane landlordPane;
  private ContractTenantsStepPane tenantsPane;
  private ContractGuarantorsStepPane guarantorsPane;
  private ContractWitnessesStepPane witnessesPane;
  private ContractDetailsStepPane detailsPane;
  private ContractPaymentAccountStepPane accountPane;
  private ContractReviewStepPane reviewPane;

  // ── Loaded data (cached for review step) ──────────────────────────────────

  private PaymentAccount resolvedAccount;

  // ── Edit mode ──────────────────────────────────────────────────────────────

  public void setContractId(Long contractId) {
    this.contractId = contractId;
  }

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());

    stepTitleLabel.setText(bundle.getString(
        contractId != null ? "contract.wizard.title.edit" : "contract.wizard.title.create"));
    stepSubtitleLabel.setText(bundle.getString("contract.wizard.step.1.subtitle"));

    backButton.setText(bundle.getString("contract.wizard.button.back"));
    nextButton.setText(bundle.getString("contract.wizard.button.next"));
    backButton.setVisible(false);
    backButton.setManaged(false);
    nextButton.setDisable(true);

    buildStepper();

    backButton.setOnAction(e -> handleBack());
    nextButton.setOnAction(e -> handleNext());

    loadAllData();
  }

  // ── Stepper ────────────────────────────────────────────────────────────────

  private void buildStepper() {
    List<String> labels = List.of(
        bundle.getString("contract.wizard.stepper.property"),
        bundle.getString("contract.wizard.stepper.landlord"),
        bundle.getString("contract.wizard.stepper.tenants"),
        bundle.getString("contract.wizard.stepper.guarantors"),
        bundle.getString("contract.wizard.stepper.witnesses"),
        bundle.getString("contract.wizard.stepper.details"),
        bundle.getString("contract.wizard.stepper.account"),
        bundle.getString("contract.wizard.stepper.review")
    );
    stepperBar = new WizardStepperBar(labels);
    HBox.setHgrow(stepperBar, Priority.ALWAYS);
    stepperContainer.getChildren().setAll(stepperBar);
    stepperBar.setCurrentStep(currentStep);
  }

  // ── Data loading ───────────────────────────────────────────────────────────

  private record WizardData(
      List<Property> properties,
      List<Person> allPersons,
      List<PaymentAccount> accounts,
      Config config,
      Contract existingContract
  ) {}

  private void loadAllData() {
    PaginationInput all = new PaginationInput(LOAD_ALL_LIMIT, 0);

    Task<WizardData> task = new Task<>() {
      @Override
      protected WizardData call() {
        List<Property> properties = findAllProperties
            .execute(new FindAllPropertiesInput(all)).result().items();

        List<PhysicalPerson> physical = findAllPhysical
            .execute(new FindAllPhysicalPeopleInput(all)).result().items();
        List<JuridicalPerson> juridical = findAllJuridical
            .execute(new FindAllJuridicalPeopleInput(all)).result().items();
        List<Person> allPersons = new ArrayList<>();
        allPersons.addAll(physical);
        allPersons.addAll(juridical);

        List<PaymentAccount> accounts = findAllAccounts
            .execute(new FindAllPaymentAccountsInput(all)).result().items();

        Config config = getConfig.execute().config();

        Contract existing = null;
        if (contractId != null) {
          existing = findContractById.execute(new FindContractByIdInput(contractId)).contract();
        }

        return new WizardData(properties, allPersons, accounts, config, existing);
      }
    };

    task.setOnSucceeded(e -> {
      WizardData data = task.getValue();
      buildStepPanes(data);
      if (data.existingContract() != null) {
        populateStepPanes(data.existingContract());
      }
      nextButton.setDisable(false);
      showStep(1);
    });

    task.setOnFailed(e -> {
      nextButton.setDisable(false);
      ErrorHandler.handle(task.getException(), bundle);
    });

    new Thread(task).start();
  }

  private void buildStepPanes(WizardData data) {
    propertyPane = new ContractPropertyStepPane(bundle);
    propertyPane.setProperties(data.properties());

    Person defaultLandlord = data.config() != null ? data.config().getDefaultLandlord() : null;
    landlordPane = new ContractLandlordStepPane(bundle, defaultLandlord);
    landlordPane.setAllPersons(data.allPersons());

    tenantsPane = new ContractTenantsStepPane(bundle);
    tenantsPane.setAllPersons(data.allPersons());

    guarantorsPane = new ContractGuarantorsStepPane(bundle);
    guarantorsPane.setAllPersons(data.allPersons());

    witnessesPane = new ContractWitnessesStepPane(bundle);
    witnessesPane.setAllPersons(data.allPersons());

    detailsPane = new ContractDetailsStepPane(bundle);

    accountPane = new ContractPaymentAccountStepPane(bundle);
    accountPane.setAccounts(data.accounts());

    reviewPane = new ContractReviewStepPane(bundle);
  }

  private void populateStepPanes(Contract contract) {
    propertyPane.populate(contract.getProperty());
    landlordPane.populate(contract.getLandlord());
    tenantsPane.populate(contract.getTenants());
    guarantorsPane.populate(contract.getGuarantors());
    witnessesPane.populate(contract.getWitnesses());
    detailsPane.populate(
        contract.getStartDate(),
        contract.getDuration().toTotalMonths() > 0
            ? (int) contract.getDuration().toTotalMonths() : 1,
        contract.getRent(),
        contract.getPaymentDay(),
        contract.getPurpose()
    );
    accountPane.populate(contract.getPaymentAccount());
  }

  // ── Step navigation ────────────────────────────────────────────────────────

  private void showStep(int step) {
    currentStep = step;
    stepTitleLabel.setText(bundle.getString(
        contractId != null ? "contract.wizard.title.edit" : "contract.wizard.title.create"));
    stepSubtitleLabel.setText(bundle.getString("contract.wizard.step." + step + ".subtitle"));

    backButton.setVisible(step > 1);
    backButton.setManaged(step > 1);

    boolean isLast = step == TOTAL_STEPS;
    nextButton.setText(isLast
        ? bundle.getString("contract.wizard.button.finish")
        : bundle.getString("contract.wizard.button.next"));

    contentPane.getChildren().setAll(buildStepContent(step));
    stepperBar.setCurrentStep(step);
  }

  private Node buildStepContent(int step) {
    return switch (step) {
      case 1 -> propertyPane;
      case 2 -> landlordPane;
      case 3 -> tenantsPane;
      case 4 -> guarantorsPane;
      case 5 -> witnessesPane;
      case 6 -> detailsPane;
      case 7 -> accountPane;
      case 8 -> {
        populateReview();
        yield reviewPane;
      }
      default -> propertyPane;
    };
  }

  static PaymentAccount resolveAccountForReview(ContractPaymentAccountStepPane accountPane) {
    if (!accountPane.isNewAccount()) {
      return accountPane.getSelectedAccount();
    }

    String pixKey = accountPane.getNewPixKey();
    return PaymentAccount.create(accountPane.getNewBank(), accountPane.getNewBranch(),
        accountPane.getNewAccountNumber(), pixKey.isBlank() ? null : pixKey);
  }

  private void populateReview() {
    resolvedAccount = resolveAccountForReview(accountPane);
    reviewPane.populate(
        propertyPane.getSelectedProperty(),
        landlordPane.getSelectedLandlord(),
        tenantsPane.getSelectedTenants(),
        guarantorsPane.getSelectedGuarantors(),
        witnessesPane.getSelectedWitnesses(),
        detailsPane.getStartDate(),
        detailsPane.getDurationMonths(),
        detailsPane.getRentCents(),
        detailsPane.getPaymentDay(),
        detailsPane.getPurpose(),
        resolvedAccount
    );
  }

  private void handleNext() {
    if (!validateCurrentStep()) return;
    if (currentStep == TOTAL_STEPS) {
      handleSubmit();
      return;
    }
    showStep(currentStep + 1);
  }

  private void handleBack() {
    if (currentStep <= 1) return;
    showStep(currentStep - 1);
  }

  // ── Validation ─────────────────────────────────────────────────────────────

  private boolean validateCurrentStep() {
    return switch (currentStep) {
      case 1 -> propertyPane.validate();
      case 2 -> landlordPane.validate();
      case 3 -> tenantsPane.validate();
      case 4 -> guarantorsPane.validate();
      case 5 -> witnessesPane.validate();
      case 6 -> detailsPane.validate();
      case 7 -> accountPane.validate();
      default -> true;
    };
  }

  // ── Submission ─────────────────────────────────────────────────────────────

  private void handleSubmit() {
    nextButton.setDisable(true);
    backButton.setDisable(true);

    if (accountPane.isNewAccount()) {
      submitWithNewAccount();
    } else {
      submitWithExistingAccount(accountPane.getSelectedAccount().getId());
    }
  }

  private void submitWithNewAccount() {
    CreatePaymentAccountInput input = new CreatePaymentAccountInput(
        accountPane.getNewBank(),
        accountPane.getNewBranch(),
        accountPane.getNewAccountNumber(),
        accountPane.getNewPixKey().isBlank() ? null : accountPane.getNewPixKey()
    );

    Task<CreatePaymentAccountOutput> task = new Task<>() {
      @Override
      protected CreatePaymentAccountOutput call() {
        return createPaymentAccount.execute(input);
      }
    };

    task.setOnSucceeded(e -> submitWithExistingAccount(task.getValue().paymentAccount().getId()));
    task.setOnFailed(e -> handleError(task.getException()));
    new Thread(task).start();
  }

  private void submitWithExistingAccount(Long accountId) {
    Property property = propertyPane.getSelectedProperty();
    Person landlord = landlordPane.getSelectedLandlord();
    List<Person> tenants = tenantsPane.getSelectedTenants();
    List<Person> guarantors = guarantorsPane.getSelectedGuarantors();
    List<Person> witnesses = witnessesPane.getSelectedWitnesses();

    List<PersonReference> tenantRefs = tenants.stream()
        .map(this::toPersonReference)
        .toList();
    List<PersonReference> guarantorRefs = guarantors.stream()
        .map(this::toPersonReference)
        .toList();
    List<PersonReference> witnessRefs = witnesses.stream()
        .map(this::toPersonReference)
        .toList();
    PersonReference landlordRef = toPersonReference(landlord);

    if (contractId != null) {
      EditContractInput input = new EditContractInput(
          contractId,
          detailsPane.getStartDate(),
          Period.ofMonths(detailsPane.getDurationMonths()),
          detailsPane.getPaymentDay(),
          detailsPane.getRentCents(),
          detailsPane.getPurpose(),
          accountId,
          property.getId(),
          landlordRef,
          tenantRefs,
          guarantorRefs,
          witnessRefs
      );

      Task<Void> task = new Task<>() {
        @Override
        protected Void call() {
          editContract.execute(input);
          return null;
        }
      };

      task.setOnSucceeded(e -> Platform.runLater(() -> navigationService.goBack()));
      task.setOnFailed(e -> handleError(task.getException()));
      new Thread(task).start();

    } else {
      CreateContractInput input = new CreateContractInput(
          detailsPane.getStartDate(),
          Period.ofMonths(detailsPane.getDurationMonths()),
          detailsPane.getPaymentDay(),
          detailsPane.getRentCents(),
          detailsPane.getPurpose(),
          accountId,
          property.getId(),
          landlordRef,
          tenantRefs,
          guarantorRefs,
          witnessRefs
      );

      Task<CreateContractOutput> task = new Task<>() {
        @Override
        protected CreateContractOutput call() {
          return createContract.execute(input);
        }
      };

      task.setOnSucceeded(e -> Platform.runLater(() -> navigationService.goBack()));
      task.setOnFailed(e -> handleError(task.getException()));
      new Thread(task).start();
    }
  }

  private PersonReference toPersonReference(Person p) {
    PersonReference.PersonType type = (p instanceof PhysicalPerson)
        ? PersonReference.PersonType.PHYSICAL
        : PersonReference.PersonType.JURIDICAL;
    return new PersonReference(p.getId(), type);
  }

  private void handleError(Throwable t) {
    Platform.runLater(() -> {
      nextButton.setDisable(false);
      backButton.setDisable(false);
    });
    ErrorHandler.handle(t, bundle);
  }

  // ── Build view ─────────────────────────────────────────────────────────────

  public Node buildView() {
    URL resource = getClass().getResource(WIZARD_FXML);
    if (resource == null) {
      log.error("contract-wizard-view.fxml not found at {}", WIZARD_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load contract wizard view", e);
      return new StackPane();
    }
  }
}
