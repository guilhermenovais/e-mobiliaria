package com.guilherme.emobiliaria.person.ui.component;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.EditAddressInput;
import com.guilherme.emobiliaria.person.application.input.SearchAddressByCepInput;
import com.guilherme.emobiliaria.person.application.output.SearchAddressByCepOutput;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.shared.ui.component.MaskedTextField;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

public class AddressFormPane extends VBox {

  private final SearchAddressByCepInteractor searchByCep;
  private final ResourceBundle bundle;

  private final MaskedTextField cepField;
  private final TextField streetField;
  private final TextField numberField;
  private final TextField complementField;
  private final TextField neighborhoodField;
  private final TextField cityField;
  private final ComboBox<BrazilianState> stateCombo;
  private final Label cepErrorLabel;

  private Runnable disableNext = () -> {
  };
  private Runnable enableNext = () -> {
  };
  private boolean populating = false;

  public AddressFormPane(SearchAddressByCepInteractor searchByCep, ResourceBundle bundle) {
    this.searchByCep = searchByCep;
    this.bundle = bundle;

    cepField = new MaskedTextField("00000-000");
    streetField = styledInput();
    numberField = styledInput();
    complementField = styledInput();
    neighborhoodField = styledInput();
    cityField = styledInput();
    stateCombo = buildStateCombo();
    cepErrorLabel = new Label();
    cepErrorLabel.getStyleClass().add("form-error-label");
    cepErrorLabel.setVisible(false);
    cepErrorLabel.setManaged(false);

    setReadonly(streetField, true);
    setReadonly(neighborhoodField, true);
    setReadonly(cityField, true);
    setReadonlyCombo(stateCombo, true);

    getChildren().add(buildGrid());
    setupCepListener();
  }

  public void setDisableNextCallback(Runnable disable, Runnable enable) {
    this.disableNext = disable;
    this.enableNext = enable;
  }

  public boolean validate() {
    boolean valid = true;
    if (isEmpty(cepField)) {
      markError(cepField);
      valid = false;
    }
    if (isEmpty(streetField)) {
      markError(streetField);
      valid = false;
    }
    if (isEmpty(numberField)) {
      markError(numberField);
      valid = false;
    }
    if (isEmpty(neighborhoodField)) {
      markError(neighborhoodField);
      valid = false;
    }
    if (isEmpty(cityField)) {
      markError(cityField);
      valid = false;
    }
    if (stateCombo.getValue() == null) {
      markComboError(stateCombo);
      valid = false;
    }
    return valid;
  }

  public CreateAddressInput buildInput() {
    String complement = complementField.getText().trim();
    return new CreateAddressInput(cepField.getText().replaceAll("\\D", ""),
        streetField.getText().trim(), numberField.getText().trim(),
        complement.isEmpty() ? null : complement, neighborhoodField.getText().trim(),
        cityField.getText().trim(), stateCombo.getValue());
  }

  public void populate(Address address) {
    populating = true;
    cepField.setText(address.getCep());
    populating = false;
    setReadonly(streetField, false);
    streetField.setText(address.getAddress());
    setReadonly(streetField, true);
    numberField.setText(address.getNumber());
    complementField.setText(address.getComplement() != null ? address.getComplement() : "");
    setReadonly(neighborhoodField, false);
    neighborhoodField.setText(address.getNeighborhood());
    setReadonly(neighborhoodField, true);
    setReadonly(cityField, false);
    cityField.setText(address.getCity());
    setReadonly(cityField, true);
    setReadonlyCombo(stateCombo, false);
    stateCombo.setValue(address.getState());
    setReadonlyCombo(stateCombo, true);
  }

  public EditAddressInput buildEditInput(long id) {
    String complement = complementField.getText().trim();
    return new EditAddressInput(id, cepField.getText().replaceAll("\\D", ""),
        streetField.getText().trim(), numberField.getText().trim(),
        complement.isEmpty() ? null : complement, neighborhoodField.getText().trim(),
        cityField.getText().trim(), stateCombo.getValue());
  }

