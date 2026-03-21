package com.guilherme.emobiliaria.contract.application.input;

public record EditPaymentAccountInput(
    Long id,
    String bank,
    String bankBranch,
    String accountNumber,
    String pixKey
) {}
