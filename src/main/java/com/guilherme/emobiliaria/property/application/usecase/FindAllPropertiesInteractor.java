package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.property.application.input.FindAllPropertiesInput;
import com.guilherme.emobiliaria.property.application.output.FindAllPropertiesOutput;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public class FindAllPropertiesInteractor {

  private final PropertyRepository propertyRepository;

  public FindAllPropertiesInteractor(PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  public FindAllPropertiesOutput execute(FindAllPropertiesInput input) {
    PagedResult<Property> result = propertyRepository.findAll(input.pagination());
    return new FindAllPropertiesOutput(result);
  }
}
