package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.FindAllJuridicalPeopleInput;
import com.guilherme.emobiliaria.person.application.output.FindAllJuridicalPeopleOutput;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import jakarta.inject.Inject;

public class FindAllJuridicalPeopleInteractor {

  private final JuridicalPersonRepository juridicalPersonRepository;

  @Inject
  public FindAllJuridicalPeopleInteractor(JuridicalPersonRepository juridicalPersonRepository) {
    this.juridicalPersonRepository = juridicalPersonRepository;
  }

  public FindAllJuridicalPeopleOutput execute(FindAllJuridicalPeopleInput input) {
    PagedResult<JuridicalPerson> result = juridicalPersonRepository.findAll(input.pagination(), input.filter());
    return new FindAllJuridicalPeopleOutput(result);
  }
}
