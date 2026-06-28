package com.guilherme.emobiliaria.receipt.ui.component;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import jakarta.inject.Inject;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.Clipboard;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.Set;

public class ProofDropZonePane extends VBox {

  private final Label dropZoneLabel = new Label();
  private final ListView<ProofListItem> proofListView = new ListView<>();
  private final Label errorLabel = new Label();

  private final List<PendingProof> pendingFiles = new ArrayList<>();
  private final List<Long> proofsToRemove = new ArrayList<>();
  private final Set<String> pendingFileNames = new HashSet<>();
  private final Set<String> existingFileNames = new HashSet<>();

  private ResourceBundle bundle;

  @Inject
  public ProofDropZonePane() {
    getStyleClass().add("proof-drop-zone-pane");
    setSpacing(8);

    dropZoneLabel.getStyleClass().add("proof-drop-zone-label");
    dropZoneLabel.setWrapText(true);
    dropZoneLabel.setMaxWidth(Double.MAX_VALUE);
    dropZoneLabel.setAlignment(Pos.CENTER);

    errorLabel.getStyleClass().add("proof-error-label");
    errorLabel.setWrapText(true);
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);

    proofListView.setMaxHeight(160);
    proofListView.setVisible(false);
    proofListView.setManaged(false);

    getChildren().addAll(dropZoneLabel, errorLabel, proofListView);

