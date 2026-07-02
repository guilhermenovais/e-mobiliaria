package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.receipt.application.input.GenerateReceiptPdfInput;
import com.guilherme.emobiliaria.receipt.application.output.GenerateReceiptPdfOutput;
import com.guilherme.emobiliaria.receipt.application.output.SkippedProofInfo;
import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.PaymentProofRepository;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofPdfEmbeddingService;
import com.guilherme.emobiliaria.receipt.domain.service.ProofEmbeddingResult;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptFileService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

import java.util.List;

public class GenerateReceiptPdfInteractor {

  private final ReceiptRepository receiptRepository;
  private final ReceiptFileService receiptFileService;
  private final PaymentProofRepository paymentProofRepository;
  private final PaymentProofPdfEmbeddingService paymentProofPdfEmbeddingService;

  @Inject
  public GenerateReceiptPdfInteractor(ReceiptRepository receiptRepository,
      ReceiptFileService receiptFileService, PaymentProofRepository paymentProofRepository,
      PaymentProofPdfEmbeddingService paymentProofPdfEmbeddingService) {
    this.receiptRepository = receiptRepository;
    this.receiptFileService = receiptFileService;
    this.paymentProofRepository = paymentProofRepository;
    this.paymentProofPdfEmbeddingService = paymentProofPdfEmbeddingService;
  }

  public GenerateReceiptPdfOutput execute(GenerateReceiptPdfInput input) {
    Receipt receipt = receiptRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    List<PaymentProof> proofs = paymentProofRepository.findAllByReceiptId(receipt.getId());
    byte[] baseBytes = receiptFileService.generateReceiptPdf(receipt);
    ProofEmbeddingResult result = paymentProofPdfEmbeddingService.embed(baseBytes, proofs);
    List<SkippedProofInfo> skippedProofs = result.skippedProofs().stream()
        .map(skipped -> new SkippedProofInfo(skipped.proof().getDisplayName(), skipped.reason()))
        .toList();
    return new GenerateReceiptPdfOutput(result.pdfBytes(), skippedProofs);
  }
}
