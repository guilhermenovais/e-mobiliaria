package com.guilherme.emobiliaria.reports.infrastructure.service;

import com.guilherme.emobiliaria.reports.domain.entity.PropertyRentHistory;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyInflationLagRowBean;
import com.guilherme.emobiliaria.shared.pdf.templates.RentEvolutionTemplate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportFileServiceImplTest {

  @Test
  @DisplayName("Rent evolution report should compute inflation KPIs and rank biggest lag first")
  void shouldComputeInflationKpisAndRanking() {
    CapturingPdfGenerationService pdfService = new CapturingPdfGenerationService();
    ReportFileServiceImpl service = new ReportFileServiceImpl(pdfService, new ChartGenerator());

    List<YearMonth> months = List.of(YearMonth.of(2026, 1), YearMonth.of(2026, 2));
    PropertyRentHistory propertyA =
        new PropertyRentHistory("Imóvel A", months, List.of(100000L, 110000L),
            List.of(100000L, 103000L), List.of(100000L, 104000L));
    PropertyRentHistory propertyB =
        new PropertyRentHistory("Imóvel B", months, List.of(50000L, 48000L),
            List.of(50000L, 52000L), List.of(50000L, 51000L));
    RentEvolutionData data =
        new RentEvolutionData(months, List.of(150000L, 158000L), List.of(propertyA, propertyB));

    byte[] result = service.generateRentEvolutionPdf(data);

    assertNotNull(result);
    assertEquals('%', result[0]);
    assertNotNull(pdfService.capturedTemplate);

    RentEvolutionTemplate template =
        assertInstanceOf(RentEvolutionTemplate.class, pdfService.capturedTemplate);
    Map<RentEvolutionTemplate.RentEvolutionParameters, Object> params = template.getParameters();

    assertEquals("Portfólio acima da inflação (IPCA e IGP-M)",
        params.get(RentEvolutionTemplate.RentEvolutionParameters.KPI_STATUS_HEADLINE));
    assertEquals("R$ +30,00 (+1,9%)",
        params.get(RentEvolutionTemplate.RentEvolutionParameters.KPI_GAP_VS_IPCA));
    assertEquals("R$ +30,00 (+1,9%)",
        params.get(RentEvolutionTemplate.RentEvolutionParameters.KPI_GAP_VS_IGPM));
    assertEquals("+5,3%",
        params.get(RentEvolutionTemplate.RentEvolutionParameters.KPI_NOMINAL_GROWTH));
    assertEquals("IPCA +1,9% | IGP-M +1,9%",
        params.get(RentEvolutionTemplate.RentEvolutionParameters.KPI_REAL_GROWTH));

    List<Object> rankingRows = template.getCollections()
        .get(RentEvolutionTemplate.RentEvolutionCollections.PROPERTY_INFLATION_LAG_ROWS).stream()
        .toList();
    assertEquals(2, rankingRows.size());
    PropertyInflationLagRowBean firstRow =
        assertInstanceOf(PropertyInflationLagRowBean.class, rankingRows.get(0));
    assertEquals("Imóvel B", firstRow.getProperty_name());
    assertEquals("Defasado", firstRow.getStatus());
  }

  private static class CapturingPdfGenerationService extends PdfGenerationService {

    private PdfTemplate<?, ?> capturedTemplate;

    @Override
    public byte[] generatePdf(PdfTemplate<?, ?> pdfTemplate) {
      this.capturedTemplate = pdfTemplate;
      return new byte[] {0x25, 0x50, 0x44, 0x46};
    }
  }
}
