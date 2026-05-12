package com.guilherme.emobiliaria.reports.ui.controller;

import com.guilherme.emobiliaria.reports.application.input.GenerateOccupationRateReportInput;
import com.guilherme.emobiliaria.reports.application.input.GenerateRentEvolutionReportInput;
import com.guilherme.emobiliaria.reports.application.usecase.GenerateOccupationRateReportInteractor;
import com.guilherme.emobiliaria.reports.application.usecase.GenerateRentEvolutionReportInteractor;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import jakarta.inject.Inject;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;

public class ReportsController {

  private static final Logger log = LoggerFactory.getLogger(ReportsController.class);

  private static final String REPORTS_FXML =
      "/com/guilherme/emobiliaria/reports/ui/view/reports-view.fxml";

  private final GenerateRentEvolutionReportInteractor generateRentEvolutionReport;
  private final GenerateOccupationRateReportInteractor generateOccupationRateReport;
  private final PaymentReportController paymentReportController;
  private final GuiceFxmlLoader fxmlLoader;

  private ResourceBundle bundle;

  @FXML
  private ProgressIndicator loadingIndicator;
  @FXML
  private ScrollPane contentScrollPane;
  @FXML
  private Label titleLabel;
  @FXML
  private Label subtitleLabel;
  @FXML
  private Label rentEvolutionNameLabel;
  @FXML
  private Label rentEvolutionDescLabel;
  @FXML
  private Button generateRentEvolutionButton;
  @FXML
  private Label occupationRateNameLabel;
  @FXML
  private Label occupationRateDescLabel;
  @FXML
  private Button generateOccupationRateButton;
  @FXML
  private Label paymentReportNameLabel;
  @FXML
  private Label paymentReportDescLabel;
  @FXML
  private Button openPaymentReportButton;

  @Inject
  public ReportsController(GenerateRentEvolutionReportInteractor generateRentEvolutionReport,
      GenerateOccupationRateReportInteractor generateOccupationRateReport,
      PaymentReportController paymentReportController, GuiceFxmlLoader fxmlLoader) {
    this.generateRentEvolutionReport = generateRentEvolutionReport;
    this.generateOccupationRateReport = generateOccupationRateReport;
    this.paymentReportController = paymentReportController;
    this.fxmlLoader = fxmlLoader;
  }

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());

    titleLabel.setText(bundle.getString("reports.title"));
    subtitleLabel.setText(bundle.getString("reports.subtitle"));
    rentEvolutionNameLabel.setText(bundle.getString("reports.rent_evolution.name"));
    rentEvolutionDescLabel.setText(bundle.getString("reports.rent_evolution.description"));
    generateRentEvolutionButton.setText(bundle.getString("reports.button.generate_pdf"));
    occupationRateNameLabel.setText(bundle.getString("reports.occupation_rate.name"));
    occupationRateDescLabel.setText(bundle.getString("reports.occupation_rate.description"));
    generateOccupationRateButton.setText(bundle.getString("reports.button.generate_pdf"));
    paymentReportNameLabel.setText(bundle.getString("reports.payment_report.name"));
    paymentReportDescLabel.setText(bundle.getString("reports.payment_report.description"));
    openPaymentReportButton.setText(bundle.getString("reports.payment_report.button.generate_pdf"));

    loadingIndicator.setVisible(false);
    contentScrollPane.setVisible(true);
  }

  @FXML
  private void onGenerateRentEvolutionPdf() {
    generatePdf(() -> generateRentEvolutionReport.execute(new GenerateRentEvolutionReportInput())
        .pdfBytes(), "rent_evolution", generateRentEvolutionButton);
  }

  @FXML
  private void onGenerateOccupationRatePdf() {
    generatePdf(() -> generateOccupationRateReport.execute(new GenerateOccupationRateReportInput())
        .pdfBytes(), "occupation_rate", generateOccupationRateButton);
  }

  @FXML
  private void onOpenPaymentReport() {
    openSubView(paymentReportController::buildView);
  }

  private void openSubView(Supplier<Node> viewBuilder) {
    Node view = viewBuilder.get();
    contentScrollPane.setContent(view);
  }

  private void generatePdf(PdfSupplier supplier, String filePrefix, Button button) {
    button.setDisable(true);
    loadingIndicator.setVisible(true);

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        byte[] pdfBytes = supplier.get();
        Path tmp = Files.createTempFile(filePrefix + "_", ".pdf");
        Files.write(tmp, pdfBytes);
        Desktop.getDesktop().open(tmp.toFile());
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      loadingIndicator.setVisible(false);
      button.setDisable(false);
    });

    task.setOnFailed(e -> {
      loadingIndicator.setVisible(false);
      button.setDisable(false);
      log.error("Failed to generate PDF report", task.getException());
      ErrorHandler.handle(task.getException(), bundle);
    });

    new Thread(task).start();
  }

  public Node buildView() {
    URL resource = getClass().getResource(REPORTS_FXML);
    if (resource == null) {
      log.error("reports-view.fxml not found at {}", REPORTS_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load reports view", e);
      return new StackPane();
    }
  }

  @FunctionalInterface
  private interface PdfSupplier {
    byte[] get() throws Exception;
  }
}
