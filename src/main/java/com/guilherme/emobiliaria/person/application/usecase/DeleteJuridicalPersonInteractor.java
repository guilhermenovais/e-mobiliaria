package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.DeleteJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.DeleteJuridicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class DeleteJuridicalPersonInteractor {

  private final JuridicalPersonRepository juridicalPersonRepository;

  @Inject
  public DeleteJuridicalPersonInteractor(JuridicalPersonRepository juridicalPersonRepository) {
    this.juridicalPersonRepository = juridicalPersonRepository;
  }

  public DeleteJuridicalPersonOutput execute(DeleteJuridicalPersonInput input) {
    juridicalPersonRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.JuridicalPerson.NOT_FOUND));
    juridicalPersonRepository.delete(input.id());
    return new DeleteJuridicalPersonOutput();
  }
}
