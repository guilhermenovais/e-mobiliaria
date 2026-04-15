package com.guilherme.emobiliaria.person.application.input;

import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

public record SearchJuridicalPeopleInput(String query, PaginationInput pagination) {}
