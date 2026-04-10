package com.guilherme.emobiliaria.person.application.input;

import java.util.List;

public record EditJuridicalPersonInput(
    Long id,
    String corporateName,
    String cnpj,
    List<Long> representativeIds,
    Long addressId
) {}
