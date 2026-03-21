package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.FindAllPaymentAccountsInput;
import com.guilherme.emobiliaria.contract.application.output.FindAllPaymentAccountsOutput;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.domain.repository.PaymentAccountRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public class FindAllPaymentAccountsInteractor {

  private final PaymentAccountRepository paymentAccountRepository;

  public FindAllPaymentAccountsInteractor(PaymentAccountRepository paymentAccountRepository) {
    this.paymentAccountRepository = paymentAccountRepository;
  }

  public FindAllPaymentAccountsOutput execute(FindAllPaymentAccountsInput input) {
    PagedResult<PaymentAccount> result = paymentAccountRepository.findAll(input.pagination());
    return new FindAllPaymentAccountsOutput(result);
  }
}
