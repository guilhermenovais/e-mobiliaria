package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.property.application.input.SearchPropertiesInput;
import com.guilherme.emobiliaria.property.application.output.SearchPropertiesOutput;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import jakarta.inject.Inject;

public class SearchPropertiesInteractor {

  private final PropertyRepository propertyRepository;

  @Inject
  public SearchPropertiesInteractor(PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  public SearchPropertiesOutput execute(SearchPropertiesInput input) {
    PagedResult<Property> result = propertyRepository.search(input.query(), input.pagination());
    return new SearchPropertiesOutput(result);
  }
}
