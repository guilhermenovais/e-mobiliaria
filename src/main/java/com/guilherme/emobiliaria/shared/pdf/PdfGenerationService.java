package com.guilherme.emobiliaria.shared.pdf;

import net.sf.jasperreports.engine.JREmptyDataSource;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import net.sf.jasperreports.engine.util.JRLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class PdfGenerationService {
  private static final Logger log = LoggerFactory.getLogger(PdfGenerationService.class);
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
    ClassLoader reportClassLoader = resolveReportClassLoader();
    ClassLoader originalContextClassLoader = Thread.currentThread().getContextClassLoader();
    String resourcePath =
        templatePath.startsWith("/") ? templatePath.substring(1) : templatePath;
    URL subreportDirUrl = resolveRequiredResourceUrl(SUBREPORT_DIR, reportClassLoader);

    try (InputStream reportStream = openTemplateStream(templatePath, resourcePath, reportClassLoader)) {
      if (reportStream == null) {
        throw new RuntimeException("Report template not found at: " + templatePath);
      }
      Thread.currentThread().setContextClassLoader(reportClassLoader);
      params.put(JRParameter.REPORT_CLASS_LOADER, reportClassLoader);
      params.put(SUBREPORT_DIR_PARAM, subreportDirUrl.toExternalForm());
      JasperReport jasperReport = (JasperReport) JRLoader.loadObject(reportStream);
      JasperPrint jasperPrint =
          JasperFillManager.fillReport(jasperReport, params, new JREmptyDataSource());
      return JasperExportManager.exportReportToPdf(jasperPrint);
    } catch (Exception e) {
      log.error("Error generating JasperReport PDF for template {}", templatePath, e);
      throw new RuntimeException("Error generating JasperReport PDF", e);
    } finally {
      Thread.currentThread().setContextClassLoader(originalContextClassLoader);
    }
  }

  private InputStream openTemplateStream(String templatePath, String resourcePath,
      ClassLoader reportClassLoader) {
    InputStream reportStream = getClass().getResourceAsStream(templatePath);
    if (reportStream != null) {
      return reportStream;
    }
    reportStream = reportClassLoader.getResourceAsStream(resourcePath);
    if (reportStream != null) {
      return reportStream;
    }
    try {
      Path sourceTemplate = Path.of("src", "main", "resources", resourcePath.replace("/", "\\"));
      if (Files.exists(sourceTemplate)) {
        return Files.newInputStream(sourceTemplate);
      }
      return null;
    } catch (Exception e) {
      throw new RuntimeException("Error opening report template at: " + templatePath, e);
    }
  }

  private URL resolveRequiredResourceUrl(String resourcePath, ClassLoader reportClassLoader) {
    String normalized = resourcePath.startsWith("/") ? resourcePath.substring(1) : resourcePath;
    URL url = reportClassLoader.getResource(normalized);
    if (url == null) {
      Path sourceResource = Path.of("src", "main", "resources", normalized.replace("/", "\\"));
      if (Files.exists(sourceResource)) {
        try {
          return sourceResource.toUri().toURL();
        } catch (Exception e) {
          throw new RuntimeException("Error resolving resource at: " + resourcePath, e);
        }
      }
      throw new RuntimeException("Resource not found at: " + resourcePath);
    }
    return url;
  }

  private ClassLoader resolveReportClassLoader() {
    ClassLoader classLoader = getClass().getClassLoader();
    if (classLoader != null) {
      return classLoader;
    }
    ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
    if (contextClassLoader != null) {
      return contextClassLoader;
    }
    throw new RuntimeException("No classloader available to load report resources");
  }
}
