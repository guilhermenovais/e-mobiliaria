package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.receipt.application.input.GenerateReceiptPdfInput;
import com.guilherme.emobiliaria.receipt.application.output.GenerateReceiptPdfOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptFileService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class GenerateReceiptPdfInteractor {

  private final ReceiptRepository receiptRepository;
  private final ReceiptFileService receiptFileService;

  @Inject
  public GenerateReceiptPdfInteractor(ReceiptRepository receiptRepository,
      ReceiptFileService receiptFileService) {
    this.receiptRepository = receiptRepository;
    this.receiptFileService = receiptFileService;
  }

  public GenerateReceiptPdfOutput execute(GenerateReceiptPdfInput input) {
    Receipt receipt = receiptRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    byte[] pdfBytes = receiptFileService.generateReceiptPdf(receipt);
    return new GenerateReceiptPdfOutput(pdfBytes);
  }
}
