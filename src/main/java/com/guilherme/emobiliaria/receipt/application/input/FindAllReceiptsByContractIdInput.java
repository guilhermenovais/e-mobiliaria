package com.guilherme.emobiliaria.receipt.application.input;

import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

public record FindAllReceiptsByContractIdInput(Long contractId, PaginationInput pagination) {}
