package com.guilherme.emobiliaria.contract.application.output;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public record FindAllContractsByPropertyIdOutput(PagedResult<Contract> result) {}
