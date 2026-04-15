package com.guilherme.emobiliaria.person.application.input;

import com.guilherme.emobiliaria.person.domain.entity.PersonFilter;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

public record FindAllPhysicalPeopleInput(PaginationInput pagination, PersonFilter filter) {}
