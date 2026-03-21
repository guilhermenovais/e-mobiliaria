package com.guilherme.emobiliaria.person.application.output;

import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public record FindAllJuridicalPeopleOutput(PagedResult<JuridicalPerson> result) {}
