package com.guilherme.emobiliaria.person.application.input;

public record EditJuridicalPersonInput(
    Long id,
    String corporateName,
    String cnpj,
    Long representativeId,
    Long addressId
) {}
