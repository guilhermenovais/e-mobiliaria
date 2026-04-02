package com.guilherme.emobiliaria.person.application.input;

import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

public record SearchPhysicalPeopleByNameInput(String name, PaginationInput pagination) {}
