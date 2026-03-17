package com.guilherme.emobiliaria.person.domain.repository;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import java.util.Optional;

public interface AddressRepository {

  Address save(Address address);

  Optional<Address> findById(Long id);

  PagedResult<Address> findAll(PaginationInput pagination);

  void delete(Long id);
}
