package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.FindJuridicalPersonByIdInput;
import com.guilherme.emobiliaria.person.application.output.FindJuridicalPersonByIdOutput;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class FindJuridicalPersonByIdInteractor {

  private final JuridicalPersonRepository juridicalPersonRepository;

  @Inject
  public FindJuridicalPersonByIdInteractor(JuridicalPersonRepository juridicalPersonRepository) {
    this.juridicalPersonRepository = juridicalPersonRepository;
  }

  public FindJuridicalPersonByIdOutput execute(FindJuridicalPersonByIdInput input) {
    JuridicalPerson person = juridicalPersonRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.JuridicalPerson.NOT_FOUND));
    return new FindJuridicalPersonByIdOutput(person);
  }
}
