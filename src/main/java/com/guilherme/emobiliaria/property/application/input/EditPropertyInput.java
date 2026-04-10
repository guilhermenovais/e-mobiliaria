package com.guilherme.emobiliaria.property.application.input;

public record EditPropertyInput(
    Long id,
    String name,
    String type,
    String cemig,
    String copasa,
    String iptu,
    Long addressId
) {}
