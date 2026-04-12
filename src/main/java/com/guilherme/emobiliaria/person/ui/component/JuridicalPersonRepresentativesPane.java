package com.guilherme.emobiliaria.person.ui.component;

import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
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

public class JuridicalPersonRepresentativesPane extends VBox {

  private final ResourceBundle bundle;

  private ObservableList<PhysicalPerson> availableList = FXCollections.observableArrayList();
  private final ObservableList<PhysicalPerson> selectedList = FXCollections.observableArrayList();

  private final TextField searchField = new TextField();
  private final ListView<PhysicalPerson> availableView = new ListView<>();
  private final ListView<PhysicalPerson> selectedView = new ListView<>();
  private final Label errorLabel = new Label();

  public JuridicalPersonRepresentativesPane(ResourceBundle bundle) {
    this.bundle = bundle;
    buildLayout();
  }

  private void buildLayout() {
    Label hint = new Label(bundle.getString("juridical_person.form.representatives.hint"));
    hint.getStyleClass().add("representative-hint-label");

    Label availableHeader = new Label(bundle.getString("juridical_person.form.representatives.available"));
    availableHeader.getStyleClass().add("form-label");
    searchField.setPromptText(bundle.getString("juridical_person.form.representatives.search.prompt"));
    searchField.getStyleClass().add("form-input");
    availableView.getStyleClass().add("representative-list");
    availableView.setCellFactory(lv -> personCell());
    availableView.setOnMouseClicked(e -> {
      if (e.getClickCount() == 2) moveToSelected();
    });
    VBox availableBox = new VBox(6, availableHeader, searchField, availableView);
    VBox.setVgrow(availableView, Priority.ALWAYS);
    HBox.setHgrow(availableBox, Priority.ALWAYS);

    Button addBtn = new Button(">");
    addBtn.getStyleClass().add("representative-transfer-button");
    addBtn.setOnAction(e -> moveToSelected());

    Button removeBtn = new Button("<");
    removeBtn.getStyleClass().add("representative-transfer-button");
    removeBtn.setOnAction(e -> moveToAvailable());

    VBox transferBox = new VBox(8, addBtn, removeBtn);
    transferBox.setAlignment(Pos.CENTER);

    Label selectedHeader = new Label(bundle.getString("juridical_person.form.representatives.selected"));
    selectedHeader.getStyleClass().add("form-label");
    selectedView.getStyleClass().add("representative-list");
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

    errorLabel.getStyleClass().add("form-error-label");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    errorLabel.setText(bundle.getString("juridical_person.form.representatives.error"));

    getChildren().addAll(hint, listsRow, errorLabel);
    setSpacing(12);

    searchField.textProperty().addListener((obs, old, text) -> applyFilter(text));
  }

  private ListCell<PhysicalPerson> personCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(PhysicalPerson p, boolean empty) {
        super.updateItem(p, empty);
        setText(empty || p == null ? null : displayName(p));
      }
    };
  }

  private static String displayName(PhysicalPerson p) {
    return p.getName() + " - CPF " + p.getCpf();
  }

  private void applyFilter(String text) {
    String lower = text == null ? "" : text.toLowerCase();
    FilteredList<PhysicalPerson> filtered = availableList.filtered(
        p -> displayName(p).toLowerCase().contains(lower));
    availableView.setItems(filtered);
  }

  private void moveToSelected() {
    PhysicalPerson selected = availableView.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    availableList.remove(selected);
    selectedList.add(selected);
    applyFilter(searchField.getText());
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
  }

  private void moveToAvailable() {
    PhysicalPerson selected = selectedView.getSelectionModel().getSelectedItem();
    if (selected == null) return;
    selectedList.remove(selected);
    availableList.add(selected);
    applyFilter(searchField.getText());
  }

  public void setAllPersons(List<PhysicalPerson> all) {
    availableList = FXCollections.observableArrayList(all);
    applyFilter(searchField.getText());
  }

  public void populate(List<PhysicalPerson> current) {
    selectedList.setAll(current);
    List<PhysicalPerson> remaining = new ArrayList<>(availableList);
    remaining.removeIf(p -> current.stream().anyMatch(r -> r.getId().equals(p.getId())));
    availableList.setAll(remaining);
    applyFilter(searchField.getText());
  }

  public List<PhysicalPerson> getSelectedRepresentatives() {
    return new ArrayList<>(selectedList);
  }

  public boolean validate() {
    boolean valid = !selectedList.isEmpty();
    errorLabel.setVisible(!valid);
    errorLabel.setManaged(!valid);
    return valid;
  }
}
