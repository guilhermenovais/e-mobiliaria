package com.guilherme.emobiliaria.property.domain.repository;

import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;

import java.util.Optional;

public interface PropertyRepository {

  Property create(Property property);

  Property update(Property property);

  void delete(Long id);

  Optional<Property> findById(Long id);

  PagedResult<Property> findAll(PaginationInput pagination);

  PagedResult<Property> searchByName(String query, PaginationInput pagination);
}
