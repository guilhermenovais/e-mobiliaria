package com.guilherme.emobiliaria.person.domain.repository;

import com.guilherme.emobiliaria.person.domain.entity.PersonFilter;
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

public class FakePhysicalPersonRepository extends FakeImplementation
    implements PhysicalPersonRepository {
  private final Map<Long, PhysicalPerson> store = new HashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public PhysicalPerson create(PhysicalPerson person) {
    maybeFail();
    person.setId(idSequence.getAndIncrement());
    store.put(person.getId(), person);
    return person;
  }

  @Override
  public PhysicalPerson update(PhysicalPerson person) {
    maybeFail();
    if (!store.containsKey(person.getId())) {
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, null);
    }
    store.put(person.getId(), person);
    return person;
  }

  @Override
  public Optional<PhysicalPerson> findById(Long id) {
    maybeFail();
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public Optional<PhysicalPerson> findByCpf(String cpf) {
    maybeFail();
    String normalizedCpf = cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    if (normalizedCpf == null || normalizedCpf.isBlank()) {
      return Optional.empty();
    }
    return store.values().stream()
        .filter(p -> normalizedCpf.equals(p.getCpf()))
        .findFirst();
  }

  @Override
  public PagedResult<PhysicalPerson> findAll(PaginationInput pagination, PersonFilter filter) {
    maybeFail();
    List<PhysicalPerson> all = new ArrayList<>(store.values());
    long total = all.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : all.size();
    List<PhysicalPerson> page = all.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public PagedResult<PhysicalPerson> findByName(String name, PaginationInput pagination) {
    maybeFail();
    List<PhysicalPerson> matched = store.values().stream()
        .filter(p -> p.getName().toLowerCase().contains(name.toLowerCase()))
        .toList();
    long total = matched.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : matched.size();
    List<PhysicalPerson> page = matched.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public PagedResult<PhysicalPerson> search(String query, PaginationInput pagination, PersonFilter filter) {
    maybeFail();
    String lower = query.toLowerCase();
    List<PhysicalPerson> matched = store.values().stream()
        .filter(p -> p.getName().toLowerCase().contains(lower)
            || (p.getCpf() != null && p.getCpf().toLowerCase().contains(lower))
            || (p.getIdCardNumber() != null && p.getIdCardNumber().toLowerCase().contains(lower)))
        .toList();
    long total = matched.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : matched.size();
    List<PhysicalPerson> page = matched.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    if (!store.containsKey(id)) {
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, null);
    }
    store.remove(id);
  }
}
