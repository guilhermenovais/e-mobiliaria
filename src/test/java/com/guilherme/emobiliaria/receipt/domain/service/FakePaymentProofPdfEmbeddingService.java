package com.guilherme.emobiliaria.receipt.domain.service;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

import java.util.List;

public class FakePaymentProofPdfEmbeddingService extends FakeImplementation
    implements PaymentProofPdfEmbeddingService {

  private List<SkippedProof> nextSkippedProofs = List.of();

  @Override
  public ProofEmbeddingResult embed(byte[] receiptPdf, List<PaymentProof> proofs) {
    maybeFail();
    return new ProofEmbeddingResult(receiptPdf, nextSkippedProofs);
  }

  public void configureSkippedProofs(List<SkippedProof> skippedProofs) {
    this.nextSkippedProofs = skippedProofs;
  }
}
