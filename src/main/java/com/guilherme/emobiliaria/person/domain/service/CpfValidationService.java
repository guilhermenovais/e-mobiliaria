package com.guilherme.emobiliaria.person.domain.service;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class CpfValidationService {

  public void validate(String cpf) {
    String digits = cpf == null ? "" : cpf.replaceAll("[^0-9]", "");

    if (digits.length() != 11) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.CPF_INVALID);
    }

    if (digits.chars().distinct().count() == 1) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.CPF_INVALID);
    }

    int[] numbers = digits.chars().map(c -> c - '0').toArray();

    int sum = 0;
    for (int i = 0; i < 9; i++) {
      sum += numbers[i] * (10 - i);
    }
    int remainder = (sum * 10) % 11;
    if (remainder >= 10) remainder = 0;
    if (remainder != numbers[9]) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.CPF_INVALID);
    }

    sum = 0;
    for (int i = 0; i < 10; i++) {
      sum += numbers[i] * (11 - i);
    }
    remainder = (sum * 10) % 11;
    if (remainder >= 10) remainder = 0;
    if (remainder != numbers[10]) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.CPF_INVALID);
    }
  }
}
