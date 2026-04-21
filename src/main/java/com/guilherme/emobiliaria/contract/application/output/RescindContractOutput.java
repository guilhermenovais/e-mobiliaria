package com.guilherme.emobiliaria.contract.application.output;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;

public record RescindContractOutput(Contract contract, byte[] pdfBytes) {
}
