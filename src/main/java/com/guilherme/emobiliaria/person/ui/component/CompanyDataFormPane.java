package com.guilherme.emobiliaria.person.ui.component;

import com.guilherme.emobiliaria.person.application.usecase.ValidateCnpjInteractor;
import com.guilherme.emobiliaria.shared.ui.component.MaskedTextField;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

public class CompanyDataFormPane extends GridPane {

  private final ResourceBundle bundle;
  private final ValidateCnpjInteractor validateCnpj;
  private final TextField corporateNameField;
  private final MaskedTextField cnpjField;
  private final Label cnpjErrorLabel;

  public CompanyDataFormPane(ResourceBundle bundle, ValidateCnpjInteractor validateCnpj) {
    this.bundle = bundle;
    this.validateCnpj = validateCnpj;

    corporateNameField = styledInput();
    cnpjField = new MaskedTextField("00.000.000/0000-00");

    cnpjErrorLabel = new Label();
    cnpjErrorLabel.getStyleClass().add("form-error-label");
    cnpjErrorLabel.setVisible(false);
    cnpjErrorLabel.setManaged(false);

    setHgap(16);
    setVgap(16);
    ColumnConstraints col = new ColumnConstraints();
    col.setHgrow(Priority.ALWAYS);
    getColumnConstraints().add(col);

    add(formField(bundle.getString("setup.field.corporate_name"), corporateNameField), 0, 0);
    add(formFieldWithError(bundle.getString("setup.field.cnpj"), cnpjField, cnpjErrorLabel), 0, 1);

    cnpjField.focusedProperty().addListener((obs, wasFocused, isNow) -> {
      if (!isNow && !cnpjField.getValue().isEmpty()) {
        runCnpjValidation();
      }
    });
  }

  public boolean validate() {
    boolean valid = true;
    if (isEmpty(corporateNameField)) {
      markError(corporateNameField);
      valid = false;
    }
    if (cnpjField.getValue().isEmpty()) {
      markError(cnpjField);
      valid = false;
    } else {
      var error = validateCnpj.execute(cnpjField.getValue());
      if (error.isPresent()) {
        showCnpjError(bundle.getString(error.get().getTranslationKey()));
        valid = false;
      }
    }
    return valid;
  }

  public String getCorporateName() {
    return corporateNameField.getText().trim();
  }

  public String getCnpj() {
    return cnpjField.getValue();
  }

  private void runCnpjValidation() {
    var error = validateCnpj.execute(cnpjField.getValue());
    if (error.isPresent()) {
      showCnpjError(bundle.getString(error.get().getTranslationKey()));
    } else {
      cnpjField.getStyleClass().removeAll("form-input-error");
      cnpjErrorLabel.setVisible(false);
      cnpjErrorLabel.setManaged(false);
    }
  }

  private void showCnpjError(String message) {
    cnpjField.getStyleClass().removeAll("form-input-error");
    cnpjField.getStyleClass().add("form-input-error");
    cnpjErrorLabel.setText(message);
    cnpjErrorLabel.setVisible(true);
    cnpjErrorLabel.setManaged(true);
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

  private VBox formFieldWithError(String labelText, Node input, Label errorLabel) {
    Label label = new Label(labelText);
    label.getStyleClass().add("form-label");
    return new VBox(4, label, input, errorLabel);
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
