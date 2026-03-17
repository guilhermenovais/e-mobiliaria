package com.guilherme.emobiliaria.person.domain.repository;

import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
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
  public PhysicalPerson save(PhysicalPerson person) {
    maybeFail();
    if (person.getId() == null) {
      person.setId(idSequence.getAndIncrement());
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
  public PagedResult<PhysicalPerson> findAll(PaginationInput pagination) {
    maybeFail();
    List<PhysicalPerson> all = new ArrayList<>(store.values());
    long total = all.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : all.size();
    List<PhysicalPerson> page = all.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    store.remove(id);
  }
}
