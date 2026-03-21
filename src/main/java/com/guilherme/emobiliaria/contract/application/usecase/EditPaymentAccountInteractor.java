package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.EditPaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.output.EditPaymentAccountOutput;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.domain.repository.PaymentAccountRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class EditPaymentAccountInteractor {

  private final PaymentAccountRepository paymentAccountRepository;

  public EditPaymentAccountInteractor(PaymentAccountRepository paymentAccountRepository) {
    this.paymentAccountRepository = paymentAccountRepository;
  }

  public EditPaymentAccountOutput execute(EditPaymentAccountInput input) {
    PaymentAccount account = paymentAccountRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PaymentAccount.NOT_FOUND));
    account.setBank(input.bank());
    account.setBankBranch(input.bankBranch());
    account.setAccountNumber(input.accountNumber());
    account.setPixKey(input.pixKey());
    PaymentAccount updated = paymentAccountRepository.update(account);
    return new EditPaymentAccountOutput(updated);
  }
}
