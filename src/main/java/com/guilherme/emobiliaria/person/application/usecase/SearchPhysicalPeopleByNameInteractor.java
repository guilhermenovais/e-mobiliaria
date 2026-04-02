package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.SearchPhysicalPeopleByNameInput;
import com.guilherme.emobiliaria.person.application.output.SearchPhysicalPeopleByNameOutput;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import jakarta.inject.Inject;

public class SearchPhysicalPeopleByNameInteractor {

  private final PhysicalPersonRepository physicalPersonRepository;

  @Inject
  public SearchPhysicalPeopleByNameInteractor(PhysicalPersonRepository physicalPersonRepository) {
    this.physicalPersonRepository = physicalPersonRepository;
  }

  public SearchPhysicalPeopleByNameOutput execute(SearchPhysicalPeopleByNameInput input) {
    PagedResult<PhysicalPerson> result = physicalPersonRepository.findByName(input.name(), input.pagination());
    return new SearchPhysicalPeopleByNameOutput(result);
  }
}
