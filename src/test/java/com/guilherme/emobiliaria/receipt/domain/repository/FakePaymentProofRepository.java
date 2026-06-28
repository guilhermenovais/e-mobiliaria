package com.guilherme.emobiliaria.receipt.domain.repository;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class FakePaymentProofRepository extends FakeImplementation
    implements PaymentProofRepository {

  private final Map<Long, PaymentProof> store = new HashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public PaymentProof create(PaymentProof proof) {
    maybeFail();
    proof.setId(idSequence.getAndIncrement());
    store.put(proof.getId(), proof);
    return proof;
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    if (!store.containsKey(id)) {
      throw new PersistenceException(ErrorMessage.PaymentProof.NOT_FOUND, null);
    }
    store.remove(id);
  }

  @Override
  public List<PaymentProof> findAllByReceiptId(Long receiptId) {
    maybeFail();
    return store.values().stream()
        .filter(p -> p.getReceiptId() != null && p.getReceiptId().equals(receiptId))
        .collect(Collectors.toList());
  }

  @Override
  public void deleteAllByReceiptId(Long receiptId) {
    maybeFail();
    List<Long> toRemove = store.values().stream()
        .filter(p -> p.getReceiptId() != null && p.getReceiptId().equals(receiptId))
        .map(PaymentProof::getId).collect(Collectors.toList());
    toRemove.forEach(store::remove);
  }

  @Override
  public Map<Long, Integer> countByReceiptIds(List<Long> receiptIds) {
    maybeFail();
    Map<Long, Integer> result = new HashMap<>();
    for (Long id : receiptIds) {
      result.put(id, 0);
    }
    for (PaymentProof proof : store.values()) {
      Long rid = proof.getReceiptId();
      if (rid != null && result.containsKey(rid)) {
        result.put(rid, result.get(rid) + 1);
      }
    }
    return result;
  }

  public List<PaymentProof> getAll() {
    return new ArrayList<>(store.values());
  }
}
