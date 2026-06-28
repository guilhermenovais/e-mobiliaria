package com.guilherme.emobiliaria.receipt.domain.repository;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;

import java.util.List;
import java.util.Map;

public interface PaymentProofRepository {
  PaymentProof create(PaymentProof proof);

  void delete(Long id);

  List<PaymentProof> findAllByReceiptId(Long receiptId);

  void deleteAllByReceiptId(Long receiptId);

  Map<Long, Integer> countByReceiptIds(List<Long> receiptIds);
}
