package com.guilherme.emobiliaria.person.application.input;

public record CreateJuridicalPersonInput(
    String corporateName,
    String cnpj,
    Long representativeId,
    Long addressId
) {}
