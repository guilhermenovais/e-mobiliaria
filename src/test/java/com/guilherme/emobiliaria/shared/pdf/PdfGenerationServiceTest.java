package com.guilherme.emobiliaria.shared.pdf;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.pdf.templates.ContractTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.ReceiptTemplate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PdfGenerationServiceTest {

  private PdfGenerationService service;

  @BeforeEach
  void setUp() {
    service = new PdfGenerationService();
  }

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private PhysicalPerson validLandlord() {
    return PhysicalPerson.create("Maria Souza", "Brasileira", CivilState.MARRIED, "Empresária",
        "529.982.247-25", "RG-9876543", validAddress());
  }

  private PhysicalPerson validTenant() {
    return PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
        "529.982.247-25", "MG-1234567", validAddress());
  }

  private Contract validContract() {
    Property property =
        Property.create("Apto Centro", "Apartamento", Purpose.RESIDENTIAL, 150000, "CEMIG-001",
            "COPASA-001", "IPTU-001", validAddress());
    PaymentAccount account = PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, account, property,
        validLandlord(), List.of(validTenant()));
  }

  private boolean isPdf(byte[] bytes) {
    return bytes.length >= 4 && bytes[0] == '%' && bytes[1] == 'P' && bytes[2] == 'D' && bytes[3] == 'F';
  }


  @Nested
  class GeneratePdf {

    @Test
    @DisplayName("When given a ContractTemplate, should return valid PDF bytes")
    void shouldGenerateContractPdf() {
      ContractTemplate template = new ContractTemplate(validContract());

      byte[] result = service.generatePdf(template);

      assertNotNull(result);
      assertTrue(result.length > 0);
      assertTrue(isPdf(result));
    }

    @Test
    @DisplayName("When given a ReceiptTemplate, should return valid PDF bytes")
    void shouldGenerateReceiptPdf() {
      Receipt receipt = Receipt.create(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 3, 1),
          LocalDate.of(2026, 3, 31), 0, 0, validContract());
      ReceiptTemplate template = new ReceiptTemplate(receipt);

      byte[] result = service.generatePdf(template);

      assertNotNull(result);
      assertTrue(result.length > 0);
      assertTrue(isPdf(result));
    }
  }
}
