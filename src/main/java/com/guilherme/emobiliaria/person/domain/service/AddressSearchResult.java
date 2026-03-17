package com.guilherme.emobiliaria.person.domain.service;

import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;

public record AddressSearchResult(BrazilianState state, String city, String neighborhood,
    String address) {}
