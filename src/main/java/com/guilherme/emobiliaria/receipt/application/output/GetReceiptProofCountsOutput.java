package com.guilherme.emobiliaria.receipt.application.output;

import java.util.Map;

public record GetReceiptProofCountsOutput(Map<Long, Integer> counts) {
}
