package com.guilherme.emobiliaria.property.ui.component;

import com.guilherme.emobiliaria.property.application.input.CreatePropertyInput;
import com.guilherme.emobiliaria.property.application.input.EditPropertyInput;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import javafx.collections.FXCollections;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.ResourceBundle;

public class PropertyFormPane extends GridPane {

  private final ResourceBundle bundle;
  private final TextField nameField;
  private final TextField typeField;
  private final ComboBox<Purpose> purposeCombo;
  private final TextField cemigField;
  private final TextField copasaField;
  private final TextField iptuField;

  public PropertyFormPane(ResourceBundle bundle) {
    this.bundle = bundle;

    nameField = styledInput();
    typeField = styledInput();
    purposeCombo = buildPurposeCombo();
    cemigField = styledInput();
    copasaField = styledInput();
    iptuField = styledInput();

    setHgap(16);
    setVgap(16);
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setHgrow(Priority.ALWAYS);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHgrow(Priority.ALWAYS);
    getColumnConstraints().addAll(col1, col2);

    int row = 0;
    add(formField(bundle.getString("property.form.field.name"), nameField), 0, row);
    add(formField(bundle.getString("property.form.field.purpose"), purposeCombo), 1, row);
    row++;
    add(formField(bundle.getString("property.form.field.type"), typeField), 0, row, 2, 1);
    row++;
    add(formField(bundle.getString("property.form.field.cemig"), cemigField), 0, row, 2, 1);
    row++;
    add(formField(bundle.getString("property.form.field.copasa"), copasaField), 0, row);
    add(formField(bundle.getString("property.form.field.iptu"), iptuField), 1, row);
  }

  public boolean validate() {
    boolean valid = true;
    if (isEmpty(nameField)) {
      markError(nameField);
      valid = false;
    }
    if (isEmpty(typeField)) {
      markError(typeField);
      valid = false;
    }
    if (purposeCombo.getValue() == null) {
      markComboError(purposeCombo);
      valid = false;
    }
    if (isEmpty(cemigField)) {
      markError(cemigField);
      valid = false;
    }
    if (isEmpty(copasaField)) {
      markError(copasaField);
      valid = false;
    }
    if (isEmpty(iptuField)) {
      markError(iptuField);
      valid = false;
    }
    return valid;
  }

  public CreatePropertyInput buildInput(long addressId) {
    return new CreatePropertyInput(
        nameField.getText().trim(),
        typeField.getText().trim(),
        purposeCombo.getValue(),
        cemigField.getText().trim(),
        copasaField.getText().trim(),
        iptuField.getText().trim(),
        addressId);
  }

  public EditPropertyInput buildEditInput(long id, long addressId) {
    return new EditPropertyInput(
        id,
        nameField.getText().trim(),
        typeField.getText().trim(),
        purposeCombo.getValue(),
        cemigField.getText().trim(),
        copasaField.getText().trim(),
        iptuField.getText().trim(),
        addressId);
  }

  public void populate(Property property) {
    nameField.setText(property.getName());
    typeField.setText(property.getType());
    purposeCombo.setValue(property.getPurpose());
    cemigField.setText(property.getCemig());
    copasaField.setText(property.getCopasa());
    iptuField.setText(property.getIptu());
  }

  public void clearErrors() {
    nameField.getStyleClass().removeAll("form-input-error");
    typeField.getStyleClass().removeAll("form-input-error");
    purposeCombo.getStyleClass().removeAll("form-input-error");
    cemigField.getStyleClass().removeAll("form-input-error");
    copasaField.getStyleClass().removeAll("form-input-error");
    iptuField.getStyleClass().removeAll("form-input-error");
  }

  private ComboBox<Purpose> buildPurposeCombo() {
    ComboBox<Purpose> combo =
        new ComboBox<>(FXCollections.observableArrayList(Purpose.values()));
    combo.getStyleClass().add("form-combo");
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(Purpose p) {
        if (p == null) return "";
        return bundle.getString("purpose." + p.name());
      }

      @Override
      public Purpose fromString(String s) {
        return null;
      }
    });
    return combo;
  }

  private TextField styledInput() {
    TextField field = new TextField();
    field.getStyleClass().add("form-input");
    field.setMaxWidth(Double.MAX_VALUE);
    return field;
  }

  private VBox formField(String labelText, Node input) {
    Label label = new Label(labelText);
    label.getStyleClass().add("form-label");
    return new VBox(4, label, input);
  }

  private boolean isEmpty(TextField field) {
    return field.getText() == null || field.getText().isBlank();
  }

  private void markError(TextField field) {
    field.getStyleClass().removeAll("form-input-error");
    field.getStyleClass().add("form-input-error");
    field.textProperty()
        .addListener((obs, o, n) -> field.getStyleClass().removeAll("form-input-error"));
  }

  private void markComboError(ComboBox<?> combo) {
    combo.getStyleClass().removeAll("form-input-error");
    combo.getStyleClass().add("form-input-error");
    combo.valueProperty()
        .addListener((obs, o, n) -> combo.getStyleClass().removeAll("form-input-error"));
  }
}
