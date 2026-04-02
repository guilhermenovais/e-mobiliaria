package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.FindAllPhysicalPeopleInput;
import com.guilherme.emobiliaria.person.application.output.FindAllPhysicalPeopleOutput;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import jakarta.inject.Inject;

public class FindAllPhysicalPeopleInteractor {

  private final PhysicalPersonRepository physicalPersonRepository;

  @Inject
  public FindAllPhysicalPeopleInteractor(PhysicalPersonRepository physicalPersonRepository) {
    this.physicalPersonRepository = physicalPersonRepository;
  }

  public FindAllPhysicalPeopleOutput execute(FindAllPhysicalPeopleInput input) {
    PagedResult<PhysicalPerson> result = physicalPersonRepository.findAll(input.pagination());
    return new FindAllPhysicalPeopleOutput(result);
  }
}
