package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.domain.service.CpfValidationService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

import java.util.Optional;

public class ValidateCpfInteractor {

  private final CpfValidationService cpfService;

  @Inject
  public ValidateCpfInteractor(CpfValidationService cpfService) {
    this.cpfService = cpfService;
  }

  public Optional<ErrorMessage> execute(String cpf) {
    try {
      cpfService.validate(cpf);
      return Optional.empty();
    } catch (BusinessException e) {
      return Optional.of(e.getErrorMessage());
    }
  }
}
