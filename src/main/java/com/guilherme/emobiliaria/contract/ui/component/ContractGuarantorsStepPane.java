package com.guilherme.emobiliaria.contract.ui.component;

import com.guilherme.emobiliaria.person.domain.entity.Person;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class ContractGuarantorsStepPane extends VBox {

  private final ResourceBundle bundle;

  private ObservableList<Person> availableList = FXCollections.observableArrayList();
  private final ObservableList<Person> selectedList = FXCollections.observableArrayList();

  private final TextField searchField = new TextField();
  private final ListView<Person> availableView = new ListView<>();
  private final ListView<Person> selectedView = new ListView<>();

  public ContractGuarantorsStepPane(ResourceBundle bundle) {
    this.bundle = bundle;
    getStyleClass().add("wizard-step-pane");
    buildLayout();
  }

  private void buildLayout() {
    Label hint = new Label(bundle.getString("contract.wizard.guarantors.hint"));
    hint.getStyleClass().add("wizard-hint-label");

    Label availableHeader = new Label(bundle.getString("contract.wizard.guarantors.available"));
    availableHeader.getStyleClass().add("form-label");
    searchField.setPromptText(bundle.getString("contract.wizard.guarantors.search.prompt"));
    searchField.getStyleClass().add("form-input");
    availableView.getStyleClass().add("wizard-tenant-list");
    availableView.setCellFactory(lv -> personCell());
    availableView.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2) moveToSelected();
    });
    VBox availableBox = new VBox(6, availableHeader, searchField, availableView);
    VBox.setVgrow(availableView, Priority.ALWAYS);
    HBox.setHgrow(availableBox, Priority.ALWAYS);

    Button addBtn = new Button(">");
    addBtn.getStyleClass().add("wizard-transfer-button");
    addBtn.setOnAction(e -> moveToSelected());

    Button removeBtn = new Button("<");
    removeBtn.getStyleClass().add("wizard-transfer-button");
    removeBtn.setOnAction(e -> moveToAvailable());

    VBox transferBox = new VBox(8, addBtn, removeBtn);
    transferBox.setAlignment(Pos.CENTER);

    Label selectedHeader = new Label(bundle.getString("contract.wizard.guarantors.selected"));
    selectedHeader.getStyleClass().add("form-label");
    selectedView.getStyleClass().add("wizard-tenant-list");
    selectedView.setCellFactory(lv -> personCell());
    selectedView.setItems(selectedList);
    selectedView.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2) moveToAvailable();
    });
    VBox selectedBox = new VBox(6, selectedHeader, selectedView);
    VBox.setVgrow(selectedView, Priority.ALWAYS);
    HBox.setHgrow(selectedBox, Priority.ALWAYS);

    HBox listsRow = new HBox(8, availableBox, transferBox, selectedBox);
    VBox.setVgrow(listsRow, Priority.ALWAYS);

    VBox card = new VBox(20, hint, listsRow);
    card.getStyleClass().add("wizard-section-card");
    getChildren().add(card);

    searchField.textProperty().addListener((obs, old, text) -> applyFilter(text));
  }

  private ListCell<Person> personCell() {
    return new ListCell<>() {
      @Override protected void updateItem(Person p, boolean empty) {
        super.updateItem(p, empty);
        setText(empty || p == null ? null : ContractLandlordStepPane.displayName(p));
      }
    };
  }

  private void applyFilter(String text) {
    String lower = text == null ? "" : text.toLowerCase();
    FilteredList<Person> filtered = availableList.filtered(
        p -> ContractLandlordStepPane.displayName(p).toLowerCase().contains(lower));
    availableView.setItems(filtered);
  }

  private void moveToSelected() {
    Person selected = availableView.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    availableList.remove(selected);
    selectedList.add(selected);
    applyFilter(searchField.getText());
  }

  private void moveToAvailable() {
    Person selected = selectedView.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    selectedList.remove(selected);
    availableList.add(selected);
    applyFilter(searchField.getText());
  }

  public void setAllPersons(List<Person> all) {
    availableList = FXCollections.observableArrayList(all);
    applyFilter(searchField.getText());
  }

  public void populate(List<Person> guarantors) {
    selectedList.setAll(guarantors);
    List<Person> remaining = new ArrayList<>(availableList);
    remaining.removeIf(p -> guarantors.stream().anyMatch(g -> g.getId().equals(p.getId())));
    availableList.setAll(remaining);
    applyFilter(searchField.getText());
  }

  public List<Person> getSelectedGuarantors() {
    return new ArrayList<>(selectedList);
  }

  public boolean validate() {
    return true;
  }
}
