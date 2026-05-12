package com.guilherme.emobiliaria.receipt.application.input;

import java.time.LocalDate;

public record CreateReceiptInput(LocalDate date, LocalDate paymentDueDate, LocalDate intervalStart,
                                 LocalDate intervalEnd, int discount, int fine, String observation,
                                 Long contractId) {
}
