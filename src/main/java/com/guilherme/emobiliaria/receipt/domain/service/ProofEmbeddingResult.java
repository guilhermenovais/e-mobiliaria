package com.guilherme.emobiliaria.receipt.domain.service;

import java.util.List;

public record ProofEmbeddingResult(byte[] pdfBytes, List<SkippedProof> skippedProofs) {
}
