package com.guilherme.emobiliaria.contract.domain.repository;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.ContractFilter;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.Optional;

public interface ContractRepository {
  Contract create(Contract contract);

  Contract update(Contract contract);

  void delete(Long id);

  Optional<Contract> findById(Long id);

  PagedResult<Contract> findAll(PaginationInput pagination, ContractFilter filter);

  PagedResult<Contract> findAllByPropertyId(Long propertyId, PaginationInput pagination);

  PagedResult<Contract> search(String query, PaginationInput pagination, ContractFilter filter);
}
