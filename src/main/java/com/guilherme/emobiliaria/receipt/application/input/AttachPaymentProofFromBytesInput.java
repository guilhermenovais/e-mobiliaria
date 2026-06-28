package com.guilherme.emobiliaria.receipt.application.input;

public record AttachPaymentProofFromBytesInput(Long receiptId, byte[] imageBytes,
                                               String originalFileName) {
}
