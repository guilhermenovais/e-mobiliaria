package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
    return Property.create("Apto Centro", "Apartamento", "CEMIG-001", "COPASA-001", "IPTU-001",
        validAddress());
  }

  private Contract validContract() {
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, 150000, "Residencial",
        PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null),
        validProperty(), validLandlord(), List.of(validTenant()), List.of(), List.of());
  }

  private Receipt validReceipt() {
    return Receipt.create(
        LocalDate.of(2026, 3, 10),
        LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), 0, 0, "Observation", validContract());
  }

  private Receipt receiptWithDiscountAndFine() {
    return Receipt.create(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 1),
        LocalDate.of(2026, 3, 31), 10000, 5000, null, validContract());
  }

  private List<ReceiptTemplate.ValueRowBean> valuesRows(ReceiptTemplate template) {
    EnumMap<ReceiptTemplate.ReceiptCollections, Collection<Object>> collections =
        template.getCollections();
    Collection<Object> rows = collections.get(ReceiptTemplate.ReceiptCollections.VALUES_TABLE_DATA);
    List<ReceiptTemplate.ValueRowBean> castedRows = new ArrayList<>();
    rows.forEach(row -> castedRows.add((ReceiptTemplate.ValueRowBean) row));
    return castedRows;
  }

  @Nested
  class GetParameters {

    @Test
    @DisplayName("When given a receipt, should return receipt text based on receipt data")
    void shouldReturnReceiptText() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals(
          "<html><body style='font-family: Arial;'>" + "Recebemos de João Silva, CPF sob o n° 529.982.247-25 a quantia de R$ 1.500,00 (mil e quinhentos reais)" + " referente a aluguel de imóvel sito à Praça da Sé nº 1, Sé, São Paulo-SP, no período de 01/03/26 a 31/03/26." + "</body></html>",
          params.get(ReceiptTemplate.ReceiptParameters.RECEIPT_TEXT));
    }

    @Test
    @DisplayName("When given a receipt, should return sample receipt title")
    void shouldReturnReceiptTitle() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("RECIBO DE ALUGUEL",
          params.get(ReceiptTemplate.ReceiptParameters.RECEIPT_TITLE));
    }

    @Test
    @DisplayName(
        "When given a receipt, should return values table data collection in getCollections")
    void shouldNotExposeValuesTableAsParameter() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals(5, params.size());
    }

    @Test
    @DisplayName("When discount and fine are present, should calculate paid total in receipt text")
    void shouldCalculatePaidTotal() {
      ReceiptTemplate template = new ReceiptTemplate(receiptWithDiscountAndFine());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals(
          "<html><body style='font-family: Arial;'>" + "Recebemos de João Silva, CPF sob o n° 529.982.247-25 a quantia de R$ 1.450,00 (mil e quatrocentos e cinquenta reais)" + " referente a aluguel de imóvel sito à Praça da Sé nº 1, Sé, São Paulo-SP, no período de 01/03/26 a 31/03/26." + "</body></html>",
          params.get(ReceiptTemplate.ReceiptParameters.RECEIPT_TEXT));
    }

    @Test
    @DisplayName("When given a receipt, should return sample city and date text")
    void shouldReturnCityAndDateText() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals(
          "<html><body style='font-family: Arial; text-align: center;'>" + "São Paulo, 10 de março de 2026." + "</body></html>",
          params.get(ReceiptTemplate.ReceiptParameters.CITY_AND_DATE_TEXT));
    }

    @Test
    @DisplayName("When given a receipt, should return with observation")
    void shouldReturnObservations() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("Observation",
          params.get(ReceiptTemplate.ReceiptParameters.OBSERVATIONS));
    }

    @Test
    @DisplayName("When given a receipt, should return landlord signing text based on landlord data")
    void shouldReturnLandlordSigningText() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("Maria Souza, CPF: 529.982.247-25",
          params.get(ReceiptTemplate.ReceiptParameters.LANDLORD_SIGNING_TEXT));
    }
  }

  @Nested
  class GetCollections {

    @Test
    @DisplayName("When given a receipt, should return values table rows")
    void shouldReturnValuesTableRows() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptCollections, Collection<Object>> collections = template.getCollections();

      assertEquals(1, collections.size());
      List<ReceiptTemplate.ValueRowBean> rows = valuesRows(template);
      assertEquals(4, rows.size());
      assertEquals("aluguel do período", rows.get(0).getLabel());
      assertEquals("R$ 1.500,00", rows.get(0).getValue());
      assertEquals("desconto", rows.get(1).getLabel());
      assertEquals("R$ 0,00", rows.get(1).getValue());
      assertEquals("multa", rows.get(2).getLabel());
      assertEquals("R$ 0,00", rows.get(2).getValue());
      assertEquals("valor pago", rows.get(3).getLabel());
      assertEquals("R$ 1.500,00", rows.get(3).getValue());
    }

    @Test
    @DisplayName("When discount and fine are present, should return calculated paid total row")
    void shouldReturnCalculatedTotalInValuesTableRows() {
      ReceiptTemplate template = new ReceiptTemplate(receiptWithDiscountAndFine());

      List<ReceiptTemplate.ValueRowBean> rows = valuesRows(template);

      assertEquals("R$ 1.450,00", rows.get(3).getValue());
    }
  }
}
