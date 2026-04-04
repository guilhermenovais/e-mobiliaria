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
    @DisplayName("When given a receipt, should return sample receipt text")
    void shouldReturnReceiptText() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals(
          "<html><body style='font-family: Arial;'>" + "Recebemos de João Silva a importância de R$ 1.500,00 referente ao aluguel do mês de março de 2026." + "</body></html>",
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
    @DisplayName("When given a receipt, should return sample values table RTF")
    void shouldReturnValuesTableRtf() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals(
          "{\\rtf1\\ansi\\deff0\\fs20" + "\\b Valores\\b0\\par" + "Aluguel: R$ 1.500,00\\par" + "Desconto: R$ 0,00\\par" + "Multa: R$ 0,00\\par" + "Total pago: R$ 1.500,00\\par" + "}",
          params.get(ReceiptTemplate.ReceiptParameters.VALUES_TABLE_RTF));
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
    @DisplayName("When given a receipt, should return sample observations")
    void shouldReturnObservations() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("{\\rtf1\\ansi\\deff0\\fs20 Sem observações adicionais.\\par}",
          params.get(ReceiptTemplate.ReceiptParameters.OBSERVATIONS));
    }

    @Test
    @DisplayName("When given a receipt, should return sample landlord signing text")
    void shouldReturnLandlordSigningText() {
      ReceiptTemplate template = new ReceiptTemplate(validReceipt());

      EnumMap<ReceiptTemplate.ReceiptParameters, Object> params = template.getParameters();

      assertEquals("Maria Souza",
          params.get(ReceiptTemplate.ReceiptParameters.LANDLORD_SIGNING_TEXT));
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
