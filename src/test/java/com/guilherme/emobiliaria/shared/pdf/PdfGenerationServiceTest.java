package com.guilherme.emobiliaria.shared.pdf;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.pdf.templates.ContractTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.OccupationRateTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyChartBean;
import com.guilherme.emobiliaria.shared.pdf.templates.ReceiptTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.RentEvolutionTemplate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
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
        Property.create("Apto Centro", "Apartamento", "CEMIG-001", "COPASA-001", "IPTU-001",
            validAddress());
    PaymentAccount account = PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, 150000, "Residencial",
        account, property, validLandlord(), List.of(validTenant()), List.of(), List.of());
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
          LocalDate.of(2026, 3, 31), 0, 0, null, validContract());
      ReceiptTemplate template = new ReceiptTemplate(receipt);

      byte[] result = service.generatePdf(template);

      assertNotNull(result);
      assertTrue(result.length > 0);
      assertTrue(isPdf(result));
    }

    @Test
    @DisplayName("When given a RentEvolutionTemplate, should return valid PDF bytes")
    void shouldGenerateRentEvolutionPdf() {
      ChartGenerator chartGenerator = new ChartGenerator();
      List<YearMonth> months = List.of(YearMonth.of(2026, 1), YearMonth.of(2026, 2));
      List<Long> totals = List.of(150000L, 160000L);
      BufferedImage monthlyChart = chartGenerator.monthlyEarnings(months, totals);
      PropertyChartBean propertyChart = new PropertyChartBean("Imóvel Teste",
          chartGenerator.rentEvolution("Imóvel Teste", months,
              List.of(150000L, 160000L), List.of(151000L, 162000L), List.of(152000L, 163000L)));
      RentEvolutionTemplate template = new RentEvolutionTemplate(monthlyChart,
          List.of(propertyChart), "17/04/2026", "Jan/2026 a Fev/2026");

      byte[] result = service.generatePdf(template);

      assertNotNull(result);
      assertTrue(result.length > 0);
      assertTrue(isPdf(result));
    }

    @Test
    @DisplayName("When given an OccupationRateTemplate, should return valid PDF bytes")
    void shouldGenerateOccupationRatePdf() {
      ChartGenerator chartGenerator = new ChartGenerator();
      List<YearMonth> months = List.of(YearMonth.of(2026, 1), YearMonth.of(2026, 2));
      List<Integer> occupiedCounts = List.of(1, 2);
      BufferedImage overallChart = chartGenerator.overallOccupation(months, occupiedCounts, 3);
      PropertyChartBean propertyChart = new PropertyChartBean("Imóvel Teste",
          chartGenerator.propertyOccupation("Imóvel Teste", months, List.of(true, false)));
      OccupationRateTemplate template = new OccupationRateTemplate(overallChart,
          List.of(propertyChart), "17/04/2026", "Jan/2026 a Fev/2026");

      byte[] result = service.generatePdf(template);

      assertNotNull(result);
      assertTrue(result.length > 0);
      assertTrue(isPdf(result));
    }
  }
}
