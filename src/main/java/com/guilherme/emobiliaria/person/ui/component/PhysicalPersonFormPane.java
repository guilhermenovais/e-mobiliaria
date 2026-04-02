package com.guilherme.emobiliaria.person.ui.component;

import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.EditPhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.usecase.ValidateCpfInteractor;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.shared.ui.component.MaskedTextField;
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

public class PhysicalPersonFormPane extends GridPane {

  private final ResourceBundle bundle;
  private final ValidateCpfInteractor validateCpf;
  private final TextField nameField;
  private final TextField nationalityField;
  private final ComboBox<CivilState> civilStateCombo;
  private final TextField occupationField;
  private final MaskedTextField cpfField;
  private final TextField idCardField;
  private final Label cpfErrorLabel;

  public PhysicalPersonFormPane(ResourceBundle bundle, ValidateCpfInteractor validateCpf) {
    this.bundle = bundle;
    this.validateCpf = validateCpf;

    nameField = styledInput();
    nationalityField = styledInput();
    civilStateCombo = buildCivilStateCombo();
    occupationField = styledInput();
    cpfField = new MaskedTextField("000.000.000-00");
    idCardField = styledInput();

    cpfErrorLabel = new Label();
    cpfErrorLabel.getStyleClass().add("form-error-label");
    cpfErrorLabel.setVisible(false);
    cpfErrorLabel.setManaged(false);

    setHgap(16);
    setVgap(16);
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setHgrow(Priority.ALWAYS);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHgrow(Priority.ALWAYS);
    getColumnConstraints().addAll(col1, col2);

    int row = 0;
    add(formField(bundle.getString("setup.field.name"), nameField), 0, row, 2, 1);
    row++;
    add(formField(bundle.getString("setup.field.nationality"), nationalityField), 0, row);
    add(formField(bundle.getString("setup.field.civil_state"), civilStateCombo), 1, row);
    row++;
    add(formField(bundle.getString("setup.field.occupation"), occupationField), 0, row, 2, 1);
    row++;
    add(formFieldWithError(bundle.getString("setup.field.cpf"), cpfField, cpfErrorLabel), 0, row);
    add(formField(bundle.getString("setup.field.id_card"), idCardField), 1, row);

    cpfField.focusedProperty().addListener((obs, wasFocused, isNow) -> {
      if (!isNow && !cpfField.getValue().isEmpty()) {
        runCpfValidation();
      }
    });
  }

  public boolean validate() {
    boolean valid = true;
    if (isEmpty(nameField)) {
      markError(nameField);
      valid = false;
    }
    if (isEmpty(nationalityField)) {
      markError(nationalityField);
      valid = false;
    }
    if (civilStateCombo.getValue() == null) {
      markComboError(civilStateCombo);
      valid = false;
    }
    if (isEmpty(occupationField)) {
      markError(occupationField);
      valid = false;
    }
    if (cpfField.getValue().isEmpty()) {
      markError(cpfField);
      valid = false;
    } else {
      var error = validateCpf.execute(cpfField.getValue());
      if (error.isPresent()) {
        showCpfError(bundle.getString(error.get().getTranslationKey()));
        valid = false;
      }
    }
    if (isEmpty(idCardField)) {
      markError(idCardField);
      valid = false;
    }
    return valid;
  }

  public CreatePhysicalPersonInput buildInput(long addressId) {
    return new CreatePhysicalPersonInput(nameField.getText().trim(),
        nationalityField.getText().trim(), civilStateCombo.getValue(),
        occupationField.getText().trim(), cpfField.getText().trim(), idCardField.getText().trim(),
        addressId);
  }

  public void populate(PhysicalPerson person) {
    nameField.setText(person.getName());
    nationalityField.setText(person.getNationality());
    civilStateCombo.setValue(person.getCivilState());
    occupationField.setText(person.getOccupation());
    cpfField.setText(person.getCpf());
    idCardField.setText(person.getIdCardNumber());
  }

  public EditPhysicalPersonInput buildEditInput(long id, long addressId) {
    return new EditPhysicalPersonInput(id, nameField.getText().trim(),
        nationalityField.getText().trim(), civilStateCombo.getValue(),
        occupationField.getText().trim(), cpfField.getText().trim(),
        idCardField.getText().trim(), addressId);
  }

  public void clearErrors() {
    nameField.getStyleClass().removeAll("form-input-error");
    nationalityField.getStyleClass().removeAll("form-input-error");
    civilStateCombo.getStyleClass().removeAll("form-input-error");
    occupationField.getStyleClass().removeAll("form-input-error");
    cpfField.getStyleClass().removeAll("form-input-error");
    cpfErrorLabel.setVisible(false);
    cpfErrorLabel.setManaged(false);
    idCardField.getStyleClass().removeAll("form-input-error");
  }

  private void runCpfValidation() {
    var error = validateCpf.execute(cpfField.getValue());
    if (error.isPresent()) {
      showCpfError(bundle.getString(error.get().getTranslationKey()));
    } else {
      cpfField.getStyleClass().removeAll("form-input-error");
      cpfErrorLabel.setVisible(false);
      cpfErrorLabel.setManaged(false);
    }
  }

  private void showCpfError(String message) {
    cpfField.getStyleClass().removeAll("form-input-error");
    cpfField.getStyleClass().add("form-input-error");
    cpfErrorLabel.setText(message);
    cpfErrorLabel.setVisible(true);
    cpfErrorLabel.setManaged(true);
  }

  private ComboBox<CivilState> buildCivilStateCombo() {
    ComboBox<CivilState> combo =
        new ComboBox<>(FXCollections.observableArrayList(CivilState.values()));
    combo.getStyleClass().add("form-combo");
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.setConverter(new StringConverter<>() {
      @Override
      public String toString(CivilState cs) {
        if (cs == null)
          return "";
        return bundle.getString("civil_state." + cs.name());
      }

      @Override
      public CivilState fromString(String s) {
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

  private void markComboError(ComboBox<?> combo) {
    combo.getStyleClass().removeAll("form-input-error");
    combo.getStyleClass().add("form-input-error");
    combo.valueProperty()
        .addListener((obs, o, n) -> combo.getStyleClass().removeAll("form-input-error"));
  }
}
