package com.guilherme.emobiliaria.person.domain.repository;

import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import java.util.Optional;

public interface PhysicalPersonRepository {

  PhysicalPerson save(PhysicalPerson person);

  Optional<PhysicalPerson> findById(Long id);

  PagedResult<PhysicalPerson> findAll(PaginationInput pagination);

  void delete(Long id);
}
