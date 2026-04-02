package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.FindPhysicalPersonByIdInput;
import com.guilherme.emobiliaria.person.application.output.FindPhysicalPersonByIdOutput;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class FindPhysicalPersonByIdInteractor {

  private final PhysicalPersonRepository physicalPersonRepository;

  @Inject
  public FindPhysicalPersonByIdInteractor(PhysicalPersonRepository physicalPersonRepository) {
    this.physicalPersonRepository = physicalPersonRepository;
  }

  public FindPhysicalPersonByIdOutput execute(FindPhysicalPersonByIdInput input) {
    PhysicalPerson person = physicalPersonRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PhysicalPerson.NOT_FOUND));
    return new FindPhysicalPersonByIdOutput(person);
  }
}
