package com.guilherme.emobiliaria.shared.update;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.text.MessageFormat;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.concurrent.FutureTask;

public class UpdateProgressWindow {

  private final ResourceBundle bundle;

  private Stage stage;
  private Label statusLabel;
  private Label errorLabel;
  private Button closeButton;

  public UpdateProgressWindow(ResourceBundle bundle) {
    this.bundle = bundle;
  }

  public boolean confirmUpdate(String currentVersion, String newVersion) {
    FutureTask<Boolean> task = new FutureTask<>(() -> {
      Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
      alert.setTitle(bundle.getString("update.confirm.title"));
      alert.setHeaderText(bundle.getString("update.confirm.header"));
      alert.setContentText(
          MessageFormat.format(bundle.getString("update.confirm.content"),
              currentVersion, newVersion));
      Optional<ButtonType> result = alert.showAndWait();
      return result.isPresent() && result.get() == ButtonType.OK;
    });

    if (Platform.isFxApplicationThread()) {
      task.run();
    } else {
      Platform.runLater(task);
    }

    try {
      return task.get();
    } catch (Exception e) {
      return false;
    }
  }

  public void showProgress() {
    Platform.runLater(this::buildAndShowStage);
  }

  private void buildAndShowStage() {
    stage = new Stage(StageStyle.UTILITY);
    stage.setTitle(bundle.getString("update.progress.title"));
    stage.setResizable(false);

    VBox root = new VBox(12);
    root.setPadding(new Insets(24));
    root.setPrefWidth(420);

    Label titleLabel = new Label(bundle.getString("update.progress.title"));
    titleLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");

    statusLabel = new Label();
    statusLabel.setWrapText(true);

    ProgressBar progressBar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
    progressBar.setMaxWidth(Double.MAX_VALUE);

    errorLabel = new Label();
    errorLabel.setWrapText(true);
    errorLabel.setStyle("-fx-text-fill: #C0392B;");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);

    Region spacer = new Region();
    HBox.setHgrow(spacer, Priority.ALWAYS);

    closeButton = new Button(bundle.getString("update.close"));
    closeButton.setDisable(true);
    closeButton.setOnAction(e -> stage.close());

    HBox footer = new HBox(spacer, closeButton);
    footer.setAlignment(Pos.CENTER_RIGHT);

    root.getChildren().addAll(titleLabel, statusLabel, progressBar, errorLabel, footer);

    stage.setScene(new Scene(root));
    stage.show();
  }

  public void setStatus(String messageKey) {
    Platform.runLater(() -> {
      if (statusLabel != null) {
        statusLabel.setText(bundle.getString(messageKey));
      }
    });
  }

  public void markComplete() {
    Platform.runLater(() -> {
      if (closeButton != null) {
        closeButton.setDisable(false);
        closeButton.setOnAction(e -> System.exit(0));
      }
    });
  }

  public void showError(String message) {
    Platform.runLater(() -> {
      if (errorLabel != null) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
      }
      if (closeButton != null) {
        closeButton.setDisable(false);
      }
    });
  }
}
