package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.DeletePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.DeletePhysicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class DeletePhysicalPersonInteractor {

  private final PhysicalPersonRepository physicalPersonRepository;

  @Inject
  public DeletePhysicalPersonInteractor(PhysicalPersonRepository physicalPersonRepository) {
    this.physicalPersonRepository = physicalPersonRepository;
  }

  public DeletePhysicalPersonOutput execute(DeletePhysicalPersonInput input) {
    physicalPersonRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PhysicalPerson.NOT_FOUND));
    physicalPersonRepository.delete(input.id());
    return new DeletePhysicalPersonOutput();
  }
}
