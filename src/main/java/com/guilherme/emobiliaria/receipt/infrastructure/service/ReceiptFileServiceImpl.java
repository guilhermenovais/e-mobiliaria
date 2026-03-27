package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptFileService;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.ReceiptTemplate;
import jakarta.inject.Inject;

public class ReceiptFileServiceImpl implements ReceiptFileService {

  private final PdfGenerationService pdfGenerationService;

  @Inject
  public ReceiptFileServiceImpl(PdfGenerationService pdfGenerationService) {
    this.pdfGenerationService = pdfGenerationService;
  }

  @Override
  public byte[] generateReceiptPdf(Receipt receipt) {
    return pdfGenerationService.generatePdf(new ReceiptTemplate(receipt));
  }
}
