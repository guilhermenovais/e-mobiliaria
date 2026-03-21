package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.DeletePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.output.DeletePaymentAccountOutput;
import com.guilherme.emobiliaria.contract.domain.repository.PaymentAccountRepository;

public class DeletePaymentAccountInteractor {

  private final PaymentAccountRepository paymentAccountRepository;

  public DeletePaymentAccountInteractor(PaymentAccountRepository paymentAccountRepository) {
    this.paymentAccountRepository = paymentAccountRepository;
  }

  public DeletePaymentAccountOutput execute(DeletePaymentAccountInput input) {
    paymentAccountRepository.delete(input.id());
    return new DeletePaymentAccountOutput();
  }
}
