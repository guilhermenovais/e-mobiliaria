package com.guilherme.emobiliaria.property.domain.repository;

import com.guilherme.emobiliaria.property.domain.entity.Property;
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

public class FakePropertyRepository extends FakeImplementation implements PropertyRepository {
  private final Map<Long, Property> store = new HashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public Property create(Property property) {
    maybeFail();
    property.setId(idSequence.getAndIncrement());
    store.put(property.getId(), property);
    return property;
  }

  @Override
  public Property update(Property property) {
    maybeFail();
    if (!store.containsKey(property.getId())) {
      throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, null);
    }
    store.put(property.getId(), property);
    return property;
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    if (!store.containsKey(id)) {
      throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, null);
    }
    store.remove(id);
  }

  @Override
  public Optional<Property> findById(Long id) {
    maybeFail();
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public PagedResult<Property> findAll(PaginationInput pagination) {
    maybeFail();
    List<Property> all = new ArrayList<>(store.values());
    long total = all.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : all.size();
    List<Property> page = all.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public PagedResult<Property> search(String query, PaginationInput pagination) {
    maybeFail();
    String lower = query.toLowerCase();
    List<Property> filtered = store.values().stream()
        .filter(p -> p.getName().toLowerCase().contains(lower)
            || (p.getType() != null && p.getType().toLowerCase().contains(lower)))
        .toList();
    long total = filtered.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : filtered.size();
    List<Property> page = filtered.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public PagedResult<Property> searchByName(String query, PaginationInput pagination) {
    maybeFail();
    List<Property> filtered =
        store.values().stream().filter(p -> p.getName().toLowerCase().contains(query.toLowerCase()))
            .toList();
    long total = filtered.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : filtered.size();
    List<Property> page = filtered.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }
}
