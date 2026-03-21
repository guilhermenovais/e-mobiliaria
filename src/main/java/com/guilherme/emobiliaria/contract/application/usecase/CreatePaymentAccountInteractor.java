package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.output.CreatePaymentAccountOutput;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.domain.repository.PaymentAccountRepository;

public class CreatePaymentAccountInteractor {

  private final PaymentAccountRepository paymentAccountRepository;

  public CreatePaymentAccountInteractor(PaymentAccountRepository paymentAccountRepository) {
    this.paymentAccountRepository = paymentAccountRepository;
  }

  public CreatePaymentAccountOutput execute(CreatePaymentAccountInput input) {
    PaymentAccount account = PaymentAccount.create(
        input.bank(),
        input.bankBranch(),
        input.accountNumber(),
        input.pixKey()
    );
    PaymentAccount created = paymentAccountRepository.create(account);
    return new CreatePaymentAccountOutput(created);
  }
}
