package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.property.application.input.DeletePropertyInput;
import com.guilherme.emobiliaria.property.application.output.DeletePropertyOutput;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class DeletePropertyInteractor {

  private final PropertyRepository propertyRepository;

  public DeletePropertyInteractor(PropertyRepository propertyRepository) {
    this.propertyRepository = propertyRepository;
  }

  public DeletePropertyOutput execute(DeletePropertyInput input) {
    propertyRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Property.NOT_FOUND));
    propertyRepository.delete(input.id());
    return new DeletePropertyOutput();
  }
}
