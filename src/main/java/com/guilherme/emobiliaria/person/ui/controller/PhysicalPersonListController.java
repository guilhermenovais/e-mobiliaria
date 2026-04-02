package com.guilherme.emobiliaria.person.ui.controller;

import com.google.inject.Provider;
import com.guilherme.emobiliaria.person.application.input.DeletePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.FindAllPhysicalPeopleInput;
import com.guilherme.emobiliaria.person.application.input.SearchPhysicalPeopleByNameInput;
import com.guilherme.emobiliaria.person.application.output.FindAllPhysicalPeopleOutput;
import com.guilherme.emobiliaria.person.application.output.SearchPhysicalPeopleByNameOutput;
import com.guilherme.emobiliaria.person.application.usecase.DeletePhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindAllPhysicalPeopleInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchPhysicalPeopleByNameInteractor;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;

public class PhysicalPersonListController {

  // ── Injected dependencies ──────────────────────────────────────────────────

  private final FindAllPhysicalPeopleInteractor findAll;
  private final SearchPhysicalPeopleByNameInteractor searchByName;
  private final DeletePhysicalPersonInteractor deletePhysicalPerson;
  private final NavigationService navigationService;
  private final Provider<PhysicalPersonCreateController> createControllerProvider;
  private final Provider<PhysicalPersonEditController> editControllerProvider;

  @Inject
  public PhysicalPersonListController(
      FindAllPhysicalPeopleInteractor findAll,
      SearchPhysicalPeopleByNameInteractor searchByName,
      DeletePhysicalPersonInteractor deletePhysicalPerson,
      NavigationService navigationService,
      Provider<PhysicalPersonCreateController> createControllerProvider,
      Provider<PhysicalPersonEditController> editControllerProvider) {
    this.findAll = findAll;
    this.searchByName = searchByName;
    this.deletePhysicalPerson = deletePhysicalPerson;
    this.navigationService = navigationService;
    this.createControllerProvider = createControllerProvider;
    this.editControllerProvider = editControllerProvider;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML private Label titleLabel;
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

    titleLabel.setText(bundle.getString("physical_person.list.title"));

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
    tableView.setOnMouseClicked(event -> {
      PhysicalPerson selected = tableView.getSelectionModel().getSelectedItem();
      if (selected != null) {
        navigateToEdit(selected.getId());
      }
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

    TableColumn<PhysicalPerson, String> civilStateCol = new TableColumn<>(
        bundle.getString("physical_person.list.column.civil_state"));
    civilStateCol.setCellValueFactory(c -> {
      CivilState cs = c.getValue().getCivilState();
      return new SimpleStringProperty(bundle.getString("civil_state." + cs.name()));
    });

    TableColumn<PhysicalPerson, Void> actionsCol = new TableColumn<>(
        bundle.getString("physical_person.list.column.actions"));
    actionsCol.setCellFactory(col -> new DeleteButtonCell());

    tableView.getColumns().setAll(nameCol, cpfCol, civilStateCol, actionsCol);
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
          SearchPhysicalPeopleByNameOutput output = searchByName.execute(
              new SearchPhysicalPeopleByNameInput(query.trim(), pagination));
          return output.result();
        }
      }
    };

    task.setOnSucceeded(e -> {
      PagedResult<PhysicalPerson> result = task.getValue();
      currentPage = page;
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
    pageLabel.setText(
        bundle.getString("physical_person.list.page_label")
            .formatted(currentPage, totalPages));
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

  private class DeleteButtonCell extends TableCell<PhysicalPerson, Void> {

    private final Button deleteBtn = new Button(
        bundle.getString("physical_person.list.button.delete"));

    DeleteButtonCell() {
      deleteBtn.getStyleClass().add("btn-secondary");
      deleteBtn.setOnAction(e -> {
        PhysicalPerson person = getTableView().getItems().get(getIndex());
        handleDelete(person);
      });
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
      super.updateItem(item, empty);
      if (empty) {
        setGraphic(null);
      } else {
        setGraphic(deleteBtn);
      }
    }
  }
}
