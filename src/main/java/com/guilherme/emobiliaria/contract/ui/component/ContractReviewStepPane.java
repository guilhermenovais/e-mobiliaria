package com.guilherme.emobiliaria.contract.ui.component;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class ContractReviewStepPane extends VBox {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final ResourceBundle bundle;

  private final VBox propertySection = new VBox(4);
  private final VBox landlordSection = new VBox(4);
  private final VBox tenantsSection = new VBox(4);
  private final VBox detailsSection = new VBox(4);
  private final VBox accountSection = new VBox(4);

  public ContractReviewStepPane(ResourceBundle bundle) {
    this.bundle = bundle;
    getStyleClass().add("wizard-step-pane");
    buildLayout();
  }

  private void buildLayout() {
    VBox container = new VBox();
    container.getStyleClass().add("wizard-review-container");

    propertySection.getStyleClass().add("wizard-review-section");
    landlordSection.getStyleClass().add("wizard-review-section");
    tenantsSection.getStyleClass().add("wizard-review-section");
    detailsSection.getStyleClass().add("wizard-review-section");
    accountSection.getStyleClass().add("wizard-review-section");

    container.getChildren().addAll(
        propertySection, landlordSection, tenantsSection, detailsSection, accountSection);

    ScrollPane scroll = new ScrollPane(container);
    scroll.setFitToWidth(true);
    scroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
    scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

    getChildren().add(scroll);
  }

  public void populate(Property property, Person landlord, List<Person> tenants,
      LocalDate startDate, int durationMonths, int rentCents, int paymentDay,
      PaymentAccount account) {

    // Property
    propertySection.getChildren().clear();
    propertySection.getChildren().add(sectionHeader(bundle.getString("contract.wizard.step6.section.property")));
    if (property.getAddress() != null) {
      var addr = property.getAddress();
      String address = addr.getAddress() + ", " + addr.getNumber()
          + " - " + addr.getNeighborhood() + ", " + addr.getCity()
          + "/" + (addr.getState() != null ? addr.getState().name() : "");
      propertySection.getChildren().add(sectionValue(address));
    }
    propertySection.getChildren().add(sectionValue("Tipo: " + property.getType()));

    // Landlord
    landlordSection.getChildren().clear();
    landlordSection.getChildren().add(sectionHeader(bundle.getString("contract.wizard.step6.section.landlord")));
    landlordSection.getChildren().add(sectionValue(ContractLandlordStepPane.displayName(landlord)));

    // Tenants
    tenantsSection.getChildren().clear();
    tenantsSection.getChildren().add(sectionHeader(bundle.getString("contract.wizard.step6.section.tenants")));
    for (Person tenant : tenants) {
      tenantsSection.getChildren().add(sectionValue(ContractLandlordStepPane.displayName(tenant)));
    }

    // Details
    detailsSection.getChildren().clear();
    detailsSection.getChildren().add(sectionHeader(bundle.getString("contract.wizard.step6.section.details")));
    detailsSection.getChildren().add(sectionValue(
        "Data de Início: " + startDate.format(DATE_FMT) + " | Duração: " + durationMonths + " meses"));
    detailsSection.getChildren().add(sectionValue(
        "Valor do Aluguel: R$ " + String.format("%.2f", rentCents / 100.0).replace('.', ',')
            + " | Dia do Pagamento: " + paymentDay));

    // Account
    accountSection.getChildren().clear();
    accountSection.getChildren().add(sectionHeader(bundle.getString("contract.wizard.step6.section.account")));
    if (account != null) {
      accountSection.getChildren().add(sectionValue(
          account.getBank() + " - Agência " + account.getBankBranch()
              + " - Conta " + account.getAccountNumber()));
      if (account.getPixKey() != null && !account.getPixKey().isBlank()) {
        accountSection.getChildren().add(sectionValue("PIX: " + account.getPixKey()));
      }
    } else {
      accountSection.getChildren()
          .add(sectionValue(bundle.getString("contract.wizard.step6.section.account.empty")));
    }
  }

  private Label sectionHeader(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("wizard-section-header");
    return label;
  }

  private Label sectionValue(String text) {
    Label label = new Label(text);
    label.getStyleClass().add("wizard-section-value");
    label.setWrapText(true);
    return label;
  }
}
