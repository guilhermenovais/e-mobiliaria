package com.guilherme.emobiliaria.contract.ui.component;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.util.List;
import java.util.ResourceBundle;

public class ContractPropertyStepPane extends VBox {

  private final ResourceBundle bundle;

  private final ComboBox<Property> propertyCombo = new ComboBox<>();
  private final Label selectedHeader = new Label();
  private final Label addressRow = new Label();
  private final Label typeRow = new Label();
  private final Label utilitiesRow = new Label();
  private final VBox detailPanel = new VBox(6);
  private final Label errorLabel = new Label();

  private ObservableList<Property> allProperties = FXCollections.observableArrayList();

  public ContractPropertyStepPane(ResourceBundle bundle) {
    this.bundle = bundle;
    getStyleClass().add("wizard-step-pane");
    buildLayout();
  }

  private void buildLayout() {
    // Combo label
    Label comboLabel = new Label(bundle.getString("contract.wizard.step1.field.property"));
    comboLabel.getStyleClass().add("form-label");

    // Combo
    propertyCombo.setPromptText(bundle.getString("contract.wizard.step1.field.property.prompt"));
    propertyCombo.setMaxWidth(Double.MAX_VALUE);
    propertyCombo.setEditable(true);
    propertyCombo.getStyleClass().add("form-combo");

    StringConverter<Property> converter = new StringConverter<>() {
      @Override
      public String toString(Property p) {
        return p == null ? "" : p.getName();
      }

      @Override
      public Property fromString(String s) {
        return null;
      }
    };
    propertyCombo.setConverter(converter);

    propertyCombo.setCellFactory(lv -> new ListCell<>() {
      @Override
      protected void updateItem(Property p, boolean empty) {
        super.updateItem(p, empty);
        setText(empty || p == null ? null : p.getName());
      }
    });

    // Filter on typing
    propertyCombo.getEditor().textProperty().addListener((obs, old, text) -> {
      if (propertyCombo.getValue() != null && converter.toString(propertyCombo.getValue())
          .equals(text))
        return;
      String lower = text == null ? "" : text.toLowerCase();
      FilteredList<Property> filtered =
          allProperties.filtered(p -> p.getName().toLowerCase().contains(lower));
      propertyCombo.setItems(filtered);
      if (!filtered.isEmpty())
        propertyCombo.show();
    });

    propertyCombo.getEditor().setOnMouseClicked(e -> {
      if (propertyCombo.getValue() != null) {
        propertyCombo.getEditor().clear();
      }
      propertyCombo.setItems(allProperties);
      if (!allProperties.isEmpty())
        propertyCombo.show();
    });

    propertyCombo.getSelectionModel().selectedItemProperty().addListener((obs, old, selected) -> {
      showDetails(selected);
      errorLabel.setVisible(false);
    });

    // Detail panel
    selectedHeader.getStyleClass().add("wizard-detail-panel-header");
    addressRow.getStyleClass().add("wizard-detail-panel-row");
    typeRow.getStyleClass().add("wizard-detail-panel-row");
    utilitiesRow.getStyleClass().add("wizard-detail-panel-row");
    detailPanel.getStyleClass().add("wizard-detail-panel");
    detailPanel.getChildren().addAll(selectedHeader, addressRow, typeRow, utilitiesRow);
    detailPanel.setVisible(false);
    detailPanel.setManaged(false);

    // Error label
    errorLabel.getStyleClass().add("form-error-label");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
    errorLabel.setText(bundle.getString("contract.wizard.step1.error.property_required"));

    VBox comboGroup = new VBox(8, comboLabel, propertyCombo);
    VBox card = new VBox(20, comboGroup, detailPanel);
    card.getStyleClass().add("wizard-section-card");
    getChildren().addAll(card, errorLabel);
  }

  private void showDetails(Property property) {
    if (property == null) {
      detailPanel.setVisible(false);
      detailPanel.setManaged(false);
      return;
    }
    selectedHeader.setText(bundle.getString("contract.wizard.step1.selected.header"));
    Address addr = property.getAddress();
    if (addr != null) {
      String address =
          addr.getAddress() + ", " + addr.getNumber() + (addr.getComplement() != null && !addr.getComplement()
              .isBlank() ?
              " " + addr.getComplement() :
              "") + " - " + addr.getNeighborhood() + ", " + addr.getCity() + "/" + (addr.getState() != null ?
              addr.getState().name() :
              "");
      addressRow.setText(bundle.getString("contract.wizard.step1.field.address") + " " + address);
    }
    typeRow.setText(
        bundle.getString("contract.wizard.step1.field.type") + " " + property.getType());
    utilitiesRow.setText(
        "CEMIG: " + property.getCemig() + " | COPASA: " + property.getCopasa() + " | IPTU: " + property.getIptu());
    detailPanel.setVisible(true);
    detailPanel.setManaged(true);
  }

  public void setProperties(List<Property> properties) {
    allProperties = FXCollections.observableArrayList(properties);
    propertyCombo.setItems(allProperties);
  }

  public void populate(Property property) {
    propertyCombo.getSelectionModel().select(property);
  }

  public Property getSelectedProperty() {
    return propertyCombo.getValue();
  }

  public boolean validate() {
    boolean valid = propertyCombo.getValue() != null;
    errorLabel.setVisible(!valid);
    errorLabel.setManaged(!valid);
    return valid;
  }
}
