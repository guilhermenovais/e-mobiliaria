package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.property.application.input.SearchPropertiesByNameInput;
import com.guilherme.emobiliaria.property.application.output.SearchPropertiesByNameOutput;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public class SearchPropertiesByNameInteractor {

  private final PropertyRepository propertyRepository;

  public SearchPropertiesByNameInteractor(PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  public SearchPropertiesByNameOutput execute(SearchPropertiesByNameInput input) {
    PagedResult<Property> result = propertyRepository.searchByName(input.query(), input.pagination());
    return new SearchPropertiesByNameOutput(result);
  }
}
