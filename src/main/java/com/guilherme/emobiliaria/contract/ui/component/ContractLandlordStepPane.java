package com.guilherme.emobiliaria.contract.ui.component;

import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.RadioButton;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.ResourceBundle;

public class ContractLandlordStepPane extends VBox {

  private final ResourceBundle bundle;
  private final Person defaultLandlord;

  private final RadioButton useDefaultRadio = new RadioButton();
  private final RadioButton selectOtherRadio = new RadioButton();
  private final VBox defaultPanel = new VBox(6);
  private final VBox comboGroup = new VBox(6);
  private final ComboBox<Person> landlordCombo = new ComboBox<>();
  private final Label errorLabel = new Label();

  public ContractLandlordStepPane(ResourceBundle bundle, Person defaultLandlord) {
    this.bundle = bundle;
    this.defaultLandlord = defaultLandlord;
    getStyleClass().add("wizard-step-pane");
    buildLayout();
  }

  private void buildLayout() {
    ToggleGroup group = new ToggleGroup();
    useDefaultRadio.setText(bundle.getString("contract.wizard.step2.radio.default"));
    useDefaultRadio.setToggleGroup(group);
    selectOtherRadio.setText(bundle.getString("contract.wizard.step2.radio.other"));
    selectOtherRadio.setToggleGroup(group);

    // Default panel
    Label defaultHeader = new Label(bundle.getString("contract.wizard.step2.default_panel.header"));
    defaultHeader.getStyleClass().add("wizard-detail-panel-header");
    Label defaultNameRow = new Label(displayName(defaultLandlord));
    defaultNameRow.getStyleClass().add("wizard-detail-panel-row");
    String docLine = defaultLandlord instanceof PhysicalPerson pp
        ? "CPF: " + pp.getCpf()
        : defaultLandlord instanceof JuridicalPerson jp ? "CNPJ: " + jp.getCnpj() : "";
    Label defaultDocRow = new Label(docLine);
    defaultDocRow.getStyleClass().add("wizard-detail-panel-row");
    defaultPanel.getStyleClass().add("wizard-detail-panel");
    defaultPanel.getChildren().addAll(defaultHeader, defaultNameRow, defaultDocRow);

    // Other combo
    Label comboLabel = new Label(bundle.getString("contract.wizard.step2.field.landlord"));
    comboLabel.getStyleClass().add("form-label");
    landlordCombo.setPromptText(bundle.getString("contract.wizard.step2.field.landlord.prompt"));
    landlordCombo.setMaxWidth(Double.MAX_VALUE);
    landlordCombo.getStyleClass().add("form-combo");
    landlordCombo.setConverter(new StringConverter<>() {
      @Override public String toString(Person p) { return p == null ? "" : displayName(p); }
      @Override public Person fromString(String s) { return null; }
    });
    landlordCombo.setCellFactory(lv -> new ListCell<>() {
      @Override protected void updateItem(Person p, boolean empty) {
        super.updateItem(p, empty);
        setText(empty || p == null ? null : displayName(p));
      }
    });
    comboGroup.getChildren().addAll(comboLabel, landlordCombo);
    comboGroup.setVisible(false);
    comboGroup.setManaged(false);

    // Error label
    errorLabel.getStyleClass().add("form-error-label");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    errorLabel.setText(bundle.getString("contract.wizard.step2.error.landlord_required"));

    // Toggle listeners
    useDefaultRadio.selectedProperty().addListener((obs, old, selected) -> {
      defaultPanel.setVisible(selected);
      defaultPanel.setManaged(selected);
      comboGroup.setVisible(!selected);
      comboGroup.setManaged(!selected);
      errorLabel.setVisible(false);
    });

    // Default selection
    if (defaultLandlord != null) {
      useDefaultRadio.setSelected(true);
    } else {
      selectOtherRadio.setSelected(true);
      useDefaultRadio.setDisable(true);
    }

    VBox radioGroup = new VBox(10, useDefaultRadio, selectOtherRadio);
    VBox card = new VBox(20, radioGroup, defaultPanel, comboGroup);
    card.getStyleClass().add("wizard-section-card");
    getChildren().addAll(card, errorLabel);
  }

  public void setAllPersons(List<Person> persons) {
    landlordCombo.setItems(FXCollections.observableArrayList(persons));
  }

  public void populate(Person landlord) {
    if (defaultLandlord != null && landlord.getId().equals(defaultLandlord.getId())) {
      useDefaultRadio.setSelected(true);
    } else {
      selectOtherRadio.setSelected(true);
      landlordCombo.getItems().stream()
          .filter(p -> p.getId().equals(landlord.getId()))
          .findFirst()
          .ifPresent(landlordCombo.getSelectionModel()::select);
    }
  }

  public Person getSelectedLandlord() {
    if (useDefaultRadio.isSelected()) return defaultLandlord;
    return landlordCombo.getValue();
  }

  public boolean validate() {
    boolean valid;
    if (useDefaultRadio.isSelected()) {
      valid = defaultLandlord != null;
    } else {
      valid = landlordCombo.getValue() != null;
    }
    errorLabel.setVisible(!valid);
    errorLabel.setManaged(!valid);
    return valid;
  }

  static String displayName(Person p) {
    if (p instanceof PhysicalPerson pp) return pp.getName() + " - CPF " + pp.getCpf();
    if (p instanceof JuridicalPerson jp) return jp.getCorporateName() + " - CNPJ " + jp.getCnpj();
    return p == null ? "" : "ID " + p.getId();
  }
}
