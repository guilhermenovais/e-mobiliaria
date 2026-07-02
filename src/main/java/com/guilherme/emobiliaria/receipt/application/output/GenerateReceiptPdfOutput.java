package com.guilherme.emobiliaria.receipt.application.output;

import java.util.List;

public record GenerateReceiptPdfOutput(byte[] pdfBytes, List<SkippedProofInfo> skippedProofs) {
}
