package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.receipt.application.input.RemovePaymentProofInput;
import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.repository.PaymentProofRepository;
import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofStorageService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

import java.util.List;

public class RemovePaymentProofInteractor {

  private final PaymentProofRepository proofRepository;
  private final PaymentProofStorageService storageService;

  @Inject
  public RemovePaymentProofInteractor(PaymentProofRepository proofRepository,
      PaymentProofStorageService storageService) {
    this.proofRepository = proofRepository;
    this.storageService = storageService;
  }

  public void execute(RemovePaymentProofInput input) {
    List<PaymentProof> proofs = proofRepository.findAllByReceiptId(input.receiptId());
    PaymentProof proof = proofs.stream().filter(p -> p.getId().equals(input.proofId())).findFirst()
        .orElseThrow(() -> new BusinessException(ErrorMessage.PaymentProof.NOT_FOUND));
    storageService.delete(proof.getStoredFileName());
    proofRepository.delete(input.proofId());
  }
}
