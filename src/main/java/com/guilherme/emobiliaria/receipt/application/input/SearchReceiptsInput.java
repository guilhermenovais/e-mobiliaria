package com.guilherme.emobiliaria.receipt.application.input;

import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

public record SearchReceiptsInput(String query, Long contractId, PaginationInput pagination) {}
