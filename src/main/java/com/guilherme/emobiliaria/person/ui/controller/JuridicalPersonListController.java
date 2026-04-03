package com.guilherme.emobiliaria.person.ui.controller;

import com.google.inject.Provider;
import com.guilherme.emobiliaria.person.application.input.FindAllJuridicalPeopleInput;
import com.guilherme.emobiliaria.person.application.output.FindAllJuridicalPeopleOutput;
import com.guilherme.emobiliaria.person.application.usecase.FindAllJuridicalPeopleInteractor;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.Locale;
import java.util.ResourceBundle;

public class JuridicalPersonListController {

  private final FindAllJuridicalPeopleInteractor findAll;
  private final NavigationService navigationService;
  private final Provider<JuridicalPersonController> juridicalControllerProvider;

  @Inject
  public JuridicalPersonListController(
      FindAllJuridicalPeopleInteractor findAll,
      NavigationService navigationService,
      Provider<JuridicalPersonController> juridicalControllerProvider) {
    this.findAll = findAll;
    this.navigationService = navigationService;
    this.juridicalControllerProvider = juridicalControllerProvider;
  }

  @FXML private Label titleLabel;
  @FXML private Label subtitleLabel;
  @FXML private Button newButton;
  @FXML private TableView<JuridicalPerson> tableView;
  @FXML private Button prevButton;
  @FXML private Label pageLabel;
  @FXML private Button nextButton;

  private int currentPage = 1;
  private static final int PAGE_SIZE = 20;
  private int totalPages = 1;
  private long totalResults = 0;
  private ResourceBundle bundle;

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

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
    if (newButton == null) {
      newButton = new Button();
    }
    if (titleLabel == null) {
      titleLabel = new Label();
    }
    if (subtitleLabel == null) {
      subtitleLabel = new Label();
    }

    titleLabel.setText(bundle.getString("juridical_person.list.title"));
    subtitleLabel.setText(bundle.getString("juridical_person.list.subtitle"));
    newButton.setText(bundle.getString("juridical_person.list.button.new"));
    prevButton.setText(bundle.getString("juridical_person.list.button.prev"));
    nextButton.setText(bundle.getString("juridical_person.list.button.next"));

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

    loadPage(1);
  }

  @SuppressWarnings("unchecked")
  private void buildTableColumns() {
    TableColumn<JuridicalPerson, String> corporateNameCol = new TableColumn<>(
        bundle.getString("juridical_person.list.column.corporate_name"));
    corporateNameCol.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getCorporateName()));

    TableColumn<JuridicalPerson, String> cnpjCol = new TableColumn<>(
        bundle.getString("juridical_person.list.column.cnpj"));
    cnpjCol.setCellValueFactory(
        c -> new SimpleStringProperty(formatCnpj(c.getValue().getCnpj())));

    TableColumn<JuridicalPerson, String> representativeCol = new TableColumn<>(
        bundle.getString("juridical_person.list.column.representative"));
    representativeCol.setCellValueFactory(
        c -> new SimpleStringProperty(c.getValue().getRepresentative().getName()));

    TableColumn<JuridicalPerson, Void> actionsCol = new TableColumn<>(
        bundle.getString("juridical_person.list.column.actions"));
    actionsCol.setCellFactory(col -> new ActionsCell());
    actionsCol.setStyle("-fx-alignment: CENTER-RIGHT;");

    corporateNameCol.setReorderable(false);
    cnpjCol.setReorderable(false);
    representativeCol.setReorderable(false);
    actionsCol.setReorderable(false);

    corporateNameCol.setResizable(true);
    cnpjCol.setResizable(true);
    representativeCol.setResizable(true);
    actionsCol.setResizable(true);

    corporateNameCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.38));
    cnpjCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.20));
    representativeCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.28));
    actionsCol.prefWidthProperty().bind(tableView.widthProperty().multiply(0.14));

    tableView.getColumns().setAll(corporateNameCol, cnpjCol, representativeCol, actionsCol);
  }

  void loadPage(int page) {
    Task<PagedResult<JuridicalPerson>> task = new Task<>() {
      @Override
      protected PagedResult<JuridicalPerson> call() {
        PaginationInput pagination = new PaginationInput(PAGE_SIZE, (page - 1) * PAGE_SIZE);
        FindAllJuridicalPeopleOutput output = findAll.execute(
            new FindAllJuridicalPeopleInput(pagination));
        return output.result();
      }
    };

    task.setOnSucceeded(e -> {
      PagedResult<JuridicalPerson> result = task.getValue();
      currentPage = page;
      totalResults = result.total();
      totalPages = (int) Math.ceil((double) result.total() / PAGE_SIZE);
      if (totalPages < 1) {
        totalPages = 1;
      }

      tableView.getItems().setAll(result.items());
      updatePagination();
    });

    task.setOnFailed(e -> ErrorHandler.handle(task.getException(), bundle));
    new Thread(task).start();
  }

  private void updatePagination() {
    long from = totalResults == 0 ? 0 : ((long) (currentPage - 1) * PAGE_SIZE) + 1;
    long to = totalResults == 0 ? 0 : Math.min((long) currentPage * PAGE_SIZE, totalResults);
    pageLabel.setText(bundle.getString("juridical_person.list.results_label")
        .formatted(from, to, totalResults));
    prevButton.setDisable(currentPage == 1);
    nextButton.setDisable(currentPage >= totalPages);
  }

  private void navigateToCreate() {
    JuridicalPersonController ctrl = juridicalControllerProvider.get();
    ctrl.setJuridicalPersonId(null);
    navigationService.navigate(ctrl::buildView);
  }

  private void navigateToEdit(long juridicalPersonId) {
    JuridicalPersonController ctrl = juridicalControllerProvider.get();
    ctrl.setJuridicalPersonId(juridicalPersonId);
    navigationService.navigate(ctrl::buildView);
  }

  private static String formatCnpj(String raw) {
    if (raw == null || raw.length() != 14) {
      return raw == null ? "" : raw;
    }
    return raw.substring(0, 2) + "." + raw.substring(2, 5) + "." + raw.substring(5, 8)
        + "/" + raw.substring(8, 12) + "-" + raw.substring(12, 14);
  }

  TableView<JuridicalPerson> getTableView() {
    return tableView;
  }

  Button getPrevButton() {
    return prevButton;
  }

  Button getNextButton() {
    return nextButton;
  }

  private class ActionsCell extends TableCell<JuridicalPerson, Void> {
    private final HBox actionsBox = new HBox();
    private final Button editBtn = new Button(bundle.getString("juridical_person.list.button.edit"));

    ActionsCell() {
      setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
      setAlignment(Pos.CENTER_RIGHT);
      getStyleClass().add("list-actions-cell");
      actionsBox.setAlignment(Pos.CENTER_RIGHT);
      actionsBox.setSpacing(6);
      actionsBox.getStyleClass().add("list-actions-box");

      editBtn.getStyleClass().add("list-row-edit-button");
      editBtn.setOnAction(e -> {
        JuridicalPerson person = getTableView().getItems().get(getIndex());
        navigateToEdit(person.getId());
      });

      actionsBox.getChildren().setAll(editBtn);
    }

    @Override
    protected void updateItem(Void item, boolean empty) {
      super.updateItem(item, empty);
      setGraphic(empty ? null : actionsBox);
    }
  }
}
