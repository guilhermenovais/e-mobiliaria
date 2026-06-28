package com.guilherme.emobiliaria.receipt.application.input;

import java.nio.file.Path;

public record AttachPaymentProofFromFileInput(Long receiptId, Path sourceFile,
                                              String originalFileName) {
}
