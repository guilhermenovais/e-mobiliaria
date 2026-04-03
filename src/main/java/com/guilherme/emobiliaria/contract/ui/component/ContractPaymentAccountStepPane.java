package com.guilherme.emobiliaria.contract.ui.component;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import javafx.collections.FXCollections;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.ResourceBundle;

public class ContractPaymentAccountStepPane extends VBox {

  private final ResourceBundle bundle;

  private final ComboBox<PaymentAccount> accountCombo = new ComboBox<>();
  private final CheckBox newAccountCheckBox = new CheckBox();
  private final VBox newAccountForm = new VBox(8);
  private final TextField bankField = new TextField();
  private final TextField branchField = new TextField();
  private final TextField accountNumberField = new TextField();
  private final TextField pixField = new TextField();
  private final Label errorLabel = new Label();

  public ContractPaymentAccountStepPane(ResourceBundle bundle) {
    this.bundle = bundle;
    getStyleClass().add("wizard-step-pane");
    buildLayout();
  }

  private void buildLayout() {
    // Account combo
    Label comboLabel = new Label(bundle.getString("contract.wizard.step5.field.account"));
    comboLabel.getStyleClass().add("form-label");
    accountCombo.setPromptText(bundle.getString("contract.wizard.step5.field.account.prompt"));
    accountCombo.setMaxWidth(Double.MAX_VALUE);
    accountCombo.getStyleClass().add("form-combo");
    accountCombo.setConverter(new StringConverter<>() {
      @Override public String toString(PaymentAccount a) {
        return a == null ? "" : a.getBank() + " - Ag " + a.getBankBranch() + " - Conta " + a.getAccountNumber();
      }
      @Override public PaymentAccount fromString(String s) { return null; }
    });
    accountCombo.setCellFactory(lv -> new ListCell<>() {
      @Override protected void updateItem(PaymentAccount a, boolean empty) {
        super.updateItem(a, empty);
        if (empty || a == null) { setText(null); return; }
        setText(a.getBank() + " - Ag " + a.getBankBranch() + " - Conta " + a.getAccountNumber());
      }
    });
    accountCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, val) -> {
      errorLabel.setVisible(false);
      errorLabel.setManaged(false);
    });
    VBox comboGroup = new VBox(6, comboLabel, accountCombo);

    // New account checkbox
    newAccountCheckBox.setText(bundle.getString("contract.wizard.step5.new_account.toggle"));
    newAccountCheckBox.selectedProperty().addListener((obs, old, selected) -> {
      newAccountForm.setVisible(selected);
      newAccountForm.setManaged(selected);
      accountCombo.setDisable(selected);
    });

    // New account form
    bankField.setPromptText(bundle.getString("contract.wizard.step5.field.bank"));
    bankField.getStyleClass().add("form-input");
    branchField.setPromptText(bundle.getString("contract.wizard.step5.field.branch"));
    branchField.getStyleClass().add("form-input");
    accountNumberField.setPromptText(bundle.getString("contract.wizard.step5.field.account_number"));
    accountNumberField.getStyleClass().add("form-input");
    pixField.setPromptText(bundle.getString("contract.wizard.step5.field.pix"));
    pixField.getStyleClass().add("form-input");

    GridPane formGrid = new GridPane();
    formGrid.setHgap(24);
    formGrid.setVgap(8);
    ColumnConstraints c1 = new ColumnConstraints();
    c1.setPercentWidth(50);
    ColumnConstraints c2 = new ColumnConstraints();
    c2.setPercentWidth(50);
    formGrid.getColumnConstraints().addAll(c1, c2);

    Label bankLabel = new Label(bundle.getString("contract.wizard.step5.field.bank"));
    bankLabel.getStyleClass().add("form-label");
    Label branchLabel = new Label(bundle.getString("contract.wizard.step5.field.branch"));
    branchLabel.getStyleClass().add("form-label");
    Label accNumLabel = new Label(bundle.getString("contract.wizard.step5.field.account_number"));
    accNumLabel.getStyleClass().add("form-label");
    Label pixLabel = new Label(bundle.getString("contract.wizard.step5.field.pix"));
    pixLabel.getStyleClass().add("form-label");

    formGrid.add(bankLabel, 0, 0);
    formGrid.add(branchLabel, 1, 0);
    formGrid.add(bankField, 0, 1);
    formGrid.add(branchField, 1, 1);
    formGrid.add(accNumLabel, 0, 2);
    formGrid.add(pixLabel, 1, 2);
    formGrid.add(accountNumberField, 0, 3);
    formGrid.add(pixField, 1, 3);

    newAccountForm.getChildren().add(formGrid);
    newAccountForm.setVisible(false);
    newAccountForm.setManaged(false);

    // Error label
    errorLabel.getStyleClass().add("form-error-label");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    errorLabel.setText(bundle.getString("contract.wizard.step5.error.account_required"));

    VBox card = new VBox(20, comboGroup, newAccountCheckBox, newAccountForm);
    card.getStyleClass().add("wizard-section-card");
    getChildren().addAll(card, errorLabel);
  }

  public void setAccounts(List<PaymentAccount> accounts) {
    accountCombo.setItems(FXCollections.observableArrayList(accounts));
  }

  public void populate(PaymentAccount account) {
    accountCombo.getItems().stream()
        .filter(a -> a.getId().equals(account.getId()))
        .findFirst()
        .ifPresent(accountCombo.getSelectionModel()::select);
  }

  public boolean isNewAccount() {
    return newAccountCheckBox.isSelected();
  }

  public PaymentAccount getSelectedAccount() {
    return accountCombo.getValue();
  }

  public String getNewBank() { return bankField.getText().trim(); }
  public String getNewBranch() { return branchField.getText().trim(); }
  public String getNewAccountNumber() { return accountNumberField.getText().trim(); }
  public String getNewPixKey() { return pixField.getText().trim(); }

  public boolean validate() {
    boolean valid;
    if (newAccountCheckBox.isSelected()) {
      valid = !bankField.getText().isBlank()
          && !branchField.getText().isBlank()
          && !accountNumberField.getText().isBlank();
    } else {
      valid = accountCombo.getValue() != null;
    }
    errorLabel.setVisible(!valid);
    errorLabel.setManaged(!valid);
    return valid;
  }
}
