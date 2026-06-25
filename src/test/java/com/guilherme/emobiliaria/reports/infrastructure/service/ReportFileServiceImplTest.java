package com.guilherme.emobiliaria.reports.infrastructure.service;

import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRowStatus;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyRentHistory;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyInflationLagRowBean;
import com.guilherme.emobiliaria.shared.pdf.templates.RentEvolutionTemplate;
import net.sf.jasperreports.engine.JRDataSource;
import net.sf.jasperreports.engine.JRException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ReportFileServiceImplTest {

  private ResourceBundle bundle() {
    return ResourceBundle.getBundle("messages", Locale.forLanguageTag("pt-BR"));
  }

  @Test
  @DisplayName("Rent evolution report should compute inflation KPIs and rank biggest lag first")
  void shouldComputeInflationKpisAndRanking() {
    CapturingPdfGenerationService pdfService = new CapturingPdfGenerationService();
    ReportFileServiceImpl service =
        new ReportFileServiceImpl(pdfService, new ChartGenerator(bundle()), bundle());

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

  private List<PaymentReportRowBean> extractBeans(JRDataSource dataSource) {
    List<PaymentReportRowBean> beans = new ArrayList<>();
    try {
      while (dataSource.next()) {
        String propertyName = (String) dataSource.getFieldValue(new SimpleJRField("propertyName"));
        String pageGroup = (String) dataSource.getFieldValue(new SimpleJRField("pageGroup"));
        beans.add(new BeanProxy(propertyName, pageGroup));
      }
    } catch (JRException e) {
      throw new RuntimeException(e);
    }
    return beans;
  }


  private static class BeanProxy extends PaymentReportRowBean {
    private final String propertyName;
    private final String pageGroup;

    BeanProxy(String propertyName, String pageGroup) {
      super(new PaymentReportRow(propertyName, null, null, null, null, null, null,
          PaymentReportRowStatus.VACANT));
      this.propertyName = propertyName;
      this.pageGroup = pageGroup;
    }

    @Override
    public String getPropertyName() {
      return propertyName;
    }

    @Override
    public String getPageGroup() {
      return pageGroup;
    }
  }


  private record SimpleJRField(String name) implements net.sf.jasperreports.engine.JRField {
    @Override
    public String getName() {
      return name;
    }

    @Override
    public String getDescription() {
      return name;
    }

    @Override
    public void setDescription(String description) {
    }

    @Override
    public Class<?> getValueClass() {
      return String.class;
    }

    @Override
    public String getValueClassName() {
      return String.class.getName();
    }

    @Override
    public net.sf.jasperreports.engine.JRPropertyExpression[] getPropertyExpressions() {
      return new net.sf.jasperreports.engine.JRPropertyExpression[0];
    }

    @Override
    public boolean hasProperties() {
      return false;
    }

    @Override
    public net.sf.jasperreports.engine.JRPropertiesMap getPropertiesMap() {
      return null;
    }

    @Override
    public net.sf.jasperreports.engine.JRPropertiesHolder getParentProperties() {
      return null;
    }

    @Override
    public Object clone() {
      return this;
    }
  }


  private static class CapturingPdfGenerationService extends PdfGenerationService {

    private PdfTemplate<?, ?> capturedTemplate;
    private JRDataSource capturedDataSource;

    @Override
    public byte[] generatePdf(PdfTemplate<?, ?> pdfTemplate) {
      this.capturedTemplate = pdfTemplate;
      return new byte[] {0x25, 0x50, 0x44, 0x46};
    }

    @Override
    public byte[] generatePdf(PdfTemplate<?, ?> pdfTemplate, JRDataSource dataSource) {
      this.capturedTemplate = pdfTemplate;
      this.capturedDataSource = dataSource;
      return new byte[] {0x25, 0x50, 0x44, 0x46};
    }
  }


  @Nested
  class GeneratePaymentReportPdf {

    @Test
    @DisplayName(
        "When paid rows have different receipt dates, should sort by receipt date ascending with unpaid/vacant at end")
    void shouldSortPaidRowsByReceiptDateWithUnpaidAtEnd() {
      CapturingPdfGenerationService pdfService = new CapturingPdfGenerationService();
      ReportFileServiceImpl service =
          new ReportFileServiceImpl(pdfService, new ChartGenerator(bundle()), bundle());

      List<PaymentReportRow> rows = List.of(
          new PaymentReportRow("Prop C", "Tenant C", "111", null, 100000, null, null,
              PaymentReportRowStatus.UNPAID),
          new PaymentReportRow("Prop B", "Tenant B", "222", LocalDate.of(2026, 5, 20), 120000,
              LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), PaymentReportRowStatus.PAID),
          new PaymentReportRow("Prop A", "Tenant A", "333", LocalDate.of(2026, 5, 5), 130000,
              LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 31), PaymentReportRowStatus.PAID),
          new PaymentReportRow("Prop D", null, null, null, null, null, null,
              PaymentReportRowStatus.VACANT));

      service.generatePaymentReportPdf(rows, YearMonth.of(2026, 5));

      assertNotNull(pdfService.capturedDataSource);
      List<PaymentReportRowBean> beans = extractBeans(pdfService.capturedDataSource);
      assertEquals(4, beans.size());
      assertEquals("Prop A", beans.get(0).getPropertyName());
      assertEquals("Prop B", beans.get(1).getPropertyName());
      assertEquals("Prop C", beans.get(2).getPropertyName());
      assertEquals("Prop D", beans.get(3).getPropertyName());
    }
  }
}
