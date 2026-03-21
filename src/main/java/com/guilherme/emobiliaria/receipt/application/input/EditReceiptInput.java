package com.guilherme.emobiliaria.receipt.application.input;

import java.time.LocalDate;

public record EditReceiptInput(
    Long id,
    LocalDate date,
    LocalDate intervalStart,
    LocalDate intervalEnd,
    int discount,
    int fine,
    Long contractId
) {}
