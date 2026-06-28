package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.receipt.application.input.FindPaymentProofsByReceiptIdInput;
import com.guilherme.emobiliaria.receipt.application.output.FindPaymentProofsByReceiptIdOutput;
import com.guilherme.emobiliaria.receipt.domain.repository.PaymentProofRepository;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class FindPaymentProofsByReceiptIdInteractor {

  private final ReceiptRepository receiptRepository;
  private final PaymentProofRepository proofRepository;

  @Inject
  public FindPaymentProofsByReceiptIdInteractor(ReceiptRepository receiptRepository,
      PaymentProofRepository proofRepository) {
    this.receiptRepository = receiptRepository;
    this.proofRepository = proofRepository;
  }

  public FindPaymentProofsByReceiptIdOutput execute(FindPaymentProofsByReceiptIdInput input) {
    receiptRepository.findById(input.receiptId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    return new FindPaymentProofsByReceiptIdOutput(
        proofRepository.findAllByReceiptId(input.receiptId()));
  }
}
