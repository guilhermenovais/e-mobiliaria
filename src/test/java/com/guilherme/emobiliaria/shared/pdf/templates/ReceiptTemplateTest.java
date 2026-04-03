package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReceiptTemplateTest {

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo", BrazilianState.SP);
  }

  private PhysicalPerson validLandlord() {
    return PhysicalPerson.create("Maria Souza", "Brasileira", CivilState.MARRIED,
        "Empresária", "529.982.247-25", "RG-9876543", validAddress());
  }

  private PhysicalPerson validTenant() {
    return PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE,
        "Engenheiro", "529.982.247-25", "MG-1234567", validAddress());
  }

  private Property validProperty() {
    return Property.create("Apto Centro", "Apartamento", Purpose.RESIDENTIAL,
        "CEMIG-001", "COPASA-001", "IPTU-001", validAddress());
  }

  private Contract validContract() {
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10,
        150000,
        PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null),
        validProperty(), validLandlord(), List.of(validTenant()));
  }

  private Receipt validReceipt() {
    return Receipt.create(
        LocalDate.of(2026, 3, 10),
        LocalDate.of(2026, 3, 1),
        LocalDate.of(2026, 3, 31),
        0, 0, validContract());
  }

  @Nested
  class GetParameters {

    @Test
    @DisplayName("When given a receipt, should return tenant name as payer identification")
    void shouldReturnPayerIdentification() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("João Silva", params.get(ReceiptTemplate.ReceiptParameters.PAYER_IDENTIFICATION));
    }

    @Test
    @DisplayName("When given a receipt, should return rent value in full Portuguese words")
    void shouldReturnPaymentValueInFull() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("mil e quinhentos reais", params.get(ReceiptTemplate.ReceiptParameters.PAYMENT_VALUE_IN_FULL));
    }

    @Test
    @DisplayName("When given a receipt, should return formatted period")
    void shouldReturnPeriod() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("01/03/2026 a 31/03/2026", params.get(ReceiptTemplate.ReceiptParameters.PERIOD));
    }

    @Test
    @DisplayName("When receipt has no discount, should return zero formatted")
    void shouldReturnZeroDiscount() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("R$ 0,00", params.get(ReceiptTemplate.ReceiptParameters.DISCOUNT));
    }

    @Test
    @DisplayName("When receipt has a discount, should return formatted discount")
    void shouldReturnDiscount() {
      Receipt receipt = Receipt.create(LocalDate.of(2026, 3, 10),
          LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31),
          5000, 0, validContract());
      ReceiptTemplate template = new ReceiptTemplate(receipt);

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("R$ 50,00", params.get(ReceiptTemplate.ReceiptParameters.DISCOUNT));
    }

    @Test
    @DisplayName("When receipt has no fine, should return zero formatted")
    void shouldReturnZeroFine() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("R$ 0,00", params.get(ReceiptTemplate.ReceiptParameters.FINE));
    }

    @Test
    @DisplayName("When receipt has a fine, should return formatted fine")
    void shouldReturnFine() {
      Receipt receipt = Receipt.create(LocalDate.of(2026, 3, 10),
          LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31),
          0, 10000, validContract());
      ReceiptTemplate template = new ReceiptTemplate(receipt);

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("R$ 100,00", params.get(ReceiptTemplate.ReceiptParameters.FINE));
    }

    @Test
    @DisplayName("When receipt has no discount or fine, should return rent as payed value")
    void shouldReturnPayedValueEqualToRent() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("R$ 1.500,00", params.get(ReceiptTemplate.ReceiptParameters.PAYED_VALUE));
    }

    @Test
    @DisplayName("When receipt has discount and fine, should return rent minus discount plus fine")
    void shouldReturnPayedValueWithDiscountAndFine() {
      Receipt receipt = Receipt.create(LocalDate.of(2026, 3, 10),
          LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31),
          5000, 10000, validContract());
      ReceiptTemplate template = new ReceiptTemplate(receipt);

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("R$ 1.550,00", params.get(ReceiptTemplate.ReceiptParameters.PAYED_VALUE));
    }

    @Test
    @DisplayName("When given a receipt, should return landlord city")
    void shouldReturnLandlordCity() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("São Paulo", params.get(ReceiptTemplate.ReceiptParameters.LANDLORD_CITY));
    }

    @Test
    @DisplayName("When given a receipt, should return formatted receipt date")
    void shouldReturnReceiptDate() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("10/03/2026", params.get(ReceiptTemplate.ReceiptParameters.RECEIPT_DATE));
    }

    @Test
    @DisplayName("When given a receipt, should return landlord identification with name and description")
    void shouldReturnLandlordIdentification() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      String identification = (String) params.get(ReceiptTemplate.ReceiptParameters.LANDLORD_IDENTIFICATION);
      assertTrue(identification.startsWith("Maria Souza, "));
      assertTrue(identification.contains("529.982.247-25"));
    }
  }

  @Nested
  class GetCollections {

    @Test
    @DisplayName("When given a receipt, should return an empty collections map")
    void shouldReturnEmptyCollections() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptCollections, Collection<Object>> collections = template.getCollections();

      assertTrue(collections.isEmpty());
    }
  }
}
