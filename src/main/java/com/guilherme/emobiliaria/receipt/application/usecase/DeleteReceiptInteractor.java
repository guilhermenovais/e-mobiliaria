package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.receipt.application.input.DeleteReceiptInput;
import com.guilherme.emobiliaria.receipt.application.output.DeleteReceiptOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.repository.PaymentProofRepository;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofStorageService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

import java.util.List;

public class DeleteReceiptInteractor {

  private final ReceiptRepository receiptRepository;
  private final PaymentProofRepository proofRepository;
  private final PaymentProofStorageService storageService;

  @Inject
  public DeleteReceiptInteractor(ReceiptRepository receiptRepository,
      PaymentProofRepository proofRepository, PaymentProofStorageService storageService) {
    this.receiptRepository = receiptRepository;
    this.proofRepository = proofRepository;
    this.storageService = storageService;
  }

  public DeleteReceiptOutput execute(DeleteReceiptInput input) {
    receiptRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    List<PaymentProof> proofs = proofRepository.findAllByReceiptId(input.id());
    for (PaymentProof proof : proofs) {
      storageService.delete(proof.getStoredFileName());
    }
    proofRepository.deleteAllByReceiptId(input.id());
    receiptRepository.delete(input.id());
    return new DeleteReceiptOutput();
  }
}
