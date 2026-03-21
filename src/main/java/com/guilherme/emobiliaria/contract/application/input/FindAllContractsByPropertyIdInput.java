package com.guilherme.emobiliaria.contract.application.input;

import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

public record FindAllContractsByPropertyIdInput(Long propertyId, PaginationInput pagination) {}