  private GridPane buildGrid() {
    GridPane grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(16);

    ColumnConstraints colWide = new ColumnConstraints();
    colWide.setHgrow(Priority.ALWAYS);
    colWide.setPercentWidth(50);
    ColumnConstraints colNarrow = new ColumnConstraints();
    colNarrow.setHgrow(Priority.ALWAYS);
    colNarrow.setPercentWidth(50);
    grid.getColumnConstraints().addAll(colWide, colNarrow);

    int row = 0;
    HBox cepRow = new HBox(10, cepField, cepErrorLabel);
    cepRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(cepField, Priority.NEVER);
    cepField.setPrefWidth(140);
    VBox cepBox = formField(bundle.getString("setup.field.cep"), cepRow);
    Label hint = new Label(bundle.getString("setup.field.cep.hint"));
    hint.getStyleClass().add("form-hint");
    cepBox.getChildren().add(hint);
    grid.add(cepBox, 0, row, 2, 1);
    row++;

    grid.add(formField(bundle.getString("setup.field.street"), streetField), 0, row);
    grid.add(formField(bundle.getString("setup.field.number"), numberField), 1, row);
    row++;

    grid.add(formField(bundle.getString("setup.field.complement"), complementField), 0, row, 2, 1);
    row++;

    ColumnConstraints c1 = new ColumnConstraints();
    c1.setHgrow(Priority.ALWAYS);
    c1.setPercentWidth(35);
    ColumnConstraints c2 = new ColumnConstraints();
    c2.setHgrow(Priority.ALWAYS);
    c2.setPercentWidth(40);
    ColumnConstraints c3 = new ColumnConstraints();
    c3.setHgrow(Priority.ALWAYS);
    c3.setPercentWidth(25);

    GridPane bottomRow = new GridPane();
    bottomRow.setHgap(16);
    bottomRow.getColumnConstraints().addAll(c1, c2, c3);
    bottomRow.add(formField(bundle.getString("setup.field.neighborhood"), neighborhoodField), 0, 0);
    bottomRow.add(formField(bundle.getString("setup.field.city"), cityField), 1, 0);
    bottomRow.add(formField(bundle.getString("setup.field.state"), stateCombo), 2, 0);

    grid.add(bottomRow, 0, row, 2, 1);

    return grid;
  }

  private void setupCepListener() {
    cepField.textProperty().addListener((obs, oldVal, newVal) -> {
      String digits = newVal.replaceAll("\\D", "");
      final boolean triggeredByPopulate = populating;
      if (digits.length() == 8) {
        if (triggeredByPopulate) {
          return;
        }
        cepErrorLabel.setVisible(false);
        cepErrorLabel.setManaged(false);
        disableNext.run();

        Task<SearchAddressByCepOutput> task = new Task<>() {
          @Override
          protected SearchAddressByCepOutput call() {
            return searchByCep.execute(new SearchAddressByCepInput(digits));
          }
        };

        task.setOnSucceeded(e -> {
          SearchAddressByCepOutput result = task.getValue();
          streetField.setText(result.result().address());
          neighborhoodField.setText(result.result().neighborhood());
          cityField.setText(result.result().city());
          stateCombo.setValue(result.result().state());
          setReadonly(streetField, true);
          setReadonly(neighborhoodField, true);
          setReadonly(cityField, true);
          setReadonlyCombo(stateCombo, true);
          enableNext.run();
        });

        task.setOnFailed(e -> {
          clearAutoFilledFields();
          cepErrorLabel.setText(bundle.getString("setup.error.cep_not_found"));
          cepErrorLabel.setVisible(true);
          cepErrorLabel.setManaged(true);
          enableNext.run();
        });

        new Thread(task).start();
      } else if (digits.length() < 8 && !triggeredByPopulate) {
        clearAutoFilledFields();
      }
    });
  }

  private void clearAutoFilledFields() {
    streetField.clear();
    neighborhoodField.clear();
    cityField.clear();
    stateCombo.setValue(null);
    setReadonly(streetField, false);
    setReadonly(neighborhoodField, false);
    setReadonly(cityField, false);
    setReadonlyCombo(stateCombo, false);
  }

  private void setReadonly(TextField field, boolean readonly) {
    field.setEditable(!readonly);
    field.getStyleClass().removeAll("form-input-readonly");
    if (readonly)
      field.getStyleClass().add("form-input-readonly");
  }

  private void setReadonlyCombo(ComboBox<?> combo, boolean readonly) {
    combo.setDisable(readonly);
    combo.getStyleClass().removeAll("form-input-readonly");
    if (readonly)
      combo.getStyleClass().add("form-input-readonly");
  }

  private ComboBox<BrazilianState> buildStateCombo() {
    ComboBox<BrazilianState> combo =
        new ComboBox<>(FXCollections.observableArrayList(BrazilianState.values()));
    combo.getStyleClass().add("form-combo");
    combo.setMaxWidth(Double.MAX_VALUE);
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
