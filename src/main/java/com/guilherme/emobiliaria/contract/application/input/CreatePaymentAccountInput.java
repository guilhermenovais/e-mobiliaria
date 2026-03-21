package com.guilherme.emobiliaria.contract.application.input;

public record CreatePaymentAccountInput(
    String bank,
    String bankBranch,
    String accountNumber,
    String pixKey
) {}
