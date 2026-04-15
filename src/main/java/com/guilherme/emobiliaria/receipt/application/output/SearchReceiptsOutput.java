package com.guilherme.emobiliaria.receipt.application.output;

import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public record SearchReceiptsOutput(PagedResult<Receipt> result) {}
