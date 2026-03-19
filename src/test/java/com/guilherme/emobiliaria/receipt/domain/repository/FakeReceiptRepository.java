package com.guilherme.emobiliaria.receipt.domain.repository;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeReceiptRepository extends FakeImplementation implements ReceiptRepository {
  private final Map<Long, Receipt> store = new HashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public Receipt create(Receipt receipt) {
    maybeFail();
    receipt.setId(idSequence.getAndIncrement());
    store.put(receipt.getId(), receipt);
    return receipt;
  }

  @Override
  public Receipt update(Receipt receipt) {
    maybeFail();
    store.put(receipt.getId(), receipt);
    return receipt;
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    store.remove(id);
  }

  @Override
  public Optional<Receipt> findById(Long id) {
    maybeFail();
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public PagedResult<Receipt> findAllByContract(Contract contract, PaginationInput pagination) {
    maybeFail();
    List<Receipt> filtered = store.values().stream().filter(
            r -> r.getContract().getId() != null && r.getContract().getId().equals(contract.getId()))
        .toList();
    long total = filtered.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : filtered.size();
    List<Receipt> page = new ArrayList<>(filtered).stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }
}
