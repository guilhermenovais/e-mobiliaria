package com.guilherme.emobiliaria.person.application.output;

import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public record SearchPhysicalPeopleByNameOutput(PagedResult<PhysicalPerson> result) {}
