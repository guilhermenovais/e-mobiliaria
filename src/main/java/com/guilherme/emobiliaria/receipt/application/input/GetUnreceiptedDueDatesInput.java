package com.guilherme.emobiliaria.receipt.application.input;

import java.time.LocalDate;

public record GetUnreceiptedDueDatesInput(Long contractId, LocalDate today, Long excludeReceiptId) {
}
