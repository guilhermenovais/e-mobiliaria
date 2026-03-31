package com.guilherme.emobiliaria.shared.ui.component;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.ArrayList;
import java.util.List;

public class WizardStepperBar extends HBox {

  private final List<Label> dots = new ArrayList<>();
  private final List<Region> connectors = new ArrayList<>();
  private final List<Label> labels = new ArrayList<>();

  public WizardStepperBar(List<String> labelTexts) {
    for (int i = 0; i < labelTexts.size(); i++) {
      Label dot = new Label(String.valueOf(i + 1));
      dot.getStyleClass().add("stepper-dot");
      dot.setMinSize(28, 28);
      dot.setMaxSize(28, 28);
      dot.setAlignment(Pos.CENTER);

      Label label = new Label(labelTexts.get(i));
      label.getStyleClass().add("stepper-label");

      VBox dotBox = new VBox(5, dot, label);
      dotBox.setAlignment(Pos.CENTER);

      dots.add(dot);
      labels.add(label);
      getChildren().add(dotBox);

      if (i < labelTexts.size() - 1) {
        Region connector = new Region();
        connector.getStyleClass().add("stepper-connector");
        HBox.setHgrow(connector, Priority.ALWAYS);
        connector.setTranslateY(-8);
        connectors.add(connector);
        getChildren().add(connector);
      }
    }
  }

  public void setCurrentStep(int step) {
    for (int i = 0; i < dots.size(); i++) {
      Label dot = dots.get(i);
      Label label = labels.get(i);
      dot.getStyleClass().removeAll("stepper-dot", "stepper-dot-active", "stepper-dot-completed");
      label.getStyleClass().removeAll("stepper-label", "stepper-label-active");

      if (i + 1 < step) {
        dot.setText("✓");
        dot.getStyleClass().add("stepper-dot-completed");
        label.getStyleClass().add("stepper-label-active");
      } else if (i + 1 == step) {
        dot.setText(String.valueOf(i + 1));
        dot.getStyleClass().add("stepper-dot-active");
        label.getStyleClass().add("stepper-label-active");
      } else {
        dot.setText(String.valueOf(i + 1));
        dot.getStyleClass().add("stepper-dot");
        label.getStyleClass().add("stepper-label");
      }
    }
    for (int i = 0; i < connectors.size(); i++) {
      Region connector = connectors.get(i);
      connector.getStyleClass().removeAll("stepper-connector", "stepper-connector-completed");
      if (i + 1 < step) {
        connector.getStyleClass().add("stepper-connector-completed");
      } else {
        connector.getStyleClass().add("stepper-connector");
      }
    }
  }
}
