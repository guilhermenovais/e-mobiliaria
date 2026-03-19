package com.guilherme.emobiliaria.receipt.domain.repository;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.Optional;

public interface ReceiptRepository {
  Receipt create(Receipt receipt);

  Receipt update(Receipt receipt);

  void delete(Long id);

  Optional<Receipt> findById(Long id);

  PagedResult<Receipt> findAllByContract(Contract contract, PaginationInput pagination);
}
