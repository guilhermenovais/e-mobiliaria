package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.SearchPhysicalPeopleInput;
import com.guilherme.emobiliaria.person.application.output.SearchPhysicalPeopleOutput;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import jakarta.inject.Inject;

public class SearchPhysicalPeopleInteractor {

  private final PhysicalPersonRepository physicalPersonRepository;

  @Inject
  public SearchPhysicalPeopleInteractor(PhysicalPersonRepository physicalPersonRepository) {
    this.physicalPersonRepository = physicalPersonRepository;
  }

  public SearchPhysicalPeopleOutput execute(SearchPhysicalPeopleInput input) {
    PagedResult<PhysicalPerson> result = physicalPersonRepository.search(input.query(), input.pagination(), input.filter());
    return new SearchPhysicalPeopleOutput(result);
  }
}
