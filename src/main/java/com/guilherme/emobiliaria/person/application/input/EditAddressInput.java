package com.guilherme.emobiliaria.person.application.input;

import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;

public record EditAddressInput(
    Long id,
    String cep,
    String address,
    String number,
    String complement,
    String neighborhood,
    String city,
    BrazilianState state
) {}
