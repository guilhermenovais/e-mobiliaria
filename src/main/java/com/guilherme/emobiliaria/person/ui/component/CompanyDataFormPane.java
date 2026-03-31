package com.guilherme.emobiliaria.person.ui.component;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

public class CompanyDataFormPane extends GridPane {

  private final TextField corporateNameField;
  private final TextField cnpjField;

  public CompanyDataFormPane(ResourceBundle bundle) {
    corporateNameField = styledInput();
    cnpjField = styledInput();

    setHgap(16);
    setVgap(16);
    ColumnConstraints col = new ColumnConstraints();
    col.setHgrow(Priority.ALWAYS);
    getColumnConstraints().add(col);

    add(formField(bundle.getString("setup.field.corporate_name"), corporateNameField), 0, 0);
    add(formField(bundle.getString("setup.field.cnpj"), cnpjField), 0, 1);
  }

  public boolean validate() {
    boolean valid = true;
    if (isEmpty(corporateNameField)) {
      markError(corporateNameField);
      valid = false;
    }
    if (isEmpty(cnpjField)) {
      markError(cnpjField);
      valid = false;
    }
    return valid;
  }

  public String getCorporateName() {
    return corporateNameField.getText().trim();
  }

  public String getCnpj() {
    return cnpjField.getText().trim();
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
}
