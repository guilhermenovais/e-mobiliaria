package com.guilherme.emobiliaria.property.ui.controller;

import com.google.inject.Provider;
import com.guilherme.emobiliaria.property.application.input.DeletePropertyInput;
import com.guilherme.emobiliaria.property.application.input.FindAllPropertiesInput;
import com.guilherme.emobiliaria.property.application.output.FindAllPropertiesOutput;
import com.guilherme.emobiliaria.property.application.usecase.DeletePropertyInteractor;
import com.guilherme.emobiliaria.property.application.usecase.FindAllPropertiesInteractor;
import com.guilherme.emobiliaria.property.domain.entity.Property;
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
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class PropertyListController {

  // ── Injected dependencies ──────────────────────────────────────────────────

  private final FindAllPropertiesInteractor findAll;
  private final DeletePropertyInteractor deleteProperty;
  private final NavigationService navigationService;
  private final Provider<PropertyCreateController> createControllerProvider;
  private final Provider<PropertyEditController> editControllerProvider;

  @Inject
  public PropertyListController(
      FindAllPropertiesInteractor findAll,
      DeletePropertyInteractor deleteProperty,
      NavigationService navigationService,
      Provider<PropertyCreateController> createControllerProvider,
      Provider<PropertyEditController> editControllerProvider) {
    this.findAll = findAll;
    this.deleteProperty = deleteProperty;
    this.navigationService = navigationService;
    this.createControllerProvider = createControllerProvider;
    this.editControllerProvider = editControllerProvider;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML private Label titleLabel;
  @FXML private Label subtitleLabel;
  @FXML private Button newButton;
  @FXML private TableView<Property> tableView;
  @FXML private Button prevButton;
  @FXML private Label pageLabel;
  @FXML private Button nextButton;

  // ── State ──────────────────────────────────────────────────────────────────

  private int currentPage = 1;
  private static final int PAGE_SIZE = 20;
  private int totalPages = 1;
  private long totalResults = 0;
  private ResourceBundle bundle;

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    if (tableView == null) tableView = new TableView<>();
    if (prevButton == null) prevButton = new Button();
    if (nextButton == null) nextButton = new Button();
    if (pageLabel == null) pageLabel = new Label();
    if (newButton == null) newButton = new Button();
    if (titleLabel == null) titleLabel = new Label();
    if (subtitleLabel == null) subtitleLabel = new Label();

    titleLabel.setText(bundle.getString("property.list.title"));
    subtitleLabel.setText(bundle.getString("property.list.subtitle"));
    newButton.setText(bundle.getString("property.list.button.new"));
    prevButton.setText(bundle.getString("property.list.button.prev"));
    nextButton.setText(bundle.getString("property.list.button.next"));

    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    buildTableColumns();

    newButton.setOnAction(e -> navigateToCreate());
    prevButton.setOnAction(e -> {
      if (currentPage > 1) loadPage(currentPage - 1);
    });
    nextButton.setOnAction(e -> {
      if (currentPage < totalPages) loadPage(currentPage + 1);
    });

    loadPage(1);
  }

  // ── Table columns ──────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private void buildTableColumns() {
    TableColumn<Property, String> nameCol = new TableColumn<>(
        bundle.getString("property.list.column.name"));
    nameCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));

    TableColumn<Property, String> typeCol = new TableColumn<>(
        bundle.getString("property.list.column.type"));
    typeCol.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getType()));

    TableColumn<Property, String> purposeCol = new TableColumn<>(
        bundle.getString("property.list.column.purpose"));
    purposeCol.setCellValueFactory(c -> new SimpleStringProperty(
        bundle.getString("purpose." + c.getValue().getPurpose().name())));

    TableColumn<Property, Void> actionsCol = new TableColumn<>(
        bundle.getString("property.list.column.actions"));
    actionsCol.setCellFactory(col -> new ActionsCell());
    actionsCol.setStyle("-fx-alignment: CENTER-RIGHT;");

    nameCol.setReorderable(false);
    typeCol.setReorderable(false);
    purposeCol.setReorderable(false);
    actionsCol.setReorderable(false);

    nameCol.setResizable(true);
    typeCol.setResizable(true);
    purposeCol.setResizable(true);
    actionsCol.setResizable(true);

    nameCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.35));
    typeCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.22));
    purposeCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.28));
    actionsCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.15));

    tableView.getColumns().setAll(nameCol, typeCol, purposeCol, actionsCol);
  }

  // ── Load page ──────────────────────────────────────────────────────────────

  void loadPage(int page) {
    Task<PagedResult<Property>> task = new Task<>() {
      @Override
      protected PagedResult<Property> call() {
        PaginationInput pagination = new PaginationInput(PAGE_SIZE, (page - 1) * PAGE_SIZE);
        FindAllPropertiesOutput output = findAll.execute(new FindAllPropertiesInput(pagination));
        return output.result();
      }
    };

    task.setOnSucceeded(e -> {
      PagedResult<Property> result = task.getValue();
      currentPage = page;
      totalResults = result.total();
      totalPages = (int) Math.ceil((double) result.total() / PAGE_SIZE);
      if (totalPages < 1) totalPages = 1;

      tableView.getItems().setAll(result.items());
      updatePagination();
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));

    new Thread(task).start();
  }

  // ── Pagination ─────────────────────────────────────────────────────────────

  private void updatePagination() {
    long from = totalResults == 0 ? 0 : ((long) (currentPage - 1) * PAGE_SIZE) + 1;
    long to = totalResults == 0 ? 0 : Math.min((long) currentPage * PAGE_SIZE, totalResults);
    pageLabel.setText(bundle.getString("property.list.results_label")
        .formatted(from, to, totalResults));
    prevButton.setDisable(currentPage == 1);
    nextButton.setDisable(currentPage >= totalPages);
  }

  // ── Delete ─────────────────────────────────────────────────────────────────

  private void handleDelete(Property property) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText(null);
    alert.setContentText(
        bundle.getString("property.list.delete_confirm.message")
            .formatted(property.getName()));
    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    Optional<ButtonType> result = alert.showAndWait();
    boolean confirmed = result.filter(b -> b == ButtonType.OK).isPresent();
    handleDelete(property, confirmed);
  }

  void handleDelete(Property property, boolean confirmed) {
    if (!confirmed) return;

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        deleteProperty.execute(new DeletePropertyInput(property.getId()));
        return null;
      }
    };

    task.setOnSucceeded(e -> loadPage(currentPage));
    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));

    new Thread(task).start();
  }

  // ── Navigation ─────────────────────────────────────────────────────────────

  private void navigateToCreate() {
    PropertyCreateController ctrl = createControllerProvider.get();
    navigationService.navigate(ctrl::buildView);
  }

  private void navigateToEdit(long propertyId) {
    PropertyEditController ctrl = editControllerProvider.get();
    ctrl.setPropertyId(propertyId);
    navigationService.navigate(ctrl::buildView);
  }

  // ── Test accessors ─────────────────────────────────────────────────────────

  TableView<Property> getTableView() {
    return tableView;
  }

  Button getPrevButton() {
    return prevButton;
  }

  Button getNextButton() {
    return nextButton;
  }

  // ── Inner cell class ───────────────────────────────────────────────────────

  private class ActionsCell extends TableCell<Property, Void> {

    private final HBox actionsBox = new HBox();
    private final Button editBtn = new Button(bundle.getString("property.list.button.edit"));
    private final Button deleteBtn = new Button(bundle.getString("property.list.button.delete"));

    ActionsCell() {
      actionsBox.setAlignment(Pos.CENTER_RIGHT);
      actionsBox.setSpacing(6);
      actionsBox.getStyleClass().add("list-actions-box");

      editBtn.getStyleClass().add("list-row-edit-button");
      deleteBtn.getStyleClass().add("list-row-delete-button");

      editBtn.setOnAction(e -> {
        Property property = getTableView().getItems().get(getIndex());
        navigateToEdit(property.getId());
      });
      deleteBtn.setOnAction(e -> {
        Property property = getTableView().getItems().get(getIndex());
        handleDelete(property);
      });

      actionsBox.getChildren().setAll(editBtn, deleteBtn);
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(empty ? null : actionsBox);
    }
  }
}
