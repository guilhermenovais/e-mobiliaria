package com.guilherme.emobiliaria.contract.domain.repository;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.Optional;

public interface PaymentAccountRepository {
  PaymentAccount create(PaymentAccount paymentAccount);

  PaymentAccount update(PaymentAccount paymentAccount);

  void delete(Long id);

  Optional<PaymentAccount> findById(Long id);

  PagedResult<PaymentAccount> findAll(PaginationInput pagination);
}
