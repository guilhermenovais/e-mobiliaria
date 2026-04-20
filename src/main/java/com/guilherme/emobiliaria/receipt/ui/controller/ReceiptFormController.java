package com.guilherme.emobiliaria.receipt.ui.controller;

import com.google.inject.Provider;
import com.guilherme.emobiliaria.contract.application.input.FindAllContractsInput;
import com.guilherme.emobiliaria.contract.application.usecase.FindAllContractsInteractor;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.ContractFilter;
import com.guilherme.emobiliaria.receipt.application.input.CreateReceiptInput;
import com.guilherme.emobiliaria.receipt.application.input.EditReceiptInput;
import com.guilherme.emobiliaria.receipt.application.input.FindReceiptByIdInput;
import com.guilherme.emobiliaria.receipt.application.usecase.CreateReceiptInteractor;
import com.guilherme.emobiliaria.receipt.application.usecase.EditReceiptInteractor;
import com.guilherme.emobiliaria.receipt.application.usecase.FindReceiptByIdInteractor;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.exception.UserFacingException;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import com.guilherme.emobiliaria.shared.util.MoneyFormatter;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ReceiptFormController {

  private static final Logger log = LoggerFactory.getLogger(ReceiptFormController.class);

  private static final String FORM_FXML =
      "/com/guilherme/emobiliaria/receipt/ui/view/receipt-form-view.fxml";
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final int LOAD_ALL_LIMIT = 10_000;
  private static final String ERROR_DATE_REQUIRED = "receipt.form.error.date_required";
  private static final String ERROR_INTERVAL_REQUIRED = "receipt.form.error.interval_required";
  private static final String ERROR_INTERVAL_INVALID = "receipt.form.error.interval_invalid";
  private static final String ERROR_AMOUNT_INVALID = "receipt.form.error.amount_invalid";

  // ── Injected use cases ─────────────────────────────────────────────────────

  private final FindAllContractsInteractor findAllContracts;
  private final FindReceiptByIdInteractor findReceiptById;
  private final CreateReceiptInteractor createReceipt;
  private final EditReceiptInteractor editReceipt;
  private final NavigationService navigationService;
  private final Provider<ReceiptListController> receiptListControllerProvider;
  private final GuiceFxmlLoader fxmlLoader;
  @FXML
  private Label titleLabel;

  // ── FXML fields ────────────────────────────────────────────────────────────
  @FXML
  private Label subtitleLabel;
  @FXML
  private Label formSectionLabel;
  @FXML
  private Label contractFieldLabel;
  @FXML
  private ComboBox<Contract> contractComboBox;
  @FXML
  private Label contractErrorLabel;
  @FXML
  private Label dateFieldLabel;
  @FXML
  private DatePicker datePicker;
  @FXML
  private Label intervalStartFieldLabel;
  @FXML
  private DatePicker intervalStartPicker;
  @FXML
  private Label intervalEndFieldLabel;
  @FXML
  private DatePicker intervalEndPicker;
  @FXML
  private Label discountFieldLabel;
  @FXML
  private TextField discountField;
  @FXML
  private Label fineFieldLabel;
  @FXML
  private TextField fineField;
  @FXML
  private Label observationFieldLabel;
  @FXML
  private TextArea observationTextArea;
  @FXML
  private Button cancelButton;
  @FXML
  private Button submitButton;
  private Long receiptId = null;

  // ── Mode state ─────────────────────────────────────────────────────────────
  private Long preSelectedContractId = null;
  private ResourceBundle bundle;

  @Inject
  public ReceiptFormController(FindAllContractsInteractor findAllContracts,
      FindReceiptByIdInteractor findReceiptById, CreateReceiptInteractor createReceipt,
      EditReceiptInteractor editReceipt, NavigationService navigationService,
      Provider<ReceiptListController> receiptListControllerProvider, GuiceFxmlLoader fxmlLoader) {
    this.findAllContracts = findAllContracts;
    this.findReceiptById = findReceiptById;
    this.createReceipt = createReceipt;
    this.editReceipt = editReceipt;
    this.navigationService = navigationService;
    this.receiptListControllerProvider = receiptListControllerProvider;
    this.fxmlLoader = fxmlLoader;
  }

  private static javafx.scene.control.ListCell<Contract> contractCell() {
    return new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(Contract contract, boolean empty) {
        super.updateItem(contract, empty);
        if (empty || contract == null) {
          setText(null);
        } else {
          String propertyName =
              contract.getProperty() != null ? contract.getProperty().getName() : "";
          String tenantName = tenantDisplayName(contract);
          setText(propertyName + (tenantName.isBlank() ? "" : " - " + tenantName));
        }
      }
    };
  }

  private static String tenantDisplayName(Contract contract) {
    if (contract.getTenants() == null || contract.getTenants().isEmpty())
      return "";
    var first = contract.getTenants().get(0);
    if (first instanceof com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson pp) {
      return pp.getName();
    }
    if (first instanceof com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson jp) {
      return jp.getCorporateName();
    }
    return "";
  }

  // ── Initialization ─────────────────────────────────────────────────────────

  static PeriodInterval calculatePeriod(LocalDate today, LocalDate contractStartDate) {
    if (contractStartDate == null) {
      throw new IllegalArgumentException("contractStartDate must not be null");
    }
    LocalDate periodStart = nextOccurrenceOnOrAfter(today, contractStartDate.getDayOfMonth());
    return new PeriodInterval(periodStart, periodStart.plusMonths(1).minusDays(1));
  }

  // ── Contract cell factory ──────────────────────────────────────────────────

  static LocalDate nextOccurrenceOnOrAfter(LocalDate today, int targetDayOfMonth) {
    if (today == null) {
      throw new IllegalArgumentException("today must not be null");
    }
    if (targetDayOfMonth < 1 || targetDayOfMonth > 31) {
      throw new IllegalArgumentException("targetDayOfMonth must be between 1 and 31");
    }

    LocalDate cursor = LocalDate.of(today.getYear(), today.getMonth(), 1);
    while (true) {
      if (targetDayOfMonth <= cursor.lengthOfMonth()) {
        LocalDate candidate = cursor.withDayOfMonth(targetDayOfMonth);
        if (!candidate.isBefore(today)) {
          return candidate;
        }
      }
      cursor = cursor.plusMonths(1).withDayOfMonth(1);
    }
  }

  static int parseCentsValue(String text) {
    if (text == null || text.isBlank())
      return 0;
    try {
      java.text.NumberFormat nf =
          java.text.NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
      double value = nf.parse(text.trim()).doubleValue();
      return (int) Math.round(value * 100);
    } catch (java.text.ParseException e) {
      throw new UserFacingException(ERROR_AMOUNT_INVALID, "Invalid receipt amount format: " + text);
    }
  }

  // ── Data loading ───────────────────────────────────────────────────────────

  public void setReceiptId(Long receiptId) {
    this.receiptId = receiptId;
  }

  public void setContractId(Long contractId) {
    this.preSelectedContractId = contractId;
  }

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());

    boolean editMode = receiptId != null;

    titleLabel.setText(
        bundle.getString(editMode ? "receipt.form.title.edit" : "receipt.form.title.create"));
    subtitleLabel.setText(
        bundle.getString(editMode ? "receipt.form.subtitle.edit" : "receipt.form.subtitle.create"));
    formSectionLabel.setText(bundle.getString("receipt.form.section.data"));
    contractFieldLabel.setText(bundle.getString("receipt.form.field.contract"));
    dateFieldLabel.setText(bundle.getString("receipt.form.field.date"));
    intervalStartFieldLabel.setText(bundle.getString("receipt.form.field.interval_start"));
    intervalEndFieldLabel.setText(bundle.getString("receipt.form.field.interval_end"));
    discountFieldLabel.setText(bundle.getString("receipt.form.field.discount"));
    fineFieldLabel.setText(bundle.getString("receipt.form.field.fine"));
    observationFieldLabel.setText(bundle.getString("receipt.form.field.observation"));
    cancelButton.setText(bundle.getString("receipt.form.button.cancel"));
    submitButton.setText(
        bundle.getString(editMode ? "receipt.form.button.save" : "receipt.form.button.submit"));

    contractComboBox.setCellFactory(lv -> contractCell());
    contractComboBox.setButtonCell(contractCell());
    contractErrorLabel.setVisible(false);
    contractErrorLabel.setManaged(false);

    datePicker.setValue(LocalDate.now());
    installCreateModeContractListener(editMode);

    cancelButton.setOnAction(e -> navigationService.goBack());
    submitButton.setOnAction(e -> handleSubmit());

    loadData();
  }

  private void installCreateModeContractListener(boolean editMode) {
    if (editMode) {
      return;
    }
    contractComboBox.getSelectionModel().selectedItemProperty().addListener(
        (obs, oldValue, newValue) -> applyPeriodFromContract(newValue, LocalDate.now()));
  }

  private void applyPeriodFromContract(Contract contract, LocalDate today) {
    if (contract == null || contract.getStartDate() == null) {
      return;
    }
    PeriodInterval period = calculatePeriod(today, contract.getStartDate());
    intervalStartPicker.setValue(period.start());
    intervalEndPicker.setValue(period.end());
  }

  private void loadData() {
    submitButton.setDisable(true);

    Task<FormData> task = new Task<>() {
      @Override
      protected FormData call() {
        List<Contract> contracts = findAllContracts.execute(
                new FindAllContractsInput(new PaginationInput(LOAD_ALL_LIMIT, 0), ContractFilter.NONE))
            .result().items();

        Receipt existing = null;
        if (receiptId != null) {
          existing = findReceiptById.execute(new FindReceiptByIdInput(receiptId)).receipt();
        }
        return new FormData(contracts, existing);
      }
    };

    task.setOnSucceeded(e -> {
      FormData data = task.getValue();
      contractComboBox.getItems().setAll(data.contracts());

      if (data.existingReceipt() != null) {
        Receipt r = data.existingReceipt();
        if (r.getContract() != null) {
          data.contracts().stream().filter(c -> c.getId().equals(r.getContract().getId()))
              .findFirst().ifPresent(c -> contractComboBox.getSelectionModel().select(c));
        }
        datePicker.setValue(r.getDate());
        intervalStartPicker.setValue(r.getIntervalStart());
        intervalEndPicker.setValue(r.getIntervalEnd());
        discountField.setText(MoneyFormatter.format(r.getDiscount()));
        fineField.setText(MoneyFormatter.format(r.getFine()));
        observationTextArea.setText(r.getObservation());
      } else if (preSelectedContractId != null) {
        data.contracts().stream().filter(c -> c.getId().equals(preSelectedContractId)).findFirst()
            .ifPresent(c -> contractComboBox.getSelectionModel().select(c));

        applyPeriodFromContract(contractComboBox.getSelectionModel().getSelectedItem(),
            LocalDate.now());
      }

      submitButton.setDisable(false);
    });

    task.setOnFailed(e -> {
      submitButton.setDisable(false);
      ErrorHandler.handle(task.getException(), bundle);
    });

    new Thread(task).start();
  }

  private boolean validate() {
    boolean valid = true;

    contractErrorLabel.setVisible(false);
    contractErrorLabel.setManaged(false);

    if (contractComboBox.getSelectionModel().getSelectedItem() == null) {
      contractErrorLabel.setText(bundle.getString("receipt.form.error.contract_required"));
      contractErrorLabel.setVisible(true);
      contractErrorLabel.setManaged(true);
      valid = false;
    }

    if (datePicker.getValue() == null) {
      showValidationError(ERROR_DATE_REQUIRED);
      return false;
    }

    if (intervalStartPicker.getValue() == null || intervalEndPicker.getValue() == null) {
      showValidationError(ERROR_INTERVAL_REQUIRED);
      return false;
    }

    if (intervalStartPicker.getValue().isAfter(intervalEndPicker.getValue())) {
      showValidationError(ERROR_INTERVAL_INVALID);
      return false;
    }

    return valid;
  }

  // ── Validation ─────────────────────────────────────────────────────────────

  private void showValidationError(String translationKey) {
    ErrorHandler.handle(new UserFacingException(translationKey,
        "Receipt form validation failed: " + translationKey), bundle);
  }

  private int parseCents(TextField field) {
    return parseCentsValue(field.getText());
  }

  void handleSubmit() {
    if (!validate())
      return;

    submitButton.setDisable(true);
    cancelButton.setDisable(true);

    Contract selectedContract = contractComboBox.getSelectionModel().getSelectedItem();
    LocalDate date = datePicker.getValue();
    LocalDate intervalStart = intervalStartPicker.getValue();
    LocalDate intervalEnd = intervalEndPicker.getValue();
    String observation = observationTextArea.getText();
    if (observation != null && observation.isBlank())
      observation = null;

    int discount;
    int fine;
    try {
      discount = parseCents(discountField);
      fine = parseCents(fineField);
    } catch (UserFacingException ex) {
      handleError(ex);
      return;
    }

    if (receiptId != null) {
      EditReceiptInput input =
          new EditReceiptInput(receiptId, date, intervalStart, intervalEnd, discount, fine,
              observation, selectedContract.getId());

      Task<Void> task = new Task<>() {
        @Override
        protected Void call() {
          editReceipt.execute(input);
          return null;
        }
      };

      task.setOnSucceeded(e -> Platform.runLater(
          () -> navigateToReceiptListWithContract(selectedContract.getId())));
      task.setOnFailed(e -> handleError(task.getException()));
      new Thread(task).start();

    } else {
      CreateReceiptInput input =
          new CreateReceiptInput(date, intervalStart, intervalEnd, discount, fine, observation,
              selectedContract.getId());

      Task<Void> task = new Task<>() {
        @Override
        protected Void call() {
          createReceipt.execute(input);
          return null;
        }
      };

      task.setOnSucceeded(e -> Platform.runLater(
          () -> navigateToReceiptListWithContract(selectedContract.getId())));
      task.setOnFailed(e -> handleError(task.getException()));
      new Thread(task).start();
    }
  }

  private void navigateToReceiptListWithContract(Long contractId) {
    ReceiptListController ctrl = receiptListControllerProvider.get();
    ctrl.setContractId(contractId);
    navigationService.navigate(ctrl::buildView, "sidebar.receipts");
  }

  // ── Submission ─────────────────────────────────────────────────────────────

  private void handleError(Throwable t) {
    Platform.runLater(() -> {
      submitButton.setDisable(false);
      cancelButton.setDisable(false);
    });
    ErrorHandler.handle(t, bundle);
  }

  public Node buildView() {
    URL resource = getClass().getResource(FORM_FXML);
    if (resource == null) {
      log.error("receipt-form-view.fxml not found at {}", FORM_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load receipt form view", e);
      return new StackPane();
    }
  }


  private record FormData(List<Contract> contracts, Receipt existingReceipt) {
  }

  // ── Build view ─────────────────────────────────────────────────────────────


  record PeriodInterval(LocalDate start, LocalDate end) {
  }
}
