package com.guilherme.emobiliaria.dashboard.ui.controller;

import com.guilherme.emobiliaria.dashboard.application.input.GetDashboardDataInput;
import com.guilherme.emobiliaria.dashboard.application.output.GetDashboardDataOutput;
import com.guilherme.emobiliaria.dashboard.application.usecase.GetDashboardDataInteractor;
import com.guilherme.emobiliaria.dashboard.domain.entity.DashboardData;
import com.guilherme.emobiliaria.dashboard.domain.entity.ExpiringContractEntry;
import com.guilherme.emobiliaria.dashboard.domain.entity.TopRentEntry;
import com.guilherme.emobiliaria.dashboard.domain.entity.UnpaidRentEntry;
import com.guilherme.emobiliaria.dashboard.domain.entity.UrgencyLevel;
import com.guilherme.emobiliaria.dashboard.domain.entity.VacantPropertyEntry;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.util.MoneyFormatter;
import jakarta.inject.Inject;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.ResourceBundle;

public class DashboardController {

  private static final Logger log = LoggerFactory.getLogger(DashboardController.class);

  private static final String DASHBOARD_FXML =
      "/com/guilherme/emobiliaria/dashboard/ui/view/dashboard-view.fxml";
  private static final DateTimeFormatter DUE_DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final Locale PT_BR = Locale.of("pt", "BR");

