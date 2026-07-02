package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptFileService;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.ReceiptTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.TemplateFormatter;
import jakarta.inject.Inject;

import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class ReceiptFileServiceImpl implements ReceiptFileService {

  private static final DateTimeFormatter FILE_NAME_DATE_FMT =
      DateTimeFormatter.ofPattern("ddMMyyyy");

  private final PdfGenerationService pdfGenerationService;
  private final ResourceBundle bundle;

  @Inject
  public ReceiptFileServiceImpl(PdfGenerationService pdfGenerationService, ResourceBundle bundle) {
    this.pdfGenerationService = pdfGenerationService;
    this.bundle = bundle;
  }

  @Override
  public byte[] generateReceiptPdf(Receipt receipt) {
    return pdfGenerationService.generatePdf(new ReceiptTemplate(receipt, bundle));
  }

  @Override
  public String defaultFileName(Receipt receipt) {
    String tenantName = TemplateFormatter.personName(receipt.getContract().getTenants().getFirst());
    String sanitized = tenantName.replaceAll("\\s+", "_").replaceAll("[\\\\/:*?\"<>|]", "");
    return "Recibo_" + receipt.getDate().format(FILE_NAME_DATE_FMT) + "_" + sanitized + ".pdf";
  }
}
