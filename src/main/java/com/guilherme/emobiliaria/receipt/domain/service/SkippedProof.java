package com.guilherme.emobiliaria.receipt.domain.service;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;

public record SkippedProof(PaymentProof proof, SkipReason reason) {
}
