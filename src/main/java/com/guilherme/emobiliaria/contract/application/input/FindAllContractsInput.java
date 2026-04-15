package com.guilherme.emobiliaria.contract.application.input;

import com.guilherme.emobiliaria.contract.domain.entity.ContractFilter;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

public record FindAllContractsInput(PaginationInput pagination, ContractFilter filter) {}
