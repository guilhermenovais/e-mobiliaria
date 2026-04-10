package com.guilherme.emobiliaria.person.application.input;

import java.util.List;

public record CreateJuridicalPersonInput(
    String corporateName,
    String cnpj,
    List<Long> representativeIds,
    Long addressId
) {}
