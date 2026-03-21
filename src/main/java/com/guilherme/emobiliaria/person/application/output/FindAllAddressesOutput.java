package com.guilherme.emobiliaria.person.application.output;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public record FindAllAddressesOutput(PagedResult<Address> result) {}
