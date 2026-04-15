package com.guilherme.emobiliaria.person.domain.repository;

import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PersonFilter;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.Optional;

public interface JuridicalPersonRepository {

  JuridicalPerson create(JuridicalPerson person);

  JuridicalPerson update(JuridicalPerson person);

  Optional<JuridicalPerson> findById(Long id);

  PagedResult<JuridicalPerson> findAll(PaginationInput pagination, PersonFilter filter);

  PagedResult<JuridicalPerson> search(String query, PaginationInput pagination, PersonFilter filter);

  void delete(Long id);
}
