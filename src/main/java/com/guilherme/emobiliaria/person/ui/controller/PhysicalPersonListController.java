package com.guilherme.emobiliaria.person.ui.controller;

import com.google.inject.Provider;
import com.guilherme.emobiliaria.person.application.input.DeletePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.FindAllPhysicalPeopleInput;
import com.guilherme.emobiliaria.person.application.input.SearchPhysicalPeopleInput;
import com.guilherme.emobiliaria.person.application.output.FindAllPhysicalPeopleOutput;
import com.guilherme.emobiliaria.person.application.output.SearchPhysicalPeopleOutput;
import com.guilherme.emobiliaria.person.application.usecase.DeletePhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindAllPhysicalPeopleInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchPhysicalPeopleInteractor;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
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
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;

import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class PhysicalPersonListController {

  // ── Injected dependencies ──────────────────────────────────────────────────

  private final FindAllPhysicalPeopleInteractor findAll;
  private final SearchPhysicalPeopleInteractor search;
  private final DeletePhysicalPersonInteractor deletePhysicalPerson;
  private final NavigationService navigationService;
  private final Provider<PhysicalPersonCreateController> createControllerProvider;
  private final Provider<PhysicalPersonEditController> editControllerProvider;

  @Inject
  public PhysicalPersonListController(
      FindAllPhysicalPeopleInteractor findAll,
      SearchPhysicalPeopleInteractor search,
      DeletePhysicalPersonInteractor deletePhysicalPerson,
      NavigationService navigationService,
      Provider<PhysicalPersonCreateController> createControllerProvider,
      Provider<PhysicalPersonEditController> editControllerProvider) {
    this.findAll = findAll;
    this.search = search;
    this.deletePhysicalPerson = deletePhysicalPerson;
    this.navigationService = navigationService;
    this.createControllerProvider = createControllerProvider;
    this.editControllerProvider = editControllerProvider;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML private Label titleLabel;
  @FXML private Label subtitleLabel;
  @FXML private Button newButton;
  @FXML private TextField searchField;
  @FXML private TableView<PhysicalPerson> tableView;
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

    // Create components programmatically when not injected via FXML
    if (tableView == null) {
      tableView = new TableView<>();
    }
    if (prevButton == null) {
      prevButton = new Button();
    }
    if (nextButton == null) {
      nextButton = new Button();
    }
    if (pageLabel == null) {
      pageLabel = new Label();
    }
    if (searchField == null) {
      searchField = new TextField();
    }
    if (newButton == null) {
      newButton = new Button();
    }
    if (titleLabel == null) {
      titleLabel = new Label();
    }
    if (subtitleLabel == null) {
      subtitleLabel = new Label();
    }

    titleLabel.setText(bundle.getString("physical_person.list.title"));
    subtitleLabel.setText(bundle.getString("physical_person.list.subtitle"));
    newButton.setText(bundle.getString("physical_person.list.button.new"));
    searchField.setPromptText(bundle.getString("physical_person.list.search_prompt"));
    prevButton.setText(bundle.getString("physical_person.list.button.prev"));
    nextButton.setText(bundle.getString("physical_person.list.button.next"));

    tableView.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);

    buildTableColumns();

    newButton.setOnAction(e -> navigateToCreate());
    prevButton.setOnAction(e -> {
      if (currentPage > 1) {
        loadPage(currentPage - 1);
      }
    });
    nextButton.setOnAction(e -> {
      if (currentPage < totalPages) {
        loadPage(currentPage + 1);
      }
    });
    searchField.textProperty().addListener((obs, oldVal, newVal) -> {
      currentPage = 1;
      loadPage(1);
    });
    loadPage(1);
  }

  // ── Table columns ──────────────────────────────────────────────────────────

  @SuppressWarnings("unchecked")
  private void buildTableColumns() {
    TableColumn<PhysicalPerson, String> nameCol = new TableColumn<>(
        bundle.getString("physical_person.list.column.name"));
    nameCol.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getName()));

    TableColumn<PhysicalPerson, String> cpfCol = new TableColumn<>(
        bundle.getString("physical_person.list.column.cpf"));
    cpfCol.setCellValueFactory(
        c -> new SimpleStringProperty(formatCpf(c.getValue().getCpf())));

    TableColumn<PhysicalPerson, String> idCardCol = new TableColumn<>(
        bundle.getString("physical_person.list.column.id_card"));
    idCardCol.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getIdCardNumber()));

    TableColumn<PhysicalPerson, Void> actionsCol = new TableColumn<>(
        bundle.getString("physical_person.list.column.actions"));
    actionsCol.setCellFactory(col -> new ActionsCell());
    actionsCol.setStyle("-fx-alignment: CENTER-RIGHT;");

    nameCol.setReorderable(false);
    cpfCol.setReorderable(false);
    idCardCol.setReorderable(false);
    actionsCol.setReorderable(false);

    nameCol.setResizable(true);
    cpfCol.setResizable(true);
    idCardCol.setResizable(true);
    actionsCol.setResizable(true);

    nameCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.34));
    cpfCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.22));
    idCardCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.28));
    actionsCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.16));

    tableView.getColumns().setAll(nameCol, cpfCol, idCardCol, actionsCol);
  }

  // ── Load page ──────────────────────────────────────────────────────────────

  void loadPage(int page) {
    String query = searchField.getText();

    Task<PagedResult<PhysicalPerson>> task = new Task<>() {
      @Override
      protected PagedResult<PhysicalPerson> call() {
        PaginationInput pagination = new PaginationInput(PAGE_SIZE, (page - 1) * PAGE_SIZE);
        if (query.isBlank()) {
          FindAllPhysicalPeopleOutput output = findAll.execute(
              new FindAllPhysicalPeopleInput(pagination));
          return output.result();
        } else {
          SearchPhysicalPeopleOutput output = search.execute(
              new SearchPhysicalPeopleInput(query.trim(), pagination));
          return output.result();
        }
      }
    };

    task.setOnSucceeded(e -> {
      PagedResult<PhysicalPerson> result = task.getValue();
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
    pageLabel.setText(bundle.getString("physical_person.list.results_label")
        .formatted(from, to, totalResults));
    prevButton.setDisable(currentPage == 1);
    nextButton.setDisable(currentPage >= totalPages);
  }

  // ── Delete ─────────────────────────────────────────────────────────────────

  private void handleDelete(PhysicalPerson person) {
    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
    alert.setHeaderText(null);
    alert.setContentText(
        bundle.getString("physical_person.list.delete_confirm.message")
            .formatted(person.getName()));
    alert.getButtonTypes().setAll(ButtonType.OK, ButtonType.CANCEL);
    Optional<ButtonType> result = alert.showAndWait();
    boolean confirmed = result.filter(b -> b == ButtonType.OK).isPresent();
    handleDelete(person, confirmed);
  }

  void handleDelete(PhysicalPerson person, boolean confirmed) {
    if (!confirmed) {
      return;
    }

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() {
        deletePhysicalPerson.execute(new DeletePhysicalPersonInput(person.getId()));
        return null;
      }
    };

    task.setOnSucceeded(e -> loadPage(currentPage));
    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));

    new Thread(task).start();
  }

  // ── Navigation ─────────────────────────────────────────────────────────────

  private void navigateToCreate() {
    PhysicalPersonCreateController ctrl = createControllerProvider.get();
    navigationService.navigate(ctrl::buildView);
  }

  private void navigateToEdit(long personId) {
    PhysicalPersonEditController ctrl = editControllerProvider.get();
    ctrl.setPersonId(personId);
    navigationService.navigate(ctrl::buildView);
  }

  // ── Helpers ────────────────────────────────────────────────────────────────

  private static String formatCpf(String raw) {
    if (raw == null || raw.length() != 11) {
      return raw == null ? "" : raw;
    }
    return raw.substring(0, 3) + "." + raw.substring(3, 6) + "." + raw.substring(6, 9)
        + "-" + raw.substring(9, 11);
  }

  // ── Test accessors ─────────────────────────────────────────────────────────

  TableView<PhysicalPerson> getTableView() {
    return tableView;
  }

  Button getPrevButton() {
    return prevButton;
  }

  Button getNextButton() {
    return nextButton;
  }

  // ── Inner cell class ───────────────────────────────────────────────────────

  private class ActionsCell extends TableCell<PhysicalPerson, Void> {

    private final HBox actionsBox = new HBox();
    private final Button editBtn = new Button(bundle.getString("physical_person.list.button.edit"));
    private final Button deleteBtn = new Button(bundle.getString("physical_person.list.button.delete"));

    ActionsCell() {
      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      setAlignment(Pos.CENTER_RIGHT);
      getStyleClass().add("list-actions-cell");
      actionsBox.setAlignment(Pos.CENTER_RIGHT);
      actionsBox.setSpacing(6);
      actionsBox.getStyleClass().add("list-actions-box");

      editBtn.getStyleClass().add("list-row-edit-button");
      deleteBtn.getStyleClass().add("list-row-delete-button");

      editBtn.setOnAction(e -> {
        PhysicalPerson person = getTableView().getItems().get(getIndex());
        navigateToEdit(person.getId());
      });
      deleteBtn.setOnAction(e -> {
        PhysicalPerson person = getTableView().getItems().get(getIndex());
        handleDelete(person);
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
