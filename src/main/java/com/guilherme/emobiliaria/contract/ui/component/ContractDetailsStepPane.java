package com.guilherme.emobiliaria.contract.ui.component;

import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ResourceBundle;

public class ContractDetailsStepPane extends VBox {

  private final ResourceBundle bundle;

  private final DatePicker startDatePicker = new DatePicker();
  private final Spinner<Integer> durationSpinner =
      new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 12));
  private final TextField rentField = new TextField();
  private final Spinner<Integer> paymentDaySpinner =
      new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1));

  private final Label startDateError = new Label();
  private final Label rentError = new Label();

  public ContractDetailsStepPane(ResourceBundle bundle) {
    this.bundle = bundle;
    setSpacing(16);
    setPadding(new Insets(24, 24, 24, 24));
    buildLayout();
  }

  private void buildLayout() {
    startDatePicker.setPromptText("dd/MM/yyyy");
    startDatePicker.setMaxWidth(Double.MAX_VALUE);
    startDatePicker.getStyleClass().add("form-combo");

    durationSpinner.setEditable(true);
    durationSpinner.setMaxWidth(Double.MAX_VALUE);

    rentField.setPromptText("0,00");
    rentField.getStyleClass().add("form-input");

    paymentDaySpinner.setEditable(true);
    paymentDaySpinner.setMaxWidth(Double.MAX_VALUE);

    startDateError.getStyleClass().add("form-error-label");
    startDateError.setVisible(false);
    startDateError.setManaged(false);
    startDateError.setText(bundle.getString("contract.wizard.step4.error.start_date_required"));

    rentError.getStyleClass().add("form-error-label");
    rentError.setVisible(false);
    rentError.setManaged(false);
    rentError.setText(bundle.getString("contract.wizard.step4.error.rent_required"));

    GridPane grid = new GridPane();
    grid.setHgap(24);
    grid.setVgap(8);

    ColumnConstraints col1 = new ColumnConstraints();
    col1.setPercentWidth(70);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setPercentWidth(30);
    grid.getColumnConstraints().addAll(col1, col2);

    // Row 0: Start Date + Duration
    Label startLabel = new Label(bundle.getString("contract.wizard.step4.field.start_date"));
    startLabel.getStyleClass().add("form-label");
    Label durationLabel = new Label(bundle.getString("contract.wizard.step4.field.duration"));
    durationLabel.getStyleClass().add("form-label");
    grid.add(startLabel, 0, 0);
    grid.add(durationLabel, 1, 0);
    grid.add(startDatePicker, 0, 1);
    grid.add(durationSpinner, 1, 1);
    grid.add(startDateError, 0, 2);

    // Row 3: Rent + Payment Day
    Label rentLabel = new Label(bundle.getString("contract.wizard.step4.field.rent"));
    rentLabel.getStyleClass().add("form-label");
    Label payDayLabel = new Label(bundle.getString("contract.wizard.step4.field.payment_day"));
    payDayLabel.getStyleClass().add("form-label");
    grid.add(rentLabel, 0, 3);
    grid.add(payDayLabel, 1, 3);
    grid.add(rentField, 0, 4);
    grid.add(paymentDaySpinner, 1, 4);
    grid.add(rentError, 0, 5);

    getChildren().add(grid);
  }

  public void populate(LocalDate startDate, int durationMonths, int rentCents, int paymentDay) {
    startDatePicker.setValue(startDate);
    durationSpinner.getValueFactory().setValue(durationMonths);
    rentField.setText(String.format("%.2f", rentCents / 100.0).replace('.', ','));
    paymentDaySpinner.getValueFactory().setValue(paymentDay);
  }

  public LocalDate getStartDate() {
    return startDatePicker.getValue();
  }

  public int getDurationMonths() {
    return durationSpinner.getValue();
  }

  /** Returns rent in cents (integer). */
  public int getRentCents() {
    try {
      String text = rentField.getText().trim().replace("R$", "").replace(".", "").replace(",", ".").trim();
      return (int) (Double.parseDouble(text) * 100);
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  public int getPaymentDay() {
    return paymentDaySpinner.getValue();
  }

  public boolean validate() {
    boolean valid = true;

    boolean startOk = startDatePicker.getValue() != null;
    startDateError.setVisible(!startOk);
    startDateError.setManaged(!startOk);
    if (!startOk) valid = false;

    boolean rentOk = getRentCents() >= 0;
    rentError.setVisible(!rentOk);
    rentError.setManaged(!rentOk);
    if (!rentOk) valid = false;

    return valid;
  }
}
