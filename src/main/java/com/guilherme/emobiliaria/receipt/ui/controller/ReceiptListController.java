package com.guilherme.emobiliaria.receipt.ui.controller;

import com.google.inject.Provider;
import com.guilherme.emobiliaria.contract.application.input.FindAllContractsInput;
import com.guilherme.emobiliaria.contract.application.usecase.FindAllContractsInteractor;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.receipt.application.input.DeleteReceiptInput;
import com.guilherme.emobiliaria.receipt.application.input.FindAllReceiptsByContractIdInput;
import com.guilherme.emobiliaria.receipt.application.input.GenerateReceiptPdfInput;
import com.guilherme.emobiliaria.receipt.application.output.FindAllReceiptsByContractIdOutput;
import com.guilherme.emobiliaria.receipt.application.output.GenerateReceiptPdfOutput;
import com.guilherme.emobiliaria.receipt.application.usecase.DeleteReceiptInteractor;
import com.guilherme.emobiliaria.receipt.application.usecase.FindAllReceiptsByContractIdInteractor;
import com.guilherme.emobiliaria.receipt.application.usecase.GenerateReceiptPdfInteractor;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class ReceiptListController {

  private static final Logger log = LoggerFactory.getLogger(ReceiptListController.class);

  private static final String LIST_FXML =
      "/com/guilherme/emobiliaria/receipt/ui/view/receipt-list-view.fxml";
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final int PAGE_SIZE = 20;
  private static final int LOAD_ALL_LIMIT = 10_000;

  // ── Injected dependencies ──────────────────────────────────────────────────

  private final FindAllReceiptsByContractIdInteractor findAllByContract;
  private final FindAllContractsInteractor findAllContracts;
  private final DeleteReceiptInteractor deleteReceipt;
  private final GenerateReceiptPdfInteractor generatePdf;
  private final NavigationService navigationService;
  private final Provider<ReceiptFormController> formControllerProvider;
  private final GuiceFxmlLoader fxmlLoader;

  @Inject
  public ReceiptListController(
      FindAllReceiptsByContractIdInteractor findAllByContract,
      FindAllContractsInteractor findAllContracts,
      DeleteReceiptInteractor deleteReceipt,
      GenerateReceiptPdfInteractor generatePdf,
      NavigationService navigationService,
      Provider<ReceiptFormController> formControllerProvider,
      GuiceFxmlLoader fxmlLoader) {
    this.findAllByContract = findAllByContract;
    this.findAllContracts = findAllContracts;
    this.deleteReceipt = deleteReceipt;
    this.generatePdf = generatePdf;
    this.navigationService = navigationService;
    this.formControllerProvider = formControllerProvider;
    this.fxmlLoader = fxmlLoader;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML private Label titleLabel;
  @FXML private Label subtitleLabel;
  @FXML private Button newButton;
  @FXML private Label contractFilterLabel;
  @FXML private ComboBox<Contract> contractComboBox;
  @FXML private Button clearFilterButton;
  @FXML private TableView<Receipt> tableView;
  @FXML private Label emptyLabel;
  @FXML private Button prevButton;
  @FXML private Label pageLabel;
  @FXML private Button nextButton;

  // ── State ──────────────────────────────────────────────────────────────────

  private int currentPage = 1;
  private int totalPages = 1;
  private long totalResults = 0;
  private Long preSelectedContractId = null;
  private ResourceBundle bundle;

  // ── Pre-selection (called before buildView) ────────────────────────────────

  public void setContractId(Long contractId) {
    this.preSelectedContractId = contractId;
  }

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());

    titleLabel.setText(bundle.getString("receipt.list.title"));
    subtitleLabel.setText(bundle.getString("receipt.list.subtitle"));
    newButton.setText(bundle.getString("receipt.list.button.new"));
    contractFilterLabel.setText(bundle.getString("receipt.list.filter.contract"));
    clearFilterButton.setText(bundle.getString("receipt.list.button.clear_filter"));
    prevButton.setText(bundle.getString("receipt.list.button.prev"));
    nextButton.setText(bundle.getString("receipt.list.button.next"));
    emptyLabel.setText(bundle.getString("receipt.list.empty"));

    contractComboBox.setCellFactory(lv -> contractCell());
    contractComboBox.setButtonCell(contractCell());

    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    buildTableColumns();

    newButton.setOnAction(e -> navigateToCreate());
    clearFilterButton.setOnAction(e -> {
      contractComboBox.getSelectionModel().clearSelection();
      loadPage(1);
    });
    contractComboBox.getSelectionModel().selectedItemProperty().addListener(
        (obs, old, selected) -> loadPage(1));
    prevButton.setOnAction(e -> { if (currentPage > 1) loadPage(currentPage - 1); });
    nextButton.setOnAction(e -> { if (currentPage < totalPages) loadPage(currentPage + 1); });

    loadContracts();
  }

  // ── Contract cell factory ──────────────────────────────────────────────────

  private static ListCell<Contract> contractCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(Contract contract, boolean empty) {
        super.updateItem(contract, empty);
        if (empty || contract == null) {
          setText(null);
        } else {
          String propertyName = contract.getProperty() != null ? contract.getProperty().getName() : "";
          String tenantName = (contract.getTenants() != null && !contract.getTenants().isEmpty())
              ? contractTenantName(contract) : "";
          setText(propertyName + (tenantName.isBlank() ? "" : " - " + tenantName));
        }
      }
    };
  }

  private static String contractTenantName(Contract contract) {
    var first = contract.getTenants().get(0);
    if (first instanceof com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson pp) {
      return pp.getName();
    }
    if (first instanceof com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson jp) {
      return jp.getCorporateName();
    }
    return "";
  }

  // ── Load contracts into combo box ──────────────────────────────────────────

  private void loadContracts() {
    Task<List<Contract>> task = new Task<>() {
      @Override
      protected List<Contract> call() {
        return findAllContracts.execute(
            new FindAllContractsInput(new PaginationInput(LOAD_ALL_LIMIT, 0))).result().items();
      }
    };

    task.setOnSucceeded(e -> {
      List<Contract> contracts = task.getValue();
      contractComboBox.getItems().setAll(contracts);
      if (preSelectedContractId != null) {
        contracts.stream()
            .filter(c -> c.getId().equals(preSelectedContractId))
            .findFirst()
            .ifPresent(c -> contractComboBox.getSelectionModel().select(c));
      } else {
        loadPage(1);
      }
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  // ── Table columns ──────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private void buildTableColumns() {
    TableColumn<Receipt, String> dateCol = new TableColumn<>(
        bundle.getString("receipt.list.column.date"));
    dateCol.setCellValueFactory(c -> {
      var date = c.getValue().getDate();
      return new SimpleStringProperty(date != null ? date.format(DATE_FMT) : "");
    });

    TableColumn<Receipt, String> periodCol = new TableColumn<>(
        bundle.getString("receipt.list.column.period"));
    periodCol.setCellValueFactory(c -> {
      var r = c.getValue();
      if (r.getIntervalStart() == null || r.getIntervalEnd() == null) {
        return new SimpleStringProperty("");
      }
      String period = r.getIntervalStart().format(DATE_FMT) + " a " + r.getIntervalEnd().format(DATE_FMT);
      return new SimpleStringProperty(period);
    });

    TableColumn<Receipt, String> contractCol = new TableColumn<>(
        bundle.getString("receipt.list.column.contract"));
    contractCol.setCellValueFactory(c -> {
      var contract = c.getValue().getContract();
      String name = (contract != null && contract.getProperty() != null)
          ? contract.getProperty().getName() : "";
      return new SimpleStringProperty(name);
    });

    TableColumn<Receipt, String> valueCol = new TableColumn<>(
        bundle.getString("receipt.list.column.value"));
    valueCol.setCellValueFactory(c -> {
      var r = c.getValue();
      int rent = r.getContract() != null ? r.getContract().getRent() : 0;
      int value = rent - r.getDiscount() + r.getFine();
      String formatted = "R$ " + String.format("%.2f", value / 100.0).replace('.', ',');
      return new SimpleStringProperty(formatted);
    });

    TableColumn<Receipt, Void> actionsCol = new TableColumn<>(
        bundle.getString("receipt.list.column.actions"));
    actionsCol.setCellFactory(col -> new ActionsCell());
    actionsCol.setStyle("-fx-alignment: CENTER-RIGHT;");

    for (var col : new TableColumn[]{dateCol, periodCol, contractCol, valueCol, actionsCol}) {
      col.setReorderable(false);
      col.setResizable(true);
    }

    dateCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));
    periodCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.22));
    contractCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.20));
    valueCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.16));
    actionsCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.28));

    tableView.getColumns().setAll(dateCol, periodCol, contractCol, valueCol, actionsCol);
  }

  // ── Load page ──────────────────────────────────────────────────────────────

  void loadPage(int page) {
    Contract selected = contractComboBox.getSelectionModel().getSelectedItem();
    if (selected == null) {
      tableView.getItems().clear();
      tableView.setVisible(false);
      tableView.setManaged(false);
      emptyLabel.setVisible(true);
      emptyLabel.setManaged(true);
      emptyLabel.setText(bundle.getString("receipt.list.subtitle.empty"));
      totalResults = 0;
      totalPages = 1;
      currentPage = 1;
      updatePagination();
      return;
    }

    Task<PagedResult<Receipt>> task = new Task<>() {
      @Override
      protected PagedResult<Receipt> call() {
        PaginationInput pagination = new PaginationInput(PAGE_SIZE, (page - 1) * PAGE_SIZE);
        FindAllReceiptsByContractIdOutput output = findAllByContract.execute(
            new FindAllReceiptsByContractIdInput(selected.getId(), pagination));
        return output.result();
      }
    };

    task.setOnSucceeded(e -> {
      PagedResult<Receipt> result = task.getValue();
      currentPage = page;
      totalResults = result.total();
      totalPages = (int) Math.ceil((double) result.total() / PAGE_SIZE);
      if (totalPages < 1) totalPages = 1;

      tableView.getItems().setAll(result.items());
      boolean empty = result.items().isEmpty();
      tableView.setVisible(!empty);
      tableView.setManaged(!empty);
      emptyLabel.setVisible(empty);
      emptyLabel.setManaged(empty);
      if (empty) emptyLabel.setText(bundle.getString("receipt.list.empty"));
      updatePagination();
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  // ── Pagination ─────────────────────────────────────────────────────────────

  private void updatePagination() {
    long from = totalResults == 0 ? 0 : ((long) (currentPage - 1) * PAGE_SIZE) + 1;
    long to = totalResults == 0 ? 0 : Math.min((long) currentPage * PAGE_SIZE, totalResults);
    pageLabel.setText(bundle.getString("receipt.list.results_label").formatted(from, to, totalResults));
    prevButton.setDisable(currentPage == 1);
    nextButton.setDisable(currentPage >= totalPages);
  }

  // ── Delete ─────────────────────────────────────────────────────────────────

  private void handleDelete(Receipt receipt) {
    String contractName = (receipt.getContract() != null && receipt.getContract().getProperty() != null)
        ? receipt.getContract().getProperty().getName() : "";
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText(null);
    alert.setContentText(bundle.getString("receipt.list.delete_confirm.message").formatted(contractName));
    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    Optional<ButtonType> result = alert.showAndWait();
    boolean confirmed = result.filter(b -> b == ButtonType.OK).isPresent();
    handleDelete(receipt, confirmed);
  }

  void handleDelete(Receipt receipt, boolean confirmed) {
    if (!confirmed) return;

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        deleteReceipt.execute(new DeleteReceiptInput(receipt.getId()));
        return null;
      }
    };

    task.setOnSucceeded(e -> loadPage(currentPage));
    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  // ── Generate PDF ───────────────────────────────────────────────────────────

  private void handleGeneratePdf(Receipt receipt, Button pdfBtn) {
    ProgressIndicator spinner = new ProgressIndicator();
    spinner.setMaxSize(16, 16);
    pdfBtn.setGraphic(spinner);
    pdfBtn.setText("");
    pdfBtn.setDisable(true);

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        byte[] pdfBytes = generatePdf.execute(new GenerateReceiptPdfInput(receipt.getId())).pdfBytes();
        File tmp = File.createTempFile("recibo_" + receipt.getId() + "_", ".pdf");
        tmp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
          fos.write(pdfBytes);
        }
        Desktop.getDesktop().open(tmp);
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      pdfBtn.setGraphic(null);
      pdfBtn.setText(bundle.getString("receipt.list.button.generate_pdf"));
      pdfBtn.setDisable(false);
    });
    task.setOnFailed(e -> {
      pdfBtn.setGraphic(null);
      pdfBtn.setText(bundle.getString("receipt.list.button.generate_pdf"));
      pdfBtn.setDisable(false);
      ErrorHandler.handle(task.getException(), bundle);
    });
    new Thread(task).start();
  }

  // ── Navigation ─────────────────────────────────────────────────────────────

  private void navigateToCreate() {
    ReceiptFormController ctrl = formControllerProvider.get();
    Contract selected = contractComboBox.getSelectionModel().getSelectedItem();
    if (selected != null) {
      ctrl.setContractId(selected.getId());
    }
    navigationService.navigate(ctrl::buildView);
  }

  private void navigateToEdit(long receiptId) {
    ReceiptFormController ctrl = formControllerProvider.get();
    ctrl.setReceiptId(receiptId);
    navigationService.navigate(ctrl::buildView);
  }

  // ── Build view ─────────────────────────────────────────────────────────────

  public Node buildView() {
    URL resource = getClass().getResource(LIST_FXML);
    if (resource == null) {
      log.error("receipt-list-view.fxml not found at {}", LIST_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load receipt list view", e);
      return new StackPane();
    }
  }

  // ── Inner cell class ───────────────────────────────────────────────────────

  private class ActionsCell extends TableCell<Receipt, Void> {

    private final HBox actionsBox = new HBox();
    private final Button editBtn = new Button(bundle.getString("receipt.list.button.edit"));
    private final Button deleteBtn = new Button(bundle.getString("receipt.list.button.delete"));
    private final Button pdfBtn = new Button(bundle.getString("receipt.list.button.generate_pdf"));

    ActionsCell() {
      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      setAlignment(Pos.CENTER_RIGHT);
      getStyleClass().add("list-actions-cell");
      actionsBox.setAlignment(Pos.CENTER_RIGHT);
      actionsBox.setSpacing(6);
      actionsBox.getStyleClass().add("list-actions-box");

      editBtn.getStyleClass().add("list-row-edit-button");
      deleteBtn.getStyleClass().add("list-row-delete-button");
      pdfBtn.getStyleClass().add("list-row-pdf-button");

      editBtn.setOnAction(e -> {
        Receipt receipt = getTableView().getItems().get(getIndex());
        navigateToEdit(receipt.getId());
      });
      deleteBtn.setOnAction(e -> {
        Receipt receipt = getTableView().getItems().get(getIndex());
        handleDelete(receipt);
      });
      pdfBtn.setOnAction(e -> {
        Receipt receipt = getTableView().getItems().get(getIndex());
        handleGeneratePdf(receipt, pdfBtn);
      });

      actionsBox.getChildren().setAll(editBtn, deleteBtn, pdfBtn);
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(empty ? null : actionsBox);
    }
  }
}
