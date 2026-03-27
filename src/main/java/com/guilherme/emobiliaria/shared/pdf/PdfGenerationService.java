package com.guilherme.emobiliaria.shared.pdf;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class PdfGenerationService {
  private static final String TEMPLATES_BASE_PATH = "/reports/";
  private static final String SUBREPORT_DIR_PARAM = "SUBREPORT_DIR";
  private static final String SUBREPORT_DIR = "reports/";

  public byte[] generatePdf(PdfTemplate<?, ?> pdfTemplate) {
    String templatePath = getTemplatePath(pdfTemplate);
    Map<String, Object> parameters = getReportParameters(pdfTemplate);
    return generateJasperReport(templatePath, parameters);
  }

  private Map<String, Object> getReportParameters(PdfTemplate<?, ?> pdfTemplate) {
    Map<String, Object> parameters = new HashMap<>();
    parameters.put(SUBREPORT_DIR_PARAM, SUBREPORT_DIR);
    pdfTemplate.getParameters()
        .forEach((key, value) -> parameters.put(key.name().toLowerCase(), value));
    pdfTemplate.getCollections().forEach((key, value) -> parameters.put(key.name().toLowerCase(),
        new JRBeanCollectionDataSource(value)));
    return parameters;
  }

  private String getTemplatePath(PdfTemplate<?, ?> pdfTemplate) {
    return TEMPLATES_BASE_PATH + pdfTemplate.getTemplateName() + ".jasper";
  }

  private byte[] generateJasperReport(String templatePath, Map<String, Object> params) {
    try (InputStream reportStream = getClass().getResourceAsStream(templatePath)) {
      JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
      JasperPrint jasperPrint =
          JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
      return JasperExportManager.exportReportToPdf(jasperPrint);
    } catch (Exception e) {
      throw new RuntimeException("Error generating JasperReport PDF", e);
    }
  }
}
