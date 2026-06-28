package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.receipt.application.input.GetReceiptProofCountsInput;
import com.guilherme.emobiliaria.receipt.application.output.GetReceiptProofCountsOutput;
import com.guilherme.emobiliaria.receipt.domain.repository.PaymentProofRepository;

public class GetReceiptProofCountsInteractor {

  private final PaymentProofRepository proofRepository;

  @Inject
  public GetReceiptProofCountsInteractor(PaymentProofRepository proofRepository) {
    this.proofRepository = proofRepository;
  }

  public GetReceiptProofCountsOutput execute(GetReceiptProofCountsInput input) {
    return new GetReceiptProofCountsOutput(proofRepository.countByReceiptIds(input.receiptIds()));
  }
}
