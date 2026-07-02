package com.guilherme.emobiliaria.receipt.domain.service;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;

import java.util.List;

public interface PaymentProofPdfEmbeddingService {
  ProofEmbeddingResult embed(byte[] receiptPdf, List<PaymentProof> proofs);
}
