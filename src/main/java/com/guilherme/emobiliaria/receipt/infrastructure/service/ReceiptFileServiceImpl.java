package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptFileService;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.ReceiptTemplate;
import jakarta.inject.Inject;

import java.util.ResourceBundle;

public class ReceiptFileServiceImpl implements ReceiptFileService {

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
}
