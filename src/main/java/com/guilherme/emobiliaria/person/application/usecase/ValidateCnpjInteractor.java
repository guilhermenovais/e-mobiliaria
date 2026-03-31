package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.domain.service.CnpjValidationService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

import java.util.Optional;

public class ValidateCnpjInteractor {

  private final CnpjValidationService cnpjService;

  @Inject
  public ValidateCnpjInteractor(CnpjValidationService cnpjService) {
    this.cnpjService = cnpjService;
  }

  public Optional<ErrorMessage> execute(String cnpj) {
    try {
      cnpjService.validate(cnpj);
      return Optional.empty();
    } catch (BusinessException e) {
      return Optional.of(e.getErrorMessage());
    }
  }
}
