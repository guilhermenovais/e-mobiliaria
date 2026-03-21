package com.guilherme.emobiliaria.person.application.input;

import com.guilherme.emobiliaria.person.domain.entity.CivilState;

public record EditPhysicalPersonInput(
    Long id,
    String name,
    String nationality,
    CivilState civilState,
    String occupation,
    String cpf,
    String idCardNumber,
    Long addressId
) {}
