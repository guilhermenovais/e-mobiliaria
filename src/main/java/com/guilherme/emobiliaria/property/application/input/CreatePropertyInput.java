package com.guilherme.emobiliaria.property.application.input;

public record CreatePropertyInput(
    String name,
    String type,
    String cemig,
    String copasa,
    String iptu,
    Long addressId
) {}
