package com.guilherme.emobiliaria.shared.pdf;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyOccupationHistory;
import com.guilherme.emobiliaria.reports.domain.entity.VacancyTableRow;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.pdf.templates.ContractTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.OccupationRateTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyChartBean;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyInflationLagRowBean;
import com.guilherme.emobiliaria.shared.pdf.templates.ReceiptTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.RentEvolutionTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.VacancyTableRowBean;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.parser.PdfTextExtractor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
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

  private void assertFooterPeriodAppearsOnEveryPageAfterFirst(byte[] pdfBytes, String period)
      throws IOException {
    PdfReader reader = new PdfReader(pdfBytes);
    try {
      int pageCount = reader.getNumberOfPages();
      PdfTextExtractor textExtractor = new PdfTextExtractor(reader);
      assertTrue(pageCount > 1, "Expected a multipage PDF to validate footer continuity");
      for (int page = 2; page <= pageCount; page++) {
        String pageText = textExtractor.getTextFromPage(page);
        assertTrue(pageText.contains(period),
            "Expected footer period text on page " + page + " but it was missing.");
      }
    } finally {
      reader.close();
    }
  }

  private void assertFooterTotalPagesIsNotZero(byte[] pdfBytes) throws IOException {
    PdfReader reader = new PdfReader(pdfBytes);
    try {
      int pageCount = reader.getNumberOfPages();
      PdfTextExtractor textExtractor = new PdfTextExtractor(reader);
      assertTrue(pageCount > 1, "Expected a multipage PDF to validate page totals");
      for (int page = 1; page <= pageCount; page++) {
        String normalized = textExtractor.getTextFromPage(page).replaceAll("\\s+", " ").trim();
        assertFalse(normalized.matches(".*Pág\\.\\s*\\d+\\s*/\\s*0\\b.*"),
            "Expected non-zero total pages on page " + page + " but found '/ 0'.");
      }
    } finally {
      reader.close();
    }
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
      List<Long> totals = List.of(150000L, 158000L);
      List<Long> ipcaTotals = List.of(150000L, 154500L);
      List<Long> igpmTotals = List.of(150000L, 155500L);
      List<Long> ipcaGap = List.of(0L, 3500L);
      List<Long> igpmGap = List.of(0L, 2500L);
      BufferedImage portfolioInflationChart =
          chartGenerator.portfolioInflationComparison(months, totals, ipcaTotals, igpmTotals);
      BufferedImage portfolioGapChart =
          chartGenerator.portfolioInflationGap(months, ipcaGap, igpmGap);
      PropertyChartBean propertyChart = new PropertyChartBean("Imóvel Teste",
          chartGenerator.rentEvolution("Imóvel Teste", months, List.of(150000L, 160000L),
              List.of(151000L, 162000L), List.of(152000L, 163000L)));
      PropertyInflationLagRowBean lagRow =
          new PropertyInflationLagRowBean("Imóvel Teste", "R$ 1.600,00", "R$ -20,00 (-1,2%)",
              "R$ +10,00 (+0,6%)", "IPCA R$ -20,00 (-1,2%)", "Defasado");
      RentEvolutionTemplate template =
          new RentEvolutionTemplate(portfolioInflationChart, portfolioGapChart,
              List.of(propertyChart), List.of(lagRow), "17/04/2026", "Jan/2026 a Fev/2026",
              "Portfólio abaixo da inflação (IPCA e IGP-M)", "R$ -30,00 (-1,9%)",
              "R$ -20,00 (-1,3%)", "+5,3%", "IPCA -1,9% | IGP-M -1,3%");

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
      int totalProperties = 3;

      BufferedImage trendChart =
          chartGenerator.occupancyTrend(months, occupiedCounts, totalProperties);
      BufferedImage volumeChart =
          chartGenerator.vacancyVolume(months, occupiedCounts, totalProperties);
      List<PropertyOccupationHistory> histories =
          List.of(new PropertyOccupationHistory("Imóvel Teste", months, List.of(true, false)));
      BufferedImage heatmapChart = chartGenerator.vacancyHeatmap(months, histories);

      VacancyTableRowBean tableRow =
          new VacancyTableRowBean(new VacancyTableRow("Imóvel Teste", 1, 1, "Jan/26", "Vago"));
      OccupationRateTemplate template =
          new OccupationRateTemplate(trendChart, volumeChart, heatmapChart, "17/04/2026",
              "Jan/2026 a Fev/2026", "67%", "1", "16.7%", "1 meses", "Imóvel Teste",
              List.of(tableRow));

      byte[] result = service.generatePdf(template);

      assertNotNull(result);
      assertTrue(result.length > 0);
      assertTrue(isPdf(result));
    }

    @Test
    @DisplayName("Rent evolution summary pages should keep footer period text")
    void shouldRenderFooterOnAllRentEvolutionPages() throws IOException {
      ChartGenerator chartGenerator = new ChartGenerator();
      List<YearMonth> months = List.of(YearMonth.of(2026, 1), YearMonth.of(2026, 2));
      List<Long> totals = List.of(150000L, 158000L);
      List<Long> ipcaTotals = List.of(150000L, 154500L);
      List<Long> igpmTotals = List.of(150000L, 155500L);
      List<Long> ipcaGap = List.of(0L, 3500L);
      List<Long> igpmGap = List.of(0L, 2500L);
      String period = "Jan/2026 a Fev/2026";

      BufferedImage portfolioInflationChart =
          chartGenerator.portfolioInflationComparison(months, totals, ipcaTotals, igpmTotals);
      BufferedImage portfolioGapChart =
          chartGenerator.portfolioInflationGap(months, ipcaGap, igpmGap);

      List<PropertyChartBean> propertyCharts = new ArrayList<>();
      for (int i = 1; i <= 3; i++) {
        propertyCharts.add(new PropertyChartBean("Imóvel Teste " + i,
            chartGenerator.rentEvolution("Imóvel Teste " + i, months,
                List.of(150000L + i * 1000L, 160000L + i * 1000L),
                List.of(151000L + i * 1000L, 162000L + i * 1000L),
                List.of(152000L + i * 1000L, 163000L + i * 1000L))));
      }

      List<PropertyInflationLagRowBean> lagRows = List.of(
          new PropertyInflationLagRowBean("Imóvel 1", "R$ 1.600,00", "R$ -20,00 (-1,2%)",
              "R$ +10,00 (+0,6%)", "IPCA R$ -20,00 (-1,2%)", "Defasado"),
          new PropertyInflationLagRowBean("Imóvel 2", "R$ 1.800,00", "R$ +30,00 (+1,8%)",
              "R$ +20,00 (+1,1%)", "IGP-M R$ +20,00 (+1,1%)", "Acima"),
          new PropertyInflationLagRowBean("Imóvel 3", "R$ 2.000,00", "R$ -40,00 (-2,0%)",
              "R$ -10,00 (-0,5%)", "IPCA R$ -40,00 (-2,0%)", "Defasado"));

      RentEvolutionTemplate template =
          new RentEvolutionTemplate(portfolioInflationChart, portfolioGapChart, propertyCharts,
              lagRows, "17/04/2026", period, "Portfólio abaixo da inflação (IPCA e IGP-M)",
              "R$ -30,00 (-1,9%)", "R$ -20,00 (-1,3%)", "+5,3%", "IPCA -1,9% | IGP-M -1,3%");

      byte[] result = service.generatePdf(template);

      assertFooterPeriodAppearsOnEveryPageAfterFirst(result, period);
      assertFooterTotalPagesIsNotZero(result);
    }

    @Test
    @DisplayName("Occupation rate summary pages should keep footer period text")
    void shouldRenderFooterOnAllOccupationRatePages() throws IOException {
      ChartGenerator chartGenerator = new ChartGenerator();
      List<YearMonth> months =
          List.of(YearMonth.of(2026, 1), YearMonth.of(2026, 2), YearMonth.of(2026, 3),
              YearMonth.of(2026, 4));
      List<Integer> occupiedCounts = List.of(3, 2, 4, 3);
      int totalProperties = 5;
      String period = "Jan/2026 a Abr/2026";

      BufferedImage trendChart =
          chartGenerator.occupancyTrend(months, occupiedCounts, totalProperties);
      BufferedImage volumeChart =
          chartGenerator.vacancyVolume(months, occupiedCounts, totalProperties);

      List<PropertyOccupationHistory> histories = List.of(
          new PropertyOccupationHistory("Imóvel A", months, List.of(true, false, true, true)),
          new PropertyOccupationHistory("Imóvel B", months, List.of(false, false, true, false)),
          new PropertyOccupationHistory("Imóvel C", months, List.of(true, true, true, true)),
          new PropertyOccupationHistory("Imóvel D", months, List.of(false, true, false, false)));
      BufferedImage heatmapChart = chartGenerator.vacancyHeatmap(months, histories);

      List<VacancyTableRowBean> tableRows = new ArrayList<>();
      for (int i = 1; i <= 45; i++) {
        tableRows.add(new VacancyTableRowBean(
            new VacancyTableRow("Imóvel " + i, i % 8 + 1, i % 5 + 1, "Mar/26",
                i % 2 == 0 ? "Vago" : "Ocupado")));
      }

      OccupationRateTemplate template =
          new OccupationRateTemplate(trendChart, volumeChart, heatmapChart, "17/04/2026", period,
              "60%", "2", "40,0%", "4 meses", "Imóvel B", tableRows);

      byte[] result = service.generatePdf(template);

      assertFooterPeriodAppearsOnEveryPageAfterFirst(result, period);
      assertFooterTotalPagesIsNotZero(result);
    }
  }
}
