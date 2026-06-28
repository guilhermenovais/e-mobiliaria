package com.guilherme.emobiliaria.receipt.application.input;

import java.util.List;

public record GetReceiptProofCountsInput(List<Long> receiptIds) {
}
