package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptFileServiceImplTest {

  private final ReceiptFileServiceImpl service =
      new ReceiptFileServiceImpl(new PdfGenerationService(),
          ResourceBundle.getBundle("messages", Locale.forLanguageTag("pt-BR")));

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Property validProperty() {
    return Property.create("Apartamento Centro", "Apartamento", "1234567890", "0987654321",
        "IPTU-001", validAddress());
  }

  private Contract contractWithTenant(Person tenant) {
    PaymentAccount paymentAccount =
        PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, 150000, "Residencial",
        paymentAccount, validProperty(), tenant, List.of(tenant), List.of(), List.of());
  }

  private Receipt receiptWithTenant(LocalDate date, Person tenant) {
    return Receipt.create(date, date, date, date, 0, 0, null, contractWithTenant(tenant));
  }

  @Test
  @DisplayName(
      "When tenant is a physical person, should build name from receipt date and tenant name")
  void shouldBuildFileNameForPhysicalPersonTenant() {
    Person tenant =
        PhysicalPerson.create("Joao Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
            "529.982.247-25", "MG-1234567", validAddress());
    Receipt receipt = receiptWithTenant(LocalDate.of(2026, 7, 5), tenant);

    String fileName = service.defaultFileName(receipt);

    assertEquals("Recibo_05072026_Joao_Silva.pdf", fileName);
  }

  @Test
  @DisplayName(
      "When tenant is a juridical person, should build name from receipt date and corporate name")
  void shouldBuildFileNameForJuridicalPersonTenant() {
    PhysicalPerson representative =
        PhysicalPerson.create("Maria Souza", "Brasileira", CivilState.SINGLE, "Empresaria",
            "110.876.543-27", "MG-7654321", validAddress());
    Person tenant =
        JuridicalPerson.create("Acme Ltda", "11.222.333/0001-81", List.of(representative),
            validAddress());
    Receipt receipt = receiptWithTenant(LocalDate.of(2026, 12, 25), tenant);

    String fileName = service.defaultFileName(receipt);

    assertEquals("Recibo_25122026_Acme_Ltda.pdf", fileName);
  }

  @Test
  @DisplayName(
      "When tenant name has spaces and accented characters, should sanitize for the file system")
  void shouldSanitizeSpacesAndAccentedCharacters() {
    Person tenant =
        PhysicalPerson.create("João da Conceição", "Brasileiro", CivilState.SINGLE, "Engenheiro",
            "529.982.247-25", "MG-1234567", validAddress());
    Receipt receipt = receiptWithTenant(LocalDate.of(2026, 1, 9), tenant);

    String fileName = service.defaultFileName(receipt);

    assertEquals("Recibo_09012026_João_da_Conceição.pdf", fileName);
  }
}
