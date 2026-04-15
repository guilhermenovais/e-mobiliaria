package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.SearchJuridicalPeopleInput;
import com.guilherme.emobiliaria.person.application.output.SearchJuridicalPeopleOutput;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import jakarta.inject.Inject;

public class SearchJuridicalPeopleInteractor {

  private final JuridicalPersonRepository juridicalPersonRepository;

  @Inject
  public SearchJuridicalPeopleInteractor(JuridicalPersonRepository juridicalPersonRepository) {
    this.juridicalPersonRepository = juridicalPersonRepository;
  }

  public SearchJuridicalPeopleOutput execute(SearchJuridicalPeopleInput input) {
    PagedResult<JuridicalPerson> result = juridicalPersonRepository.search(input.query(), input.pagination(), input.filter());
    return new SearchJuridicalPeopleOutput(result);
  }
}
