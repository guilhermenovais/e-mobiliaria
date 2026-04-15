package com.guilherme.emobiliaria.person.domain.repository;

import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
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

public class FakeJuridicalPersonRepository extends FakeImplementation
    implements JuridicalPersonRepository {
  private final Map<Long, JuridicalPerson> store = new HashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public JuridicalPerson create(JuridicalPerson person) {
    maybeFail();
    person.setId(idSequence.getAndIncrement());
    store.put(person.getId(), person);
    return person;
  }

  @Override
  public JuridicalPerson update(JuridicalPerson person) {
    maybeFail();
    if (!store.containsKey(person.getId())) {
      throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, null);
    }
    store.put(person.getId(), person);
    return person;
  }

  @Override
  public Optional<JuridicalPerson> findById(Long id) {
    maybeFail();
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public PagedResult<JuridicalPerson> findAll(PaginationInput pagination) {
    maybeFail();
    List<JuridicalPerson> all = new ArrayList<>(store.values());
    long total = all.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : all.size();
    List<JuridicalPerson> page = all.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public PagedResult<JuridicalPerson> search(String query, PaginationInput pagination) {
    maybeFail();
    String lower = query.toLowerCase();
    List<JuridicalPerson> matched = store.values().stream()
        .filter(p -> p.getCorporateName().toLowerCase().contains(lower)
            || (p.getCnpj() != null && p.getCnpj().toLowerCase().contains(lower))
            || p.getRepresentatives().stream()
                .anyMatch(r -> r.getName().toLowerCase().contains(lower)))
        .toList();
    long total = matched.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : matched.size();
    List<JuridicalPerson> page = matched.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    if (!store.containsKey(id)) {
      throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, null);
    }
    store.remove(id);
  }
}
