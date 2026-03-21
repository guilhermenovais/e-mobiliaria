package com.guilherme.emobiliaria.property.application.output;

import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public record SearchPropertiesByNameOutput(PagedResult<Property> result) {}
