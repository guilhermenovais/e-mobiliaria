package com.guilherme.emobiliaria.contract.application.output;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public record FindAllPaymentAccountsOutput(PagedResult<PaymentAccount> result) {}
