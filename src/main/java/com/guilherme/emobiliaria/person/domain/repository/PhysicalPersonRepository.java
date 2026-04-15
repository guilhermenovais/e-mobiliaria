package com.guilherme.emobiliaria.person.domain.repository;

import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.Optional;

public interface PhysicalPersonRepository {

  PhysicalPerson create(PhysicalPerson person);

  PhysicalPerson update(PhysicalPerson person);

  Optional<PhysicalPerson> findById(Long id);

  Optional<PhysicalPerson> findByCpf(String cpf);

  PagedResult<PhysicalPerson> findAll(PaginationInput pagination);

  PagedResult<PhysicalPerson> findByName(String name, PaginationInput pagination);

  PagedResult<PhysicalPerson> search(String query, PaginationInput pagination);

  void delete(Long id);
}
