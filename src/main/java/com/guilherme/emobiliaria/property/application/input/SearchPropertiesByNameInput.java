package com.guilherme.emobiliaria.property.application.input;

import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

public record SearchPropertiesByNameInput(String query, PaginationInput pagination) {}