    setOnDragOver(this::handleDragOver);
    setOnDragDropped(this::handleDragDropped);
    setOnMouseClicked(e -> openFileChooser());
  }

  public void initialize(ResourceBundle bundle) {
    this.bundle = bundle;
    dropZoneLabel.setText(bundle.getString("receipt.form.proof.drop_zone"));
  }

  public void loadExistingProofs(List<PaymentProof> proofs) {
    existingFileNames.clear();
    for (PaymentProof proof : proofs) {
      existingFileNames.add(proof.getOriginalFileName().toLowerCase(Locale.ROOT));
      proofListView.getItems().add(new ProofListItem(proof));
    }
    refreshListVisibility();
  }

  public List<PendingProof> getPendingFilesToAttach() {
    return new ArrayList<>(pendingFiles);
  }

  public List<Long> getProofsToRemove() {
    return new ArrayList<>(proofsToRemove);
  }

  public void handleClipboardPaste() {
    Clipboard clipboard = Clipboard.getSystemClipboard();
    if (clipboard.hasImage()) {
      javafx.scene.image.Image image = clipboard.getImage();
      try {
        byte[] bytes = imageToPngBytes(image);
        String name = "clipboard-" + System.currentTimeMillis() + ".png";
        addPendingBytes(bytes, name);
      } catch (java.io.IOException e) {
        showError(bundle != null ?
            bundle.getString("receipt.form.proof.error.unsupported_type") :
            "Unsupported file type");
      }
    } else if (clipboard.hasFiles()) {
      for (File file : clipboard.getFiles()) {
        addFileIfSupported(file.toPath(), file.getName());
      }
    }
  }

  private byte[] imageToPngBytes(javafx.scene.image.Image image) throws java.io.IOException {
    int width = (int) image.getWidth();
    int height = (int) image.getHeight();
    javafx.scene.image.PixelReader pixelReader = image.getPixelReader();
    // TYPE_INT_RGB: Windows clipboard bitmaps have no alpha channel; JavaFX fills alpha=0,
    // which would produce a fully-transparent (all-black) PNG with TYPE_INT_ARGB.
    java.awt.image.BufferedImage awtImage =
        new java.awt.image.BufferedImage(width, height, java.awt.image.BufferedImage.TYPE_INT_RGB);
    for (int y = 0; y < height; y++) {
      for (int x = 0; x < width; x++) {
        awtImage.setRGB(x, y, pixelReader.getArgb(x, y));
      }
    }
    java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
    javax.imageio.ImageIO.write(awtImage, "png", baos);
    return baos.toByteArray();
  }

  private void handleDragOver(DragEvent event) {
    Dragboard db = event.getDragboard();
    if (db.hasFiles()) {
      event.acceptTransferModes(TransferMode.COPY);
    }
    event.consume();
  }

  private void handleDragDropped(DragEvent event) {
    Dragboard db = event.getDragboard();
    boolean success = false;
    if (db.hasFiles()) {
      for (File file : db.getFiles()) {
        addFileIfSupported(file.toPath(), file.getName());
      }
      success = true;
    }
    event.setDropCompleted(success);
    event.consume();
  }

  private void openFileChooser() {
    FileChooser chooser = new FileChooser();
    if (bundle != null) {
      chooser.setTitle(bundle.getString("receipt.form.proof.dialog.title"));
    }
    FileChooser.ExtensionFilter allSupported =
        new FileChooser.ExtensionFilter("All supported", "*.pdf", "*.jpg", "*.jpeg", "*.png",
            "*.gif", "*.bmp", "*.webp", "*.tiff", "*.tif");
    chooser.getExtensionFilters()
        .addAll(allSupported, new FileChooser.ExtensionFilter("PDF", "*.pdf"),
        new FileChooser.ExtensionFilter("Images", "*.jpg", "*.jpeg", "*.png", "*.gif", "*.bmp",
            "*.webp", "*.tiff", "*.tif"));
    chooser.setSelectedExtensionFilter(allSupported);
    File selected = chooser.showOpenDialog(getScene() != null ? getScene().getWindow() : null);
    if (selected != null) {
      addFileIfSupported(selected.toPath(), selected.getName());
    }
  }

  private static String stripExtension(String name) {
    int dot = name.lastIndexOf('.');
    return dot > 0 ? name.substring(0, dot) : name;
  }

  private void addFileIfSupported(Path path, String name) {
    clearError();
    if (ProofFileType.fromExtension(name).isEmpty()) {
      showError(bundle != null ?
          bundle.getString("receipt.form.proof.error.unsupported_type") :
          "Unsupported file type");
      return;
    }
    String lowerName = name.toLowerCase(Locale.ROOT);
    if (pendingFileNames.contains(lowerName) || existingFileNames.contains(lowerName)) {
      showError(bundle != null ?
          bundle.getString("receipt.form.proof.error.duplicate") :
          "Duplicate file");
      return;
    }
    String defaultDisplayName = stripExtension(name);
    Optional<String> displayName = promptDisplayName(defaultDisplayName);
    if (displayName.isEmpty()) {
      return;
    }
    ProofFileType fileType = ProofFileType.fromExtension(name).get();
    PendingProof pending = new PendingProof(path, null, name, fileType, displayName.get());
    pendingFiles.add(pending);
    pendingFileNames.add(lowerName);
    proofListView.getItems().add(new ProofListItem(pending));
    refreshListVisibility();
  }

  private void addPendingBytes(byte[] bytes, String name) {
    clearError();
    String lowerName = name.toLowerCase(Locale.ROOT);
    if (pendingFileNames.contains(lowerName)) {
      showError(bundle != null ?
          bundle.getString("receipt.form.proof.error.duplicate") :
          "Duplicate file");
      return;
    }
    String defaultDisplayName = stripExtension(name);
    Optional<String> displayName = promptDisplayName(defaultDisplayName);
    if (displayName.isEmpty()) {
      return;
    }
    PendingProof pending =
        new PendingProof(null, bytes, name, ProofFileType.IMAGE, displayName.get());
    pendingFiles.add(pending);
    pendingFileNames.add(lowerName);
    proofListView.getItems().add(new ProofListItem(pending));
    refreshListVisibility();
  }

  private Optional<String> promptDisplayName(String defaultName) {
    TextInputDialog dialog = new TextInputDialog(defaultName);
    if (bundle != null) {
      dialog.setTitle(bundle.getString("receipt.form.proof.name_dialog.title"));
      dialog.setHeaderText(bundle.getString("receipt.form.proof.name_dialog.header"));
      dialog.setContentText(bundle.getString("receipt.form.proof.name_dialog.label"));
    } else {
      dialog.setTitle("Name the Proof");
      dialog.setHeaderText("Enter a display name for this file");
      dialog.setContentText("Display name:");
    }
    return dialog.showAndWait().map(s -> s.isBlank() ? defaultName : s);
  }

  private void removeItem(ProofListItem item) {
    proofListView.getItems().remove(item);
    if (item.existingProof != null) {
      proofsToRemove.add(item.existingProof.getId());
      existingFileNames.remove(item.existingProof.getOriginalFileName().toLowerCase(Locale.ROOT));
    } else if (item.pendingProof != null) {
      pendingFiles.remove(item.pendingProof);
      pendingFileNames.remove(item.pendingProof.originalFileName().toLowerCase(Locale.ROOT));
    }
    refreshListVisibility();
  }

  private void refreshListVisibility() {
    boolean hasItems = !proofListView.getItems().isEmpty();
    proofListView.setVisible(hasItems);
    proofListView.setManaged(hasItems);
  }

  private void showError(String message) {
    errorLabel.setText(message);
    errorLabel.setVisible(true);
    errorLabel.setManaged(true);
  }

  private void clearError() {
    errorLabel.setText("");
    errorLabel.setVisible(false);
    errorLabel.setManaged(false);
  }


  public record PendingProof(Path file, byte[] imageBytes, String originalFileName,
                             ProofFileType fileType, String displayName) {
  }


  private class ProofListItem extends HBox {

    private final PaymentProof existingProof;
    private final PendingProof pendingProof;

    ProofListItem(PaymentProof proof) {
      this.existingProof = proof;
      this.pendingProof = null;
      build(proof.getDisplayName(), proof.getFileType());
    }

    ProofListItem(PendingProof pending) {
      this.existingProof = null;
      this.pendingProof = pending;
      build(pending.displayName(), pending.fileType());
    }

    private void build(String name, ProofFileType type) {
      setAlignment(Pos.CENTER_LEFT);
      setSpacing(8);

      Label icon = new Label(type == ProofFileType.PDF ? "📄" : "🖼");
      Label nameLabel = new Label(name);
      HBox.setHgrow(nameLabel, Priority.ALWAYS);

      Button removeBtn = new Button("✕");
      removeBtn.getStyleClass().add("proof-remove-button");
      removeBtn.setOnAction(e -> removeItem(this));

      getChildren().addAll(icon, nameLabel, removeBtn);
    }
  }
}
