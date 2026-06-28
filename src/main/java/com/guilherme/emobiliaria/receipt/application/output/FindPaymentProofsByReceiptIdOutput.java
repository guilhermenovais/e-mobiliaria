package com.guilherme.emobiliaria.receipt.application.output;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;

import java.util.List;

public record FindPaymentProofsByReceiptIdOutput(List<PaymentProof> proofs) {
}
