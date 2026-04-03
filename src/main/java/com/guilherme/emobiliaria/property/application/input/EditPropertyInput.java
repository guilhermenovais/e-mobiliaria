package com.guilherme.emobiliaria.property.application.input;

import com.guilherme.emobiliaria.property.domain.entity.Purpose;

public record EditPropertyInput(
    Long id,
    String name,
    String type,
    Purpose purpose,
    String cemig,
    String copasa,
    String iptu,
    Long addressId
) {}
