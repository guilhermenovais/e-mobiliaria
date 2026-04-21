package com.guilherme.emobiliaria.contract.application.input;

import java.time.LocalDate;

public record RescindContractInput(Long contractId, LocalDate rescissionDate) {
}
