package com.guilherme.emobiliaria.receipt.application.input;

import java.time.LocalDate;

public record CreateReceiptInput(
    LocalDate date,
    LocalDate intervalStart,
    LocalDate intervalEnd,
    int discount,
    int fine,
    Long contractId
) {}
