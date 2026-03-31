package com.guilherme.emobiliaria.config.ui.component;

import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.ResourceBundle;

public class PersonTypeSelectionPane extends VBox {

  private final VBox physicalCard;
  private final VBox juridicalCard;
  private final Label errorLabel;
  private PersonType selectedType = null;

  public PersonTypeSelectionPane(ResourceBundle bundle) {
    setSpacing(20);

    physicalCard = buildTypeCard(bundle.getString("setup.type.physical"),
        bundle.getString("setup.type.physical.description"), PersonType.PHYSICAL);
    juridicalCard = buildTypeCard(bundle.getString("setup.type.juridical"),
        bundle.getString("setup.type.juridical.description"), PersonType.JURIDICAL);

    HBox cards = new HBox(16, physicalCard, juridicalCard);
    HBox.setHgrow(physicalCard, Priority.ALWAYS);
    HBox.setHgrow(juridicalCard, Priority.ALWAYS);

    errorLabel = new Label(bundle.getString("setup.error.type_not_selected"));
    errorLabel.getStyleClass().add("form-error-label");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);

    getChildren().addAll(cards, errorLabel);
  }

  public PersonType getSelectedType() {
    return selectedType;
  }

  public boolean validate() {
    if (selectedType == null) {
      errorLabel.setVisible(true);
      errorLabel.setManaged(true);
      return false;
    }
    return true;
  }

  private VBox buildTypeCard(String title, String description, PersonType type) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("type-card-title");
    Label descLabel = new Label(description);
    descLabel.getStyleClass().add("type-card-description");
    descLabel.setWrapText(true);

    VBox card = new VBox(8, titleLabel, descLabel);
    card.getStyleClass().add("type-card");

    card.setOnMouseClicked(e -> {
      selectedType = type;
      errorLabel.setVisible(false);
      errorLabel.setManaged(false);
      VBox other = (type == PersonType.PHYSICAL) ? juridicalCard : physicalCard;
      applyCardSelection(card, other);
    });

    return card;
  }

  private void applyCardSelection(VBox selected, VBox deselected) {
    selected.getStyleClass().removeAll("type-card", "type-card-selected");
    selected.getStyleClass().add("type-card-selected");
    deselected.getStyleClass().removeAll("type-card-selected");
    if (!deselected.getStyleClass().contains("type-card")) {
      deselected.getStyleClass().add("type-card");
    }
  }
}