  private final GetDashboardDataInteractor getDashboardData;
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
  private Label totalRevenueLabel;
  @FXML
  private Label activeContractsLabel;
  @FXML
  private Label revenueEyebrowLabel;
  @FXML
  private Label topRentsEyebrowLabel;
  @FXML
  private Label unpaidRentsTitleLabel;
  @FXML
  private Label unpaidRentsSubtitleLabel;
  @FXML
  private Label vacantPropertiesTitleLabel;
  @FXML
  private Label vacantPropertiesSubtitleLabel;
  @FXML
  private Label expiringContractsTitleLabel;
  @FXML
  private Label expiringContractsSubtitleLabel;
  @FXML
  private VBox topRentsContainer;
  @FXML
  private VBox unpaidRentsContainer;
  @FXML
  private VBox vacantPropertiesContainer;
  @FXML
  private VBox expiringContractsContainer;
  @Inject
  public DashboardController(GetDashboardDataInteractor getDashboardData,
      GuiceFxmlLoader fxmlLoader) {
    this.getDashboardData = getDashboardData;
    this.fxmlLoader = fxmlLoader;
  }

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());
    LocalDate today = LocalDate.now();
    titleLabel.setText(bundle.getString("dashboard.title"));
    String month = today.getMonth().getDisplayName(TextStyle.FULL, Locale.getDefault());
    month = Character.toUpperCase(month.charAt(0)) + month.substring(1);
    subtitleLabel.setText(
        MessageFormat.format(bundle.getString("dashboard.subtitle"), month, today.getYear()));
    revenueEyebrowLabel.setText(bundle.getString("dashboard.revenue.eyebrow"));
    topRentsEyebrowLabel.setText(bundle.getString("dashboard.top_rents.eyebrow"));
    unpaidRentsTitleLabel.setText(bundle.getString("dashboard.unpaid_rents.title"));
    unpaidRentsSubtitleLabel.setText(bundle.getString("dashboard.unpaid_rents.subtitle"));
    vacantPropertiesTitleLabel.setText(bundle.getString("dashboard.vacant_properties.title"));
    vacantPropertiesSubtitleLabel.setText(bundle.getString("dashboard.vacant_properties.subtitle"));
    expiringContractsTitleLabel.setText(bundle.getString("dashboard.expiring_contracts.title"));
    expiringContractsSubtitleLabel.setText(
        bundle.getString("dashboard.expiring_contracts.subtitle"));

    loadingIndicator.setVisible(true);
    contentScrollPane.setVisible(false);

    Task<GetDashboardDataOutput> task = new Task<>() {
      @Override
      protected GetDashboardDataOutput call() {
        return getDashboardData.execute(new GetDashboardDataInput(today));
      }
    };

    task.setOnSucceeded(e -> {
      DashboardData data = task.getValue().data();
      populateRevenue(data);
      populateTopRents(data);
      populateUnpaidRents(data);
      populateVacantProperties(data);
      populateExpiringContracts(data);
      loadingIndicator.setVisible(false);
      contentScrollPane.setVisible(true);
    });

    task.setOnFailed(e -> {
      log.error("Failed to load dashboard data", task.getException());
      com.guilherme.emobiliaria.shared.ui.ErrorHandler.handle(task.getException(), bundle);
      loadingIndicator.setVisible(false);
    });

    new Thread(task).start();
  }

  public Node buildView() {
    URL resource = getClass().getResource(DASHBOARD_FXML);
    if (resource == null) {
      log.error("dashboard-view.fxml not found at {}", DASHBOARD_FXML);
      return new javafx.scene.layout.StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load dashboard view", e);
      return new javafx.scene.layout.StackPane();
    }
  }

  // ── Populate methods ─────────────────────────────────────────────────────────

  private void populateRevenue(DashboardData data) {
    totalRevenueLabel.setText(formatCurrency(data.totalRevenueCents()));
    int count = data.activeContractCount();
    activeContractsLabel.setText(count + " " + (count == 1 ?
        bundle.getString("dashboard.revenue.contracts.singular") :
        bundle.getString("dashboard.revenue.contracts.plural")));
  }

  private void populateTopRents(DashboardData data) {
    topRentsContainer.getChildren().clear();
    if (data.topRents().isEmpty()) {
      topRentsContainer.getChildren()
          .add(emptyLabel(bundle.getString("dashboard.top_rents.empty")));
      return;
    }
    for (TopRentEntry entry : data.topRents()) {
      topRentsContainer.getChildren().add(buildTopRentRow(entry));
    }
  }

  private void populateUnpaidRents(DashboardData data) {
    unpaidRentsContainer.getChildren().clear();
    if (data.unpaidRents().isEmpty()) {
      unpaidRentsContainer.getChildren()
          .add(emptyLabel(bundle.getString("dashboard.unpaid_rents.empty")));
      return;
    }
    for (UnpaidRentEntry entry : data.unpaidRents()) {
      unpaidRentsContainer.getChildren().add(buildUnpaidRentRow(entry));
    }
  }

  private void populateVacantProperties(DashboardData data) {
    vacantPropertiesContainer.getChildren().clear();
    if (data.vacantProperties().isEmpty()) {
      vacantPropertiesContainer.getChildren()
          .add(emptyLabel(bundle.getString("dashboard.vacant_properties.empty")));
      return;
    }
    for (VacantPropertyEntry entry : data.vacantProperties()) {
      vacantPropertiesContainer.getChildren().add(buildVacantPropertyRow(entry));
    }
  }

  private void populateExpiringContracts(DashboardData data) {
    expiringContractsContainer.getChildren().clear();
    if (data.expiringContracts().isEmpty()) {
      expiringContractsContainer.getChildren()
          .add(emptyLabel(bundle.getString("dashboard.expiring_contracts.empty")));
      return;
    }
    for (ExpiringContractEntry entry : data.expiringContracts()) {
      expiringContractsContainer.getChildren().add(buildExpiringContractRow(entry));
    }
  }

  // ── Row builders ─────────────────────────────────────────────────────────────

  private Node buildTopRentRow(TopRentEntry entry) {
    HBox row = new HBox();
    row.getStyleClass().add("dashboard-top-rent-row");

    Label rank = new Label(String.valueOf(entry.rank()));
    rank.getStyleClass().add("dashboard-top-rent-rank");

    VBox info = new VBox();
    info.getStyleClass().add("dashboard-top-rent-info");
    HBox.setHgrow(info, Priority.ALWAYS);
    Label property = new Label(entry.propertyName());
    property.getStyleClass().add("dashboard-top-rent-property");
    Label tenant = new Label(entry.tenantName() != null ? entry.tenantName() : "—");
    tenant.getStyleClass().add("dashboard-top-rent-tenant");
    info.getChildren().addAll(property, tenant);

    Label amount = new Label(formatCurrency(entry.rentCents()));
    amount.getStyleClass().add("dashboard-top-rent-amount");

    row.getChildren().addAll(rank, info, amount);
    return row;
  }

  private Node buildUnpaidRentRow(UnpaidRentEntry entry) {
    HBox row = new HBox();
    row.getStyleClass().add("dashboard-unpaid-row");

    VBox info = new VBox();
    info.getStyleClass().add("dashboard-unpaid-info");
    HBox.setHgrow(info, Priority.ALWAYS);
    Label property = new Label(entry.propertyName());
    property.getStyleClass().add("dashboard-unpaid-property");
    Label tenant = new Label(entry.tenantName() != null ? entry.tenantName() : "—");
    tenant.getStyleClass().add("dashboard-unpaid-tenant");
    info.getChildren().addAll(property, tenant);

    VBox amountCol = new VBox();
    amountCol.getStyleClass().add("dashboard-unpaid-amount-col");
    amountCol.setAlignment(Pos.TOP_RIGHT);
    Label amount = new Label(formatCurrency(entry.rentCents()));
    amount.getStyleClass().add("dashboard-unpaid-amount");
    Label dueDate = new Label(
        bundle.getString("dashboard.unpaid_rents.due_label") + " " + entry.dueDate()
            .format(DUE_DATE_FMT));
    dueDate.getStyleClass().add("dashboard-unpaid-due-date");
    amountCol.getChildren().addAll(amount, dueDate);

    row.getChildren().addAll(info, amountCol);
    return row;
  }

  private Node buildVacantPropertyRow(VacantPropertyEntry entry) {
    HBox row = new HBox();
    row.getStyleClass().add("dashboard-vacant-row");

    VBox iconBox = new VBox();
    iconBox.getStyleClass().add("dashboard-vacant-icon-box");
    iconBox.setAlignment(Pos.CENTER);
    Label icon = new Label("⌂");
    icon.getStyleClass().add("dashboard-vacant-icon");
    iconBox.getChildren().add(icon);

    VBox info = new VBox();
    info.getStyleClass().add("dashboard-vacant-info");
    HBox.setHgrow(info, Priority.ALWAYS);
    Label property = new Label(entry.propertyName());
    property.getStyleClass().add("dashboard-vacant-property");
    String addressText = entry.type() + " · " + entry.address();
    Label address = new Label(addressText);
    address.getStyleClass().add("dashboard-vacant-address");
    info.getChildren().addAll(property, address);

    row.getChildren().addAll(iconBox, info);
    return row;
  }

  private Node buildExpiringContractRow(ExpiringContractEntry entry) {
    HBox row = new HBox();
    row.getStyleClass().add("dashboard-expiring-row");

    VBox dateCol = new VBox();
    dateCol.getStyleClass().add("dashboard-expiring-date-col");
    dateCol.setAlignment(Pos.CENTER);
    Label day = new Label(String.format("%02d", entry.endDate().getDayOfMonth()));
    day.getStyleClass().add("dashboard-expiring-day");
    applyUrgencyDayStyle(day, entry.urgency());
    String monthAbbr = entry.endDate().getMonth().getDisplayName(TextStyle.SHORT, PT_BR);
    monthAbbr = Character.toUpperCase(monthAbbr.charAt(0)) + monthAbbr.substring(1, 3);
    Label month = new Label(monthAbbr);
    month.getStyleClass().add("dashboard-expiring-month");
    dateCol.getChildren().addAll(day, month);

    Region separator = new Region();
    separator.getStyleClass().add("dashboard-expiring-separator");

    VBox info = new VBox();
    info.getStyleClass().add("dashboard-expiring-info");
    HBox.setHgrow(info, Priority.ALWAYS);
    Label property = new Label(entry.propertyName());
    property.getStyleClass().add("dashboard-expiring-property");
    String tenantText = (entry.tenantName() != null ?
        entry.tenantName() :
        "—") + " · " + entry.daysLeft() + " dias";
    Label tenant = new Label(tenantText);
    tenant.getStyleClass().add("dashboard-expiring-tenant");
    info.getChildren().addAll(property, tenant);

    Label badge = buildUrgencyBadge(entry.urgency());

    row.getChildren().addAll(dateCol, separator, info, badge);
    return row;
  }

  // ── Helpers ──────────────────────────────────────────────────────────────────

  private void applyUrgencyDayStyle(Label day, UrgencyLevel urgency) {
    switch (urgency) {
      case URGENT -> day.getStyleClass().add("dashboard-expiring-day-urgent");
      case WARNING -> day.getStyleClass().add("dashboard-expiring-day-warning");
      default -> {
      }
    }
  }

  private Label buildUrgencyBadge(UrgencyLevel urgency) {
    Label badge = new Label();
    badge.getStyleClass().add("dashboard-badge");
    switch (urgency) {
      case URGENT -> {
        badge.setText(bundle.getString("dashboard.urgency.urgent"));
        badge.getStyleClass().add("dashboard-badge-urgent");
      }
      case WARNING -> {
        badge.setText(bundle.getString("dashboard.urgency.warning"));
        badge.getStyleClass().add("dashboard-badge-warning");
      }
      default -> {
        badge.setText(bundle.getString("dashboard.urgency.normal"));
        badge.getStyleClass().add("dashboard-badge-normal");
      }
    }
    return badge;
  }

  private Label emptyLabel(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("dashboard-panel-empty");
    return label;
  }

  private String formatCurrency(int cents) {
    return MoneyFormatter.formatWithSymbol(cents);
  }
}
