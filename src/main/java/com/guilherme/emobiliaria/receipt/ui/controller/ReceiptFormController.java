package com.guilherme.emobiliaria.receipt.ui.controller;

import com.guilherme.emobiliaria.contract.application.input.FindAllContractsInput;
import com.guilherme.emobiliaria.contract.application.usecase.FindAllContractsInteractor;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.receipt.application.input.CreateReceiptInput;
import com.guilherme.emobiliaria.receipt.application.input.EditReceiptInput;
import com.guilherme.emobiliaria.receipt.application.input.FindReceiptByIdInput;
import com.guilherme.emobiliaria.receipt.application.usecase.CreateReceiptInteractor;
import com.guilherme.emobiliaria.receipt.application.usecase.EditReceiptInteractor;
import com.guilherme.emobiliaria.receipt.application.usecase.FindReceiptByIdInteractor;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
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
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
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

  // ── Injected use cases ─────────────────────────────────────────────────────

  private final FindAllContractsInteractor findAllContracts;
  private final FindReceiptByIdInteractor findReceiptById;
  private final CreateReceiptInteractor createReceipt;
  private final EditReceiptInteractor editReceipt;
  private final NavigationService navigationService;
  private final GuiceFxmlLoader fxmlLoader;

  @Inject
  public ReceiptFormController(
      FindAllContractsInteractor findAllContracts,
      FindReceiptByIdInteractor findReceiptById,
      CreateReceiptInteractor createReceipt,
      EditReceiptInteractor editReceipt,
      NavigationService navigationService,
      GuiceFxmlLoader fxmlLoader) {
    this.findAllContracts = findAllContracts;
    this.findReceiptById = findReceiptById;
    this.createReceipt = createReceipt;
    this.editReceipt = editReceipt;
    this.navigationService = navigationService;
    this.fxmlLoader = fxmlLoader;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML private Label titleLabel;
  @FXML private Label subtitleLabel;
  @FXML private Label formSectionLabel;
  @FXML private Label contractFieldLabel;
  @FXML private ComboBox<Contract> contractComboBox;
  @FXML private Label contractErrorLabel;
  @FXML private Label dateFieldLabel;
  @FXML private DatePicker datePicker;
  @FXML private Label intervalStartFieldLabel;
  @FXML private DatePicker intervalStartPicker;
  @FXML private Label intervalEndFieldLabel;
  @FXML private DatePicker intervalEndPicker;
  @FXML private Label discountFieldLabel;
  @FXML private TextField discountField;
  @FXML private Label fineFieldLabel;
  @FXML private TextField fineField;
  @FXML private Button cancelButton;
  @FXML private Button submitButton;

  // ── Mode state ─────────────────────────────────────────────────────────────

  private Long receiptId = null;
  private Long preSelectedContractId = null;
  private ResourceBundle bundle;

  public void setReceiptId(Long receiptId) {
    this.receiptId = receiptId;
  }

  public void setContractId(Long contractId) {
    this.preSelectedContractId = contractId;
  }

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());

    boolean editMode = receiptId != null;

    titleLabel.setText(bundle.getString(editMode ? "receipt.form.title.edit" : "receipt.form.title.create"));
    subtitleLabel.setText(bundle.getString(editMode ? "receipt.form.subtitle.edit" : "receipt.form.subtitle.create"));
    formSectionLabel.setText(bundle.getString("receipt.form.section.data"));
    contractFieldLabel.setText(bundle.getString("receipt.form.field.contract"));
    dateFieldLabel.setText(bundle.getString("receipt.form.field.date"));
    intervalStartFieldLabel.setText(bundle.getString("receipt.form.field.interval_start"));
    intervalEndFieldLabel.setText(bundle.getString("receipt.form.field.interval_end"));
    discountFieldLabel.setText(bundle.getString("receipt.form.field.discount"));
    fineFieldLabel.setText(bundle.getString("receipt.form.field.fine"));
    cancelButton.setText(bundle.getString("receipt.form.button.cancel"));
    submitButton.setText(bundle.getString(editMode ? "receipt.form.button.save" : "receipt.form.button.submit"));

    contractComboBox.setCellFactory(lv -> contractCell());
    contractComboBox.setButtonCell(contractCell());
    contractErrorLabel.setVisible(false);
    contractErrorLabel.setManaged(false);

    datePicker.setValue(LocalDate.now());

    cancelButton.setOnAction(e -> navigationService.goBack());
    submitButton.setOnAction(e -> handleSubmit());

    loadData();
  }

  // ── Contract cell factory ──────────────────────────────────────────────────

  private static javafx.scene.control.ListCell<Contract> contractCell() {
    return new javafx.scene.control.ListCell<>() {
      @Override
      protected void updateItem(Contract contract, boolean empty) {
        super.updateItem(contract, empty);
        if (empty || contract == null) {
          setText(null);
        } else {
          String propertyName = contract.getProperty() != null ? contract.getProperty().getName() : "";
          String tenantName = tenantDisplayName(contract);
          setText(propertyName + (tenantName.isBlank() ? "" : " - " + tenantName));
        }
      }
    };
  }

  private static String tenantDisplayName(Contract contract) {
    if (contract.getTenants() == null || contract.getTenants().isEmpty()) return "";
    var first = contract.getTenants().get(0);
    if (first instanceof com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson pp) {
      return pp.getName();
    }
    if (first instanceof com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson jp) {
      return jp.getCorporateName();
    }
    return "";
  }

  // ── Data loading ───────────────────────────────────────────────────────────

  private record FormData(List<Contract> contracts, Receipt existingReceipt) {}

  private void loadData() {
    submitButton.setDisable(true);

    Task<FormData> task = new Task<>() {
      @Override
      protected FormData call() {
        List<Contract> contracts = findAllContracts.execute(
            new FindAllContractsInput(new PaginationInput(LOAD_ALL_LIMIT, 0))).result().items();

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
          data.contracts().stream()
              .filter(c -> c.getId().equals(r.getContract().getId()))
              .findFirst()
              .ifPresent(c -> contractComboBox.getSelectionModel().select(c));
        }
        datePicker.setValue(r.getDate());
        intervalStartPicker.setValue(r.getIntervalStart());
        intervalEndPicker.setValue(r.getIntervalEnd());
        discountField.setText(String.format("%.2f", r.getDiscount() / 100.0).replace('.', ','));
        fineField.setText(String.format("%.2f", r.getFine() / 100.0).replace('.', ','));
      } else if (preSelectedContractId != null) {
        data.contracts().stream()
            .filter(c -> c.getId().equals(preSelectedContractId))
            .findFirst()
            .ifPresent(c -> contractComboBox.getSelectionModel().select(c));
      }

      submitButton.setDisable(false);
    });

    task.setOnFailed(e -> {
      submitButton.setDisable(false);
      ErrorHandler.handle(task.getException(), bundle);
    });

    new Thread(task).start();
  }

  // ── Validation ─────────────────────────────────────────────────────────────

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
      ErrorHandler.handle(new IllegalArgumentException(
          bundle.getString("receipt.form.error.date_required")), bundle);
      return false;
    }

    if (intervalStartPicker.getValue() == null || intervalEndPicker.getValue() == null) {
      ErrorHandler.handle(new IllegalArgumentException(
          bundle.getString("receipt.form.error.interval_required")), bundle);
      return false;
    }

    if (intervalStartPicker.getValue().isAfter(intervalEndPicker.getValue())) {
      ErrorHandler.handle(new IllegalArgumentException(
          bundle.getString("receipt.form.error.interval_invalid")), bundle);
      return false;
    }

    return valid;
  }

  private int parseCents(TextField field) {
    String text = field.getText();
    if (text == null || text.isBlank()) return 0;
    try {
      double value = Double.parseDouble(text.trim().replace(',', '.'));
      return (int) Math.round(value * 100);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException(bundle.getString("receipt.form.error.amount_invalid"));
    }
  }

  // ── Submission ─────────────────────────────────────────────────────────────

  void handleSubmit() {
    if (!validate()) return;

    submitButton.setDisable(true);
    cancelButton.setDisable(true);

    Contract selectedContract = contractComboBox.getSelectionModel().getSelectedItem();
    LocalDate date = datePicker.getValue();
    LocalDate intervalStart = intervalStartPicker.getValue();
    LocalDate intervalEnd = intervalEndPicker.getValue();

    int discount;
    int fine;
    try {
      discount = parseCents(discountField);
      fine = parseCents(fineField);
    } catch (IllegalArgumentException ex) {
      handleError(ex);
      return;
    }

    if (receiptId != null) {
      EditReceiptInput input = new EditReceiptInput(
          receiptId, date, intervalStart, intervalEnd, discount, fine, selectedContract.getId());

      Task<Void> task = new Task<>() {
        @Override
        protected Void call() {
          editReceipt.execute(input);
          return null;
        }
      };

      task.setOnSucceeded(e -> Platform.runLater(() -> navigationService.goBack()));
      task.setOnFailed(e -> handleError(task.getException()));
      new Thread(task).start();

    } else {
      CreateReceiptInput input = new CreateReceiptInput(
          date, intervalStart, intervalEnd, discount, fine, selectedContract.getId());

      Task<Void> task = new Task<>() {
        @Override
        protected Void call() {
          createReceipt.execute(input);
          return null;
        }
      };

      task.setOnSucceeded(e -> Platform.runLater(() -> navigationService.goBack()));
      task.setOnFailed(e -> handleError(task.getException()));
      new Thread(task).start();
    }
  }

  private void handleError(Throwable t) {
    Platform.runLater(() -> {
      submitButton.setDisable(false);
      cancelButton.setDisable(false);
    });
    ErrorHandler.handle(t, bundle);
  }

  // ── Build view ─────────────────────────────────────────────────────────────

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
}
