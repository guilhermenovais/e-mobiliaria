package com.guilherme.emobiliaria.contract.domain.repository;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakePaymentAccountRepository extends FakeImplementation
    implements PaymentAccountRepository {
  private final Map<Long, PaymentAccount> store = new HashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public PaymentAccount create(PaymentAccount paymentAccount) {
    maybeFail();
    paymentAccount.setId(idSequence.getAndIncrement());
    store.put(paymentAccount.getId(), paymentAccount);
    return paymentAccount;
  }

  @Override
  public PaymentAccount update(PaymentAccount paymentAccount) {
    maybeFail();
    if (!store.containsKey(paymentAccount.getId())) {
      throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, null);
    }
    store.put(paymentAccount.getId(), paymentAccount);
    return paymentAccount;
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    if (!store.containsKey(id)) {
      throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, null);
    }
    store.remove(id);
  }

  @Override
  public Optional<PaymentAccount> findById(Long id) {
    maybeFail();
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public PagedResult<PaymentAccount> findAll(PaginationInput pagination) {
    maybeFail();
    List<PaymentAccount> all = new ArrayList<>(store.values());
    long total = all.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : all.size();
    List<PaymentAccount> page = all.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }
}
