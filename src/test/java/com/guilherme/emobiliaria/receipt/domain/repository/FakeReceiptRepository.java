package com.guilherme.emobiliaria.receipt.domain.repository;

import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeReceiptRepository extends FakeImplementation implements ReceiptRepository {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
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
    if (!store.containsKey(receipt.getId())) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, null);
    }
    store.put(receipt.getId(), receipt);
    return receipt;
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    if (!store.containsKey(id)) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, null);
    }
    store.remove(id);
  }

  @Override
  public Optional<Receipt> findById(Long id) {
    maybeFail();
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public PagedResult<Receipt> search(String query, Long contractId, PaginationInput pagination) {
    maybeFail();
    String lower = query.toLowerCase();
    List<Receipt> filtered = store.values().stream()
        .filter(r -> {
          if (contractId != null && (r.getContract() == null || !contractId.equals(r.getContract().getId()))) {
            return false;
          }
          String start = r.getIntervalStart() != null ? r.getIntervalStart().format(DATE_FMT) : "";
          String end = r.getIntervalEnd() != null ? r.getIntervalEnd().format(DATE_FMT) : "";
          return start.toLowerCase().contains(lower) || end.toLowerCase().contains(lower);
        })
        .toList();
    long total = filtered.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : filtered.size();
    List<Receipt> page = new ArrayList<>(filtered).stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public PagedResult<Receipt> findAllByContractId(Long contractId, PaginationInput pagination) {
    maybeFail();
    List<Receipt> filtered = store.values().stream().filter(
            r -> r.getContract().getId() != null && r.getContract().getId().equals(contractId))
        .toList();
    long total = filtered.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : filtered.size();
    List<Receipt> page = new ArrayList<>(filtered).stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }
}
