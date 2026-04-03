package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.property.application.input.FindPropertyByIdInput;
import com.guilherme.emobiliaria.property.application.output.FindPropertyByIdOutput;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class FindPropertyByIdInteractor {

  private final PropertyRepository propertyRepository;

  @Inject
  public FindPropertyByIdInteractor(PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  public FindPropertyByIdOutput execute(FindPropertyByIdInput input) {
    Property property = propertyRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Property.NOT_FOUND));
    return new FindPropertyByIdOutput(property);
  }
}
