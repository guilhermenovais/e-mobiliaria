package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.FindPaymentAccountByIdInput;
import com.guilherme.emobiliaria.contract.application.output.FindPaymentAccountByIdOutput;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.domain.repository.PaymentAccountRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class FindPaymentAccountByIdInteractor {

  private final PaymentAccountRepository paymentAccountRepository;

  public FindPaymentAccountByIdInteractor(PaymentAccountRepository paymentAccountRepository) {
    this.paymentAccountRepository = paymentAccountRepository;
  }

  public FindPaymentAccountByIdOutput execute(FindPaymentAccountByIdInput input) {
    PaymentAccount account = paymentAccountRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PaymentAccount.NOT_FOUND));
    return new FindPaymentAccountByIdOutput(account);
  }
}
