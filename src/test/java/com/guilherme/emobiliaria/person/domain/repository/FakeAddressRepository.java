package com.guilherme.emobiliaria.person.domain.repository;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeAddressRepository extends FakeImplementation implements AddressRepository {
  private final Map<Long, Address> store = new HashMap<>();
  private final AtomicLong idSequence = new AtomicLong(1);

  @Override
  public Address create(Address address) {
    maybeFail();
    address.setId(idSequence.getAndIncrement());
    store.put(address.getId(), address);
    return address;
  }

  @Override
  public Address update(Address address) {
    maybeFail();
    store.put(address.getId(), address);
    return address;
  }

  @Override
  public Optional<Address> findById(Long id) {
    maybeFail();
    return Optional.ofNullable(store.get(id));
  }

  @Override
  public PagedResult<Address> findAll(PaginationInput pagination) {
    maybeFail();
    List<Address> all = new ArrayList<>(store.values());
    long total = all.size();
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    int limit = pagination.limit() != null ? pagination.limit() : all.size();
    List<Address> page = all.stream().skip(offset).limit(limit).toList();
    return new PagedResult<>(page, total);
  }

  @Override
  public void delete(Long id) {
    maybeFail();
    store.remove(id);
  }
}
