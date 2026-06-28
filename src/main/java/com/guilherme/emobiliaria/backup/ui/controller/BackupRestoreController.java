package com.guilherme.emobiliaria.backup.ui.controller;

import com.guilherme.emobiliaria.backup.application.input.CreateBackupInput;
import com.guilherme.emobiliaria.backup.application.input.ListDriveBackupsInput;
import com.guilherme.emobiliaria.backup.application.input.RestoreBackupInput;
import com.guilherme.emobiliaria.backup.application.output.DetectDrivesOutput;
import com.guilherme.emobiliaria.backup.application.output.ListDriveBackupsOutput;
import com.guilherme.emobiliaria.backup.application.usecase.CreateBackupInteractor;
import com.guilherme.emobiliaria.backup.application.usecase.DetectDrivesInteractor;
import com.guilherme.emobiliaria.backup.application.usecase.ListDriveBackupsInteractor;
import com.guilherme.emobiliaria.backup.application.usecase.RestoreBackupInteractor;
import com.guilherme.emobiliaria.backup.domain.entity.BackupFile;
import com.guilherme.emobiliaria.backup.domain.entity.RemovableDrive;
import com.guilherme.emobiliaria.shared.exception.InsufficientSpaceException;
import com.guilherme.emobiliaria.shared.exception.NoBackupsFoundException;
import com.guilherme.emobiliaria.shared.exception.NoDrivesFoundException;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.event.Event;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class BackupRestoreController {

  private static final Logger log = LoggerFactory.getLogger(BackupRestoreController.class);

  private final DetectDrivesInteractor detectDrives;
  private final ListDriveBackupsInteractor listBackups;
  private final CreateBackupInteractor createBackup;
  private final RestoreBackupInteractor restoreBackup;
  private final ResourceBundle bundle;

  @Inject
  public BackupRestoreController(DetectDrivesInteractor detectDrives,
      ListDriveBackupsInteractor listBackups, CreateBackupInteractor createBackup,
      RestoreBackupInteractor restoreBackup, ResourceBundle bundle) {
    this.detectDrives = detectDrives;
    this.listBackups = listBackups;
    this.createBackup = createBackup;
    this.restoreBackup = restoreBackup;
    this.bundle = bundle;
  }

  public VBox buildSection() {
    VBox section = new VBox(8);
    section.getStyleClass().add("config-section");

    Label titleLabel = new Label(bundle.getString("backup.section.title"));
    titleLabel.getStyleClass().add("config-section-title");

    Label descLabel = new Label(bundle.getString("backup.section.description"));
    descLabel.getStyleClass().add("config-section-desc");
    descLabel.setWrapText(true);

    Button backupButton = new Button(bundle.getString("backup.button.backup"));
    backupButton.getStyleClass().add("btn-primary");
    backupButton.setOnAction(e -> onBackup());

    Button restoreButton = new Button(bundle.getString("backup.button.restore"));
    restoreButton.getStyleClass().add("btn-secondary");
    restoreButton.setOnAction(e -> onRestore());

    HBox buttonRow = new HBox(8, backupButton, restoreButton);
    buttonRow.setAlignment(Pos.CENTER_LEFT);
    VBox.setMargin(buttonRow, new Insets(8, 0, 0, 0));

    section.getChildren().addAll(titleLabel, descLabel, buttonRow);
    return section;
  }

  private void onBackup() {
    List<RemovableDrive> drives = detectDrivesOrShowError();
    if (drives == null)
      return;

    RemovableDrive drive;
    if (drives.size() == 1) {
      RemovableDrive candidate = drives.get(0);
      Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
      confirm.setTitle(bundle.getString("backup.drive.select.title"));
      confirm.setHeaderText(candidate.label());
      confirm.setContentText(candidate.path().toString());
      Optional<ButtonType> result = confirm.showAndWait();
      if (result.isEmpty() || result.get() != ButtonType.OK)
        return;
      drive = candidate;
    } else {
      Optional<RemovableDrive> selected = selectDriveFromList(drives);
      if (selected.isEmpty())
        return;
      drive = selected.get();
    }

    Stage progress = buildProgressStage(bundle.getString("backup.progress.backing_up"));
    progress.show();

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        createBackup.execute(new CreateBackupInput(drive.path()));
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      progress.close();
      Alert ok = new Alert(Alert.AlertType.INFORMATION);
      ok.setHeaderText(null);
      ok.setContentText(bundle.getString("backup.success"));
      ok.showAndWait();
    });

    task.setOnFailed(e -> {
      progress.close();
      Throwable cause = task.getException();
      log.error("Backup failed", cause);
      if (cause instanceof InsufficientSpaceException) {
        showError(bundle.getString("backup.error.insufficient_space"));
      } else {
        showError(bundle.getString("backup.error.failed"));
      }
    });

    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
  }

  private void onRestore() {
    List<RemovableDrive> drives = detectDrivesOrShowError();
    if (drives == null)
      return;

    RemovableDrive drive;
    if (drives.size() == 1) {
      drive = drives.get(0);
    } else {
      Optional<RemovableDrive> selected = selectDriveFromList(drives);
      if (selected.isEmpty())
        return;
      drive = selected.get();
    }

    ListDriveBackupsOutput backupsOutput;
    try {
      backupsOutput = listBackups.execute(new ListDriveBackupsInput(drive.path()));
    } catch (NoBackupsFoundException e) {
      showError(bundle.getString("backup.restore.no_backups"));
      return;
    }

    Optional<BackupFile> selectedBackup = showBackupListDialog(backupsOutput.backups());
    if (selectedBackup.isEmpty())
      return;

    BackupFile backup = selectedBackup.get();
    if (!confirmRestoreWord())
      return;

    Stage progress = buildProgressStage(bundle.getString("backup.progress.restoring"));
    progress.show();

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        restoreBackup.execute(new RestoreBackupInput(backup.path()));
        return null;
      }
    };

    task.setOnFailed(e -> {
      progress.close();
      log.error("Restore failed", task.getException());
      showError(bundle.getString("backup.error.failed"));
    });

    Thread thread = new Thread(task);
    thread.setDaemon(true);
    thread.start();
  }

  private List<RemovableDrive> detectDrivesOrShowError() {
    try {
      DetectDrivesOutput output = detectDrives.execute();
      return output.drives();
    } catch (NoDrivesFoundException e) {
      showError(bundle.getString("backup.error.no_drives"));
      return null;
    }
  }

  private Optional<RemovableDrive> selectDriveFromList(List<RemovableDrive> drives) {
    ListView<RemovableDrive> listView = new ListView<>(FXCollections.observableArrayList(drives));
    listView.getSelectionModel().selectFirst();
    listView.setPrefHeight(150);

    Button selectButton = new Button(bundle.getString("backup.drive.select.title"));
    selectButton.getStyleClass().add("btn-primary");
    Button cancelButton = new Button("Cancel");

    HBox buttons = new HBox(8, selectButton, cancelButton);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    VBox content =
        new VBox(8, new Label(bundle.getString("backup.drive.select.title")), listView, buttons);
    content.setPadding(new Insets(16));

    Stage stage = new Stage(StageStyle.UTILITY);
    stage.setTitle(bundle.getString("backup.drive.select.title"));
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setScene(new Scene(content, 320, 240));
    stage.setResizable(false);

    RemovableDrive[] selected = {null};
    selectButton.setOnAction(e -> {
      selected[0] = listView.getSelectionModel().getSelectedItem();
      stage.close();
    });
    cancelButton.setOnAction(e -> stage.close());
    stage.setOnCloseRequest(e -> stage.close());

    stage.showAndWait();
    return Optional.ofNullable(selected[0]);
  }

  private Optional<BackupFile> showBackupListDialog(List<BackupFile> backups) {
    ListView<BackupFile> listView = new ListView<>(FXCollections.observableArrayList(backups));
    listView.getSelectionModel().selectFirst();
    listView.setPrefHeight(200);

    Button restoreBtn = new Button(bundle.getString("backup.button.restore"));
    restoreBtn.getStyleClass().add("btn-primary");
    Button cancelBtn = new Button("Cancel");

    HBox buttons = new HBox(8, restoreBtn, cancelBtn);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    VBox content =
        new VBox(8, new Label(bundle.getString("backup.restore.confirm.title")), listView, buttons);
    content.setPadding(new Insets(16));

    Stage stage = new Stage(StageStyle.UTILITY);
    stage.setTitle(bundle.getString("backup.restore.confirm.title"));
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setScene(new Scene(content, 380, 300));
    stage.setResizable(false);

    BackupFile[] selected = {null};
    restoreBtn.setOnAction(e -> {
      selected[0] = listView.getSelectionModel().getSelectedItem();
      stage.close();
    });
    cancelBtn.setOnAction(e -> stage.close());
    stage.setOnCloseRequest(e -> stage.close());

    stage.showAndWait();
    return Optional.ofNullable(selected[0]);
  }

  private boolean confirmRestoreWord() {
    String requiredWord = bundle.getString("backup.restore.confirm.word");

    Label messageLabel = new Label(bundle.getString("backup.restore.confirm.message"));
    messageLabel.setWrapText(true);

    TextField wordField = new TextField();

    Label errorLabel = new Label(bundle.getString("backup.restore.confirm.error"));
    errorLabel.setStyle("-fx-text-fill: #C0392B;");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);

    Button confirmBtn = new Button(bundle.getString("backup.button.restore"));
    confirmBtn.getStyleClass().add("btn-primary");
    Button cancelBtn = new Button("Cancel");

    HBox buttons = new HBox(8, confirmBtn, cancelBtn);
    buttons.setAlignment(Pos.CENTER_RIGHT);

    VBox content = new VBox(8, messageLabel, wordField, errorLabel, buttons);
    content.setPadding(new Insets(16));
    content.setPrefWidth(380);

    Stage stage = new Stage(StageStyle.UTILITY);
    stage.setTitle(bundle.getString("backup.restore.confirm.title"));
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setScene(new Scene(content));
    stage.sizeToScene();
    stage.setResizable(false);
    stage.setOnCloseRequest(e -> stage.close());

    boolean[] confirmed = {false};
    confirmBtn.setOnAction(e -> {
      if (requiredWord.equals(wordField.getText().trim())) {
        confirmed[0] = true;
        stage.close();
      } else {
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
        stage.sizeToScene();
      }
    });
    cancelBtn.setOnAction(e -> stage.close());

    stage.showAndWait();
    return confirmed[0];
  }

  private Stage buildProgressStage(String message) {
    Label label = new Label(message);
    label.setStyle("-fx-font-size: 13px;");

    ProgressBar bar = new ProgressBar(ProgressBar.INDETERMINATE_PROGRESS);
    bar.setMaxWidth(Double.MAX_VALUE);

    VBox root = new VBox(12, label, bar);
    root.setPadding(new Insets(24));
    root.setPrefWidth(320);

    Stage stage = new Stage(StageStyle.UTILITY);
    stage.setResizable(false);
    stage.initModality(Modality.APPLICATION_MODAL);
    stage.setOnCloseRequest(Event::consume);
    stage.setScene(new Scene(root));
    return stage;
  }

  private void showError(String message) {
    Alert alert = new Alert(Alert.AlertType.ERROR);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
  }
}
