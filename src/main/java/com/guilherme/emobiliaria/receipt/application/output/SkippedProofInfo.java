package com.guilherme.emobiliaria.receipt.application.output;

import com.guilherme.emobiliaria.receipt.domain.service.SkipReason;

public record SkippedProofInfo(String displayName, SkipReason reason) {
}
