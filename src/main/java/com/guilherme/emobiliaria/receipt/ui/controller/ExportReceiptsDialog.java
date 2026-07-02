package com.guilherme.emobiliaria.receipt.ui.controller;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Dialog;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;

import java.io.File;
import java.nio.file.Path;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicReference;

public class ExportReceiptsDialog extends Dialog<ExportReceiptsDialog.Result> {

  private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MM/yyyy");


  public ExportReceiptsDialog(List<YearMonth> months, ResourceBundle bundle) {
    setTitle(bundle.getString("receipt.export.dialog.title"));
    setHeaderText(null);

    Label monthLabel = new Label(bundle.getString("receipt.export.dialog.month_label"));
    ComboBox<YearMonth> monthComboBox = new ComboBox<>();
    monthComboBox.getItems().setAll(months);
    monthComboBox.setCellFactory(lv -> monthCell());
    monthComboBox.setButtonCell(monthCell());

    Label folderLabel = new Label(bundle.getString("receipt.export.dialog.folder_label"));
    Label folderPathLabel = new Label(bundle.getString("receipt.export.dialog.no_folder_selected"));
    Button chooseFolderButton =
        new Button(bundle.getString("receipt.export.dialog.choose_folder_button"));

    AtomicReference<Path> selectedFolder = new AtomicReference<>();

    ButtonType confirmButtonType =
        new ButtonType(bundle.getString("receipt.export.dialog.confirm_button"),
            ButtonBar.ButtonData.OK_DONE);
    getDialogPane().getButtonTypes().addAll(confirmButtonType, ButtonType.CANCEL);

    Node confirmButton = getDialogPane().lookupButton(confirmButtonType);
    confirmButton.setDisable(true);

    Runnable updateConfirmState = () -> confirmButton.setDisable(monthComboBox.getSelectionModel()
        .getSelectedItem() == null || selectedFolder.get() == null);

    monthComboBox.getSelectionModel().selectedItemProperty()
        .addListener((obs, old, selected) -> updateConfirmState.run());

    chooseFolderButton.setOnAction(e -> {
      DirectoryChooser chooser = new DirectoryChooser();
      File dir = chooser.showDialog(getDialogPane().getScene().getWindow());
      if (dir != null) {
        selectedFolder.set(dir.toPath());
        folderPathLabel.setText(dir.getAbsolutePath());
      }
      updateConfirmState.run();
    });

    VBox content = new VBox(10, monthLabel, monthComboBox, folderLabel,
        new HBox(8, chooseFolderButton, folderPathLabel));
    getDialogPane().setContent(content);

    setResultConverter(btn -> btn == confirmButtonType ?
        new Result(monthComboBox.getSelectionModel().getSelectedItem(), selectedFolder.get()) :
        null);
  }

  private static ListCell<YearMonth> monthCell() {
    return new ListCell<>() {
      @Override
      protected void updateItem(YearMonth item, boolean empty) {
        super.updateItem(item, empty);
        setText(empty || item == null ? null : item.format(MONTH_FMT));
      }
    };
  }


  public record Result(YearMonth month, Path destinationFolder) {
  }
}
