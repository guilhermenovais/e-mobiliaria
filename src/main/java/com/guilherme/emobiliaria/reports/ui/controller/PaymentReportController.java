package com.guilherme.emobiliaria.reports.ui.controller;

import com.guilherme.emobiliaria.reports.application.input.GeneratePaymentReportPdfInput;
import com.guilherme.emobiliaria.reports.application.input.GetPaymentReportMonthsInput;
import com.guilherme.emobiliaria.reports.application.input.LoadPaymentReportInput;
import com.guilherme.emobiliaria.reports.application.usecase.GeneratePaymentReportPdfInteractor;
import com.guilherme.emobiliaria.reports.application.usecase.GetPaymentReportMonthsInteractor;
import com.guilherme.emobiliaria.reports.application.usecase.LoadPaymentReportInteractor;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRowStatus;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import com.guilherme.emobiliaria.shared.util.MoneyFormatter;
import com.guilherme.emobiliaria.shared.util.TaxIdFormatter;
import jakarta.inject.Inject;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.layout.StackPane;
import javafx.util.StringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class PaymentReportController {

  private static final Logger log = LoggerFactory.getLogger(PaymentReportController.class);

  private static final String PAYMENT_REPORT_FXML =
      "/com/guilherme/emobiliaria/reports/ui/view/payment-report-view.fxml";

  private final GetPaymentReportMonthsInteractor getMonths;
  private final LoadPaymentReportInteractor loadReport;
  private final GeneratePaymentReportPdfInteractor generatePdf;
  private final GuiceFxmlLoader fxmlLoader;

  private ResourceBundle bundle;

  @FXML
  private Label monthSelectorLabel;
  @FXML
  private ComboBox<YearMonth> monthComboBox;
  @FXML
  private ProgressIndicator tableLoadingIndicator;
  @FXML
  private TableView<PaymentReportRow> reportTable;
  @FXML
  private TableColumn<PaymentReportRow, String> propertyCol;
  @FXML
  private TableColumn<PaymentReportRow, String> tenantCol;
  @FXML
  private TableColumn<PaymentReportRow, String> taxIdCol;
  @FXML
  private TableColumn<PaymentReportRow, String> paymentDateCol;
  @FXML
  private TableColumn<PaymentReportRow, String> rentCol;
  @FXML
  private TableColumn<PaymentReportRow, String> periodCol;
  @FXML
  private Button exportPdfButton;
  @FXML
  private ProgressIndicator pdfLoadingIndicator;

  @Inject
  public PaymentReportController(GetPaymentReportMonthsInteractor getMonths,
      LoadPaymentReportInteractor loadReport, GeneratePaymentReportPdfInteractor generatePdf,
      GuiceFxmlLoader fxmlLoader) {
    this.getMonths = getMonths;
    this.loadReport = loadReport;
    this.generatePdf = generatePdf;
    this.fxmlLoader = fxmlLoader;
  }

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());

    reportTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    monthSelectorLabel.setText(bundle.getString("reports.payment_report.month_selector.label"));
    exportPdfButton.setText(bundle.getString("reports.payment_report.button.generate_pdf"));

    propertyCol.setText(bundle.getString("reports.payment_report.column.property"));
    tenantCol.setText(bundle.getString("reports.payment_report.column.tenant"));
    taxIdCol.setText(bundle.getString("reports.payment_report.column.tax_id"));
    paymentDateCol.setText(bundle.getString("reports.payment_report.column.payment_date"));
    rentCol.setText(bundle.getString("reports.payment_report.column.rent"));
    periodCol.setText(bundle.getString("reports.payment_report.column.period"));

    propertyCol.setCellValueFactory(
        data -> new SimpleStringProperty(data.getValue().propertyName()));
    tenantCol.setCellValueFactory(data -> new SimpleStringProperty(
        data.getValue().primaryTenantName() != null ? data.getValue().primaryTenantName() : ""));
    taxIdCol.setCellValueFactory(data -> new SimpleStringProperty(
        TaxIdFormatter.format(data.getValue().primaryTenantTaxId())));
    paymentDateCol.setCellValueFactory(data -> {
      LocalDate date = data.getValue().paymentDate();
      return new SimpleStringProperty(
          date != null ? date.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "—");
    });
    DateTimeFormatter paymentDateSortFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    paymentDateCol.setComparator((a, b) -> {
      if ("—".equals(a))
        return 1;
      if ("—".equals(b))
        return -1;
      return LocalDate.parse(a, paymentDateSortFmt)
          .compareTo(LocalDate.parse(b, paymentDateSortFmt));
    });
    paymentDateCol.setSortType(TableColumn.SortType.ASCENDING);
    reportTable.getSortOrder().setAll(paymentDateCol);
    rentCol.setCellValueFactory(data -> {
      Integer rent = data.getValue().rent();
      return new SimpleStringProperty(rent != null ? MoneyFormatter.formatWithSymbol(rent) : "—");
    });
    periodCol.setCellValueFactory(data -> {
      PaymentReportRow row = data.getValue();
      if (row.periodStart() == null || row.periodEnd() == null)
        return new SimpleStringProperty("");
      DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
      return new SimpleStringProperty(
          row.periodStart().format(fmt) + " – " + row.periodEnd().format(fmt));
    });

    applyRowFactory();

    monthComboBox.setConverter(new StringConverter<>() {
      private static final String[] PT_MONTHS_SHORT =
          {"jan", "fev", "mar", "abr", "mai", "jun", "jul", "ago", "set", "out", "nov", "dez"};

      @Override
      public String toString(YearMonth ym) {
        if (ym == null)
          return "";
        return PT_MONTHS_SHORT[ym.getMonthValue() - 1] + "/" + ym.getYear();
      }

      @Override
      public YearMonth fromString(String s) {
        return null;
      }
    });

    List<YearMonth> months = getMonths.execute(new GetPaymentReportMonthsInput()).months();
    monthComboBox.setItems(FXCollections.observableArrayList(months));
    if (!months.isEmpty()) {
      monthComboBox.getSelectionModel().selectFirst();
      loadTableData(months.get(0));
    }

    monthComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
      if (newVal != null)
        loadTableData(newVal);
    });
  }

  private void applyRowFactory() {
    reportTable.setRowFactory(tv -> new TableRow<>() {
      @Override
      protected void updateItem(PaymentReportRow row, boolean empty) {
        super.updateItem(row, empty);
        getStyleClass().removeAll("payment-row-unpaid", "payment-row-vacant");
        if (!empty && row != null) {
          if (row.status() == PaymentReportRowStatus.UNPAID) {
            getStyleClass().add("payment-row-unpaid");
          } else if (row.status() == PaymentReportRowStatus.VACANT) {
            getStyleClass().add("payment-row-vacant");
          }
        }
      }
    });
  }

  private void loadTableData(YearMonth month) {
    tableLoadingIndicator.setVisible(true);
    reportTable.setItems(FXCollections.emptyObservableList());

    Task<List<PaymentReportRow>> task = new Task<>() {
      @Override
      protected List<PaymentReportRow> call() {
        return loadReport.execute(new LoadPaymentReportInput(month)).rows();
      }
    };

    task.setOnSucceeded(e -> {
      tableLoadingIndicator.setVisible(false);
      reportTable.setItems(FXCollections.observableArrayList(task.getValue()));
    });

    task.setOnFailed(e -> {
      tableLoadingIndicator.setVisible(false);
      log.error("Failed to load payment report data", task.getException());
      ErrorHandler.handle(task.getException(), bundle);
    });

    new Thread(task).start();
  }

  @FXML
  private void onExportPdf() {
    YearMonth selected = monthComboBox.getSelectionModel().getSelectedItem();
    if (selected == null)
      return;

    exportPdfButton.setDisable(true);
    pdfLoadingIndicator.setVisible(true);

    Task<Void> task = new Task<>() {
      @Override
      protected Void call() throws Exception {
        byte[] pdfBytes =
            generatePdf.execute(new GeneratePaymentReportPdfInput(selected)).pdfBytes();
        Path tmp = Files.createTempFile("payment_report_", ".pdf");
        Files.write(tmp, pdfBytes);
        Desktop.getDesktop().open(tmp.toFile());
        return null;
      }
    };

    task.setOnSucceeded(e -> {
      pdfLoadingIndicator.setVisible(false);
      exportPdfButton.setDisable(false);
    });

    task.setOnFailed(e -> {
      pdfLoadingIndicator.setVisible(false);
      exportPdfButton.setDisable(false);
      log.error("Failed to generate payment report PDF", task.getException());
      ErrorHandler.handle(task.getException(), bundle);
    });

    new Thread(task).start();
  }

  public Node buildView() {
    URL resource = getClass().getResource(PAYMENT_REPORT_FXML);
    if (resource == null) {
      log.error("payment-report-view.fxml not found at {}", PAYMENT_REPORT_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load payment report view", e);
      return new StackPane();
    }
  }
}
