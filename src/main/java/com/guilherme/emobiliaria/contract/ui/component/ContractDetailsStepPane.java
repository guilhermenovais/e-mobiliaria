package com.guilherme.emobiliaria.contract.ui.component;

import javafx.geometry.Insets;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextFormatter;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.time.LocalDate;
import java.util.Locale;
import java.util.ResourceBundle;

public class ContractDetailsStepPane extends VBox {

  private static final int MAX_RENT_DIGITS = 12;

  private final ResourceBundle bundle;

  private final TextField purposeField = new TextField();
  private final DatePicker startDatePicker = new DatePicker();
  private final Spinner<Integer> durationSpinner =
      new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 120, 12));
  private final TextField rentField = new TextField();
  private final Spinner<Integer> paymentDaySpinner =
      new Spinner<>(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 31, 1));

  private final Label purposeError = new Label();
  private final Label startDateError = new Label();
  private final Label rentError = new Label();

  public ContractDetailsStepPane(ResourceBundle bundle) {
    this.bundle = bundle;
    getStyleClass().add("wizard-step-pane");
    buildLayout();
  }

  private void buildLayout() {
    purposeField.setPromptText(bundle.getString("contract.wizard.step4.field.purpose"));
    purposeField.getStyleClass().add("form-input");
    purposeField.setMaxWidth(Double.MAX_VALUE);
    purposeField.textProperty().addListener((obs, old, text) -> {
      if (text != null && text.length() > 100) {
        purposeField.setText(old);
      }
    });

    startDatePicker.setPromptText("dd/MM/yyyy");
    startDatePicker.setMaxWidth(Double.MAX_VALUE);
    startDatePicker.getStyleClass().add("form-combo");

    durationSpinner.setEditable(true);
    durationSpinner.setMaxWidth(Double.MAX_VALUE);

    rentField.setPromptText("0,00");
    rentField.getStyleClass().add("form-input");
    rentField.setMaxWidth(Double.MAX_VALUE);
    configureRentMask();

    paymentDaySpinner.setEditable(true);
    paymentDaySpinner.setMaxWidth(Double.MAX_VALUE);

    purposeError.getStyleClass().add("form-error-label");
    purposeError.setVisible(false);
    purposeError.setManaged(false);
    purposeError.setText(bundle.getString("contract.wizard.step4.error.purpose_required"));

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

    // Row 0: Purpose (full width)
    Label purposeLabel = new Label(bundle.getString("contract.wizard.step4.field.purpose"));
    purposeLabel.getStyleClass().add("form-label");
    grid.add(purposeLabel, 0, 0, 2, 1);
    grid.add(purposeField, 0, 1, 2, 1);
    grid.add(purposeError, 0, 2, 2, 1);

    // Row 3: Start Date + Duration
    Label startLabel = new Label(bundle.getString("contract.wizard.step4.field.start_date"));
    startLabel.getStyleClass().add("form-label");
    Label durationLabel = new Label(bundle.getString("contract.wizard.step4.field.duration"));
    durationLabel.getStyleClass().add("form-label");
    grid.add(startLabel, 0, 3);
    grid.add(durationLabel, 1, 3);
    grid.add(startDatePicker, 0, 4);
    grid.add(durationSpinner, 1, 4);
    grid.add(startDateError, 0, 5);

    // Row 6: Rent + Payment Day
    Label rentLabel = new Label(bundle.getString("contract.wizard.step4.field.rent"));
    rentLabel.getStyleClass().add("form-label");
    Label payDayLabel = new Label(bundle.getString("contract.wizard.step4.field.payment_day"));
    payDayLabel.getStyleClass().add("form-label");
    grid.add(rentLabel, 0, 6);
    grid.add(payDayLabel, 1, 6);
    grid.add(rentField, 0, 7);
    grid.add(paymentDaySpinner, 1, 7);
    grid.add(rentError, 0, 8);

    VBox card = new VBox(20, grid);
    card.setPadding(new Insets(24, 24, 24, 24));
    card.getStyleClass().add("wizard-section-card");
    getChildren().add(card);
  }

  public void populate(LocalDate startDate, int durationMonths, int rentCents, int paymentDay,
      String purpose) {
    purposeField.setText(purpose);
    startDatePicker.setValue(startDate);
    durationSpinner.getValueFactory().setValue(durationMonths);
    rentField.setText(formatCents(rentCents));
    paymentDaySpinner.getValueFactory().setValue(paymentDay);
  }

  public String getPurpose() {
    return purposeField.getText() == null ? "" : purposeField.getText().trim();
  }

  public LocalDate getStartDate() {
    return startDatePicker.getValue();
  }

  public int getDurationMonths() {
    return durationSpinner.getValue();
  }

  /** Returns rent in cents (integer). */
  public int getRentCents() {
    String text = rentField.getText();
    if (text == null || text.isBlank()) {
      return -1;
    }

    try {
      String digits = text.replaceAll("\\D", "");
      if (digits.isEmpty()) {
        return -1;
      }

      long cents = Long.parseLong(digits);
      if (cents > Integer.MAX_VALUE) {
        return -1;
      }
      return (int) cents;
    } catch (NumberFormatException e) {
      return -1;
    }
  }

  private void configureRentMask() {
    rentField.setTextFormatter(new TextFormatter<>(change -> {
      String digits = change.getControlNewText().replaceAll("\\D", "");
      if (digits.length() > MAX_RENT_DIGITS) {
        digits = digits.substring(0, MAX_RENT_DIGITS);
      }

      String formatted = digits.isEmpty() ? "" : formatCents(Long.parseLong(digits));
      change.setText(formatted);
      change.setRange(0, change.getControlText().length());
      change.setCaretPosition(formatted.length());
      change.setAnchor(formatted.length());
      return change;
    }));
  }

  private String formatCents(long cents) {
    NumberFormat formatter = NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
    formatter.setMinimumFractionDigits(2);
    formatter.setMaximumFractionDigits(2);
    return formatter.format(cents / 100.0);
  }

  public int getPaymentDay() {
    return paymentDaySpinner.getValue();
  }

  public boolean validate() {
    boolean valid = true;

    boolean purposeOk = !getPurpose().isBlank();
    purposeError.setVisible(!purposeOk);
    purposeError.setManaged(!purposeOk);
    if (!purposeOk) valid = false;

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
