package com.guilherme.emobiliaria.contract.domain.repository;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.ContractFilter;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
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

public class FakeContractRepository extends FakeImplementation implements ContractRepository {
  private final Map<Long, Contract> store = new HashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public Contract create(Contract contract) {
    maybeFail();
    contract.setId(idSequence.getAndIncrement());
    store.put(contract.getId(), contract);
    return contract;
  }

  @Override
  public Contract update(Contract contract) {
    maybeFail();
    if (!store.containsKey(contract.getId())) {
      throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, null);
    }
    store.put(contract.getId(), contract);
    return contract;
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    if (!store.containsKey(id)) {
      throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, null);
    }
    store.remove(id);
  }

  @Override
  public Optional<Contract> findById(Long id) {
    maybeFail();
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public List<Contract> findAll() {
    maybeFail();
    return new ArrayList<>(store.values());
  }

  @Override
  public PagedResult<Contract> findAll(PaginationInput pagination, ContractFilter filter) {
    maybeFail();
    List<Contract> all = new ArrayList<>(store.values());
    long total = all.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : all.size();
    List<Contract> page = all.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public PagedResult<Contract> search(String query, PaginationInput pagination, ContractFilter filter) {
    maybeFail();
    String lower = query.toLowerCase();
    List<Contract> filtered = store.values().stream()
        .filter(c -> {
          String propName = c.getProperty() != null && c.getProperty().getName() != null
              ? c.getProperty().getName().toLowerCase() : "";
          boolean matchesProperty = propName.contains(lower);
          boolean matchesTenant = c.getTenants() != null && c.getTenants().stream().anyMatch(t -> {
            if (t instanceof PhysicalPerson pp) return pp.getName().toLowerCase().contains(lower);
            if (t instanceof JuridicalPerson jp) return jp.getCorporateName().toLowerCase().contains(lower);
            return false;
          });
          return matchesProperty || matchesTenant;
        })
        .toList();
    long total = filtered.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : filtered.size();
    List<Contract> page = filtered.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public PagedResult<Contract> findAllByPropertyId(Long propertyId, PaginationInput pagination) {
    maybeFail();
    List<Contract> filtered = store.values().stream()
        .filter(c -> c.getProperty().getId() != null && c.getProperty().getId().equals(propertyId))
        .toList();
    long total = filtered.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : filtered.size();
    List<Contract> page = filtered.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }
}
