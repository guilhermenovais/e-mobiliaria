package com.guilherme.emobiliaria.contract.ui.controller;

import com.google.inject.Provider;
import com.guilherme.emobiliaria.contract.application.input.DeleteContractInput;
import com.guilherme.emobiliaria.contract.application.input.FindAllContractsInput;
import com.guilherme.emobiliaria.contract.application.input.GenerateContractPdfInput;
import com.guilherme.emobiliaria.contract.application.output.FindAllContractsOutput;
import com.guilherme.emobiliaria.contract.application.output.GenerateContractPdfOutput;
import com.guilherme.emobiliaria.contract.application.usecase.DeleteContractInteractor;
import com.guilherme.emobiliaria.contract.application.usecase.FindAllContractsInteractor;
import com.guilherme.emobiliaria.contract.application.usecase.GenerateContractPdfInteractor;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.receipt.ui.controller.ReceiptListController;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class ContractListController {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final int PAGE_SIZE = 20;

  // ── Injected dependencies ──────────────────────────────────────────────────

  private final FindAllContractsInteractor findAll;
  private final DeleteContractInteractor deleteContract;
  private final GenerateContractPdfInteractor generatePdf;
  private final NavigationService navigationService;
  private final Provider<ContractWizardController> wizardControllerProvider;
  private final Provider<ReceiptListController> receiptListControllerProvider;

  @Inject
  public ContractListController(
      FindAllContractsInteractor findAll,
      DeleteContractInteractor deleteContract,
      GenerateContractPdfInteractor generatePdf,
      NavigationService navigationService,
      Provider<ContractWizardController> wizardControllerProvider,
      Provider<ReceiptListController> receiptListControllerProvider) {
    this.findAll = findAll;
    this.deleteContract = deleteContract;
    this.generatePdf = generatePdf;
    this.navigationService = navigationService;
    this.wizardControllerProvider = wizardControllerProvider;
    this.receiptListControllerProvider = receiptListControllerProvider;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML private Label titleLabel;
  @FXML private Label subtitleLabel;
  @FXML private Button newButton;
  @FXML private TableView<Contract> tableView;
  @FXML private Label emptyLabel;
  @FXML private Button prevButton;
  @FXML private Label pageLabel;
  @FXML private Button nextButton;

  // ── State ──────────────────────────────────────────────────────────────────

  private int currentPage = 1;
  private int totalPages = 1;
  private long totalResults = 0;
  private ResourceBundle bundle;

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());

    if (tableView == null) tableView = new TableView<>();
    if (prevButton == null) prevButton = new Button();
    if (nextButton == null) nextButton = new Button();
    if (pageLabel == null) pageLabel = new Label();
    if (newButton == null) newButton = new Button();
    if (titleLabel == null) titleLabel = new Label();
    if (subtitleLabel == null) subtitleLabel = new Label();
    if (emptyLabel == null) emptyLabel = new Label();

    titleLabel.setText(bundle.getString("contract.list.title"));
    subtitleLabel.setText(bundle.getString("contract.list.subtitle"));
    newButton.setText(bundle.getString("contract.list.button.new"));
    prevButton.setText(bundle.getString("contract.list.button.prev"));
    nextButton.setText(bundle.getString("contract.list.button.next"));
    emptyLabel.setText(bundle.getString("contract.list.empty"));

    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
    buildTableColumns();

    newButton.setOnAction(e -> navigateToCreate());
    prevButton.setOnAction(e -> { if (currentPage > 1) loadPage(currentPage - 1); });
    nextButton.setOnAction(e -> { if (currentPage < totalPages) loadPage(currentPage + 1); });

    loadPage(1);
  }

  // ── Table columns ──────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private void buildTableColumns() {
    TableColumn<Contract, String> propertyCol = new TableColumn<>(
        bundle.getString("contract.list.column.property"));
    propertyCol.setCellValueFactory(c -> {
      var property = c.getValue().getProperty();
      String name = property != null ? property.getName() : "";
      return new SimpleStringProperty(name);
    });

    TableColumn<Contract, String> tenantCol = new TableColumn<>(
        bundle.getString("contract.list.column.primary_tenant"));
    tenantCol.setCellValueFactory(c -> {
      var tenants = c.getValue().getTenants();
      String name = (tenants != null && !tenants.isEmpty())
          ? personDisplayName(tenants.get(0)) : "";
      return new SimpleStringProperty(name);
    });

    TableColumn<Contract, String> endDateCol = new TableColumn<>(
        bundle.getString("contract.list.column.end_date"));
    endDateCol.setCellValueFactory(c -> {
      var contract = c.getValue();
      if (contract.getStartDate() == null || contract.getDuration() == null) {
        return new SimpleStringProperty("");
      }
      String endDate = contract.getStartDate().plus(contract.getDuration()).format(DATE_FMT);
      return new SimpleStringProperty(endDate);
    });

    TableColumn<Contract, String> rentCol = new TableColumn<>(
        bundle.getString("contract.list.column.rent"));
    rentCol.setCellValueFactory(c -> {
      int cents = c.getValue().getRent();
      String formatted = "R$ " + String.format("%.2f", cents / 100.0).replace('.', ',');
      return new SimpleStringProperty(formatted);
    });

    TableColumn<Contract, Void> actionsCol = new TableColumn<>(
        bundle.getString("contract.list.column.actions"));
    actionsCol.setCellFactory(col -> new ActionsCell());
    actionsCol.setStyle("-fx-alignment: CENTER-RIGHT;");

    for (var col : new TableColumn[]{propertyCol, tenantCol, endDateCol, rentCol, actionsCol}) {
      col.setReorderable(false);
      col.setResizable(true);
    }

    propertyCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.28));
    tenantCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.20));
    endDateCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));
    rentCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.16));
    actionsCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.22));

    tableView.getColumns().setAll(propertyCol, tenantCol, endDateCol, rentCol, actionsCol);
  }

  // ── Load page ──────────────────────────────────────────────────────────────

  void loadPage(int page) {
    Task<PagedResult<Contract>> task = new Task<>() {
      @Override
      protected PagedResult<Contract> call() {
        PaginationInput pagination = new PaginationInput(PAGE_SIZE, (page - 1) * PAGE_SIZE);
        FindAllContractsOutput output = findAll.execute(new FindAllContractsInput(pagination));
        return output.result();
      }
    };

    task.setOnSucceeded(e -> {
      PagedResult<Contract> result = task.getValue();
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
      updatePagination();
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  // ── Pagination ─────────────────────────────────────────────────────────────

  private void updatePagination() {
    long from = totalResults == 0 ? 0 : ((long) (currentPage - 1) * PAGE_SIZE) + 1;
    long to = totalResults == 0 ? 0 : Math.min((long) currentPage * PAGE_SIZE, totalResults);
    pageLabel.setText(bundle.getString("contract.list.results_label").formatted(from, to, totalResults));
    prevButton.setDisable(currentPage == 1);
    nextButton.setDisable(currentPage >= totalPages);
  }

  // ── Delete ─────────────────────────────────────────────────────────────────

  private void handleDelete(Contract contract) {
    String propertyName = contract.getProperty() != null ? contract.getProperty().getName() : "";
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText(null);
    alert.setContentText(bundle.getString("contract.list.delete_confirm.message").formatted(propertyName));
    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    Optional<ButtonType> result = alert.showAndWait();
    boolean confirmed = result.filter(b -> b == ButtonType.OK).isPresent();
    handleDelete(contract, confirmed);
  }

  void handleDelete(Contract contract, boolean confirmed) {
    if (!confirmed) return;

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        deleteContract.execute(new DeleteContractInput(contract.getId()));
        return null;
      }
    };

    task.setOnSucceeded(e -> loadPage(currentPage));
    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  private void handleGeneratePdf(Contract contract) {
    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        byte[] pdfBytes = generatePdf.execute(new GenerateContractPdfInput(contract.getId())).pdfBytes();
        File tmp = File.createTempFile("contrato_" + contract.getId() + "_", ".pdf");
        tmp.deleteOnExit();
        try (FileOutputStream fos = new FileOutputStream(tmp)) {
          fos.write(pdfBytes);
        }
        Desktop.getDesktop().open(tmp);
        return null;
      }
    };

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  // ── Navigation ─────────────────────────────────────────────────────────────

  private void navigateToCreate() {
    ContractWizardController ctrl = wizardControllerProvider.get();
    navigationService.navigate(ctrl::buildView, "sidebar.contracts");
  }

  private void navigateToEdit(long contractId) {
    ContractWizardController ctrl = wizardControllerProvider.get();
    ctrl.setContractId(contractId);
    navigationService.navigate(ctrl::buildView, "sidebar.contracts");
  }

  private void navigateToReceipts(long contractId) {
    ReceiptListController ctrl = receiptListControllerProvider.get();
    ctrl.setContractId(contractId);
    navigationService.navigate(ctrl::buildView, "sidebar.receipts");
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private static String personDisplayName(Person p) {
    if (p instanceof PhysicalPerson pp) return pp.getName();
    if (p instanceof JuridicalPerson jp) return jp.getCorporateName();
    return "";
  }

  // ── Inner cell class ───────────────────────────────────────────────────────

  private class ActionsCell extends TableCell<Contract, Void> {

    private final HBox actionsBox = new HBox();
    private final Button editBtn = new Button(bundle.getString("contract.list.button.edit"));
    private final Button deleteBtn = new Button(bundle.getString("contract.list.button.delete"));
    private final Button pdfBtn = new Button(bundle.getString("contract.list.button.generate_pdf"));
    private final Button receiptsBtn = new Button(bundle.getString("contract.list.button.receipts"));

    ActionsCell() {
      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      setAlignment(Pos.CENTER_RIGHT);
      getStyleClass().add("list-actions-cell");
      actionsBox.setAlignment(Pos.CENTER_RIGHT);
      actionsBox.setSpacing(6);
      actionsBox.getStyleClass().add("list-actions-box");

      editBtn.getStyleClass().add("list-row-edit-button");
      deleteBtn.getStyleClass().add("list-row-delete-button");
      pdfBtn.getStyleClass().add("list-row-receipts-button");
      receiptsBtn.getStyleClass().add("list-row-receipts-button");

      editBtn.setOnAction(e -> {
        Contract contract = getTableView().getItems().get(getIndex());
        navigateToEdit(contract.getId());
      });
      deleteBtn.setOnAction(e -> {
        Contract contract = getTableView().getItems().get(getIndex());
        handleDelete(contract);
      });
      pdfBtn.setOnAction(e -> {
        Contract contract = getTableView().getItems().get(getIndex());
        handleGeneratePdf(contract);
      });
      receiptsBtn.setOnAction(e -> {
        Contract contract = getTableView().getItems().get(getIndex());
        navigateToReceipts(contract.getId());
      });

      actionsBox.getChildren().setAll(editBtn, deleteBtn, pdfBtn, receiptsBtn);
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(empty ? null : actionsBox);
    }
  }
}
