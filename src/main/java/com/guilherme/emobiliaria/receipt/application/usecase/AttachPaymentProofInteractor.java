package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.receipt.application.input.AttachPaymentProofFromBytesInput;
import com.guilherme.emobiliaria.receipt.application.input.AttachPaymentProofFromFileInput;
import com.guilherme.emobiliaria.receipt.application.output.AttachPaymentProofOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import com.guilherme.emobiliaria.receipt.domain.repository.PaymentProofRepository;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofStorageService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

import java.time.LocalDate;

public class AttachPaymentProofInteractor {

  private final ReceiptRepository receiptRepository;
  private final PaymentProofRepository proofRepository;
  private final PaymentProofStorageService storageService;

  @Inject
  public AttachPaymentProofInteractor(ReceiptRepository receiptRepository,
      PaymentProofRepository proofRepository, PaymentProofStorageService storageService) {
    this.receiptRepository = receiptRepository;
    this.proofRepository = proofRepository;
    this.storageService = storageService;
  }

  public AttachPaymentProofOutput execute(AttachPaymentProofFromFileInput input) {
    receiptRepository.findById(input.receiptId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    ProofFileType fileType = ProofFileType.fromExtension(input.originalFileName())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PaymentProof.UNSUPPORTED_FILE_TYPE));
    String storedName = storageService.copyToStorage(input.sourceFile(), input.originalFileName());
    PaymentProof proof =
        PaymentProof.create(input.originalFileName(), input.displayName(), storedName, fileType,
            LocalDate.now(), input.receiptId());
    return new AttachPaymentProofOutput(proofRepository.create(proof));
  }

  public AttachPaymentProofOutput execute(AttachPaymentProofFromBytesInput input) {
    receiptRepository.findById(input.receiptId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    ProofFileType fileType = ProofFileType.fromExtension(input.originalFileName())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PaymentProof.UNSUPPORTED_FILE_TYPE));
    String storedName =
        storageService.copyBytesToStorage(input.imageBytes(), input.originalFileName());
    PaymentProof proof =
        PaymentProof.create(input.originalFileName(), input.displayName(), storedName, fileType,
            LocalDate.now(), input.receiptId());
    return new AttachPaymentProofOutput(proofRepository.create(proof));
  }
}
