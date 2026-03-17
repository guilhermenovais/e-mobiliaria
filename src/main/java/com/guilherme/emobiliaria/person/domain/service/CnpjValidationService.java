package com.guilherme.emobiliaria.person.domain.service;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class CnpjValidationService {

  public void validate(String cnpj) {
    String digits = cnpj == null ? "" : cnpj.replaceAll("[^0-9]", "");

    if (digits.length() != 14) {
      throw new BusinessException(ErrorMessage.JuridicalPerson.CNPJ_INVALID);
    }

    if (digits.chars().distinct().count() == 1) {
      throw new BusinessException(ErrorMessage.JuridicalPerson.CNPJ_INVALID);
    }

    int[] numbers = digits.chars().map(c -> c - '0').toArray();

    int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    int sum = 0;
    for (int i = 0; i < 12; i++) {
      sum += numbers[i] * weights1[i];
    }
    int remainder = sum % 11;
    int check1 = remainder < 2 ? 0 : 11 - remainder;
    if (check1 != numbers[12]) {
      throw new BusinessException(ErrorMessage.JuridicalPerson.CNPJ_INVALID);
    }

    int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
    sum = 0;
    for (int i = 0; i < 13; i++) {
      sum += numbers[i] * weights2[i];
    }
    remainder = sum % 11;
    int check2 = remainder < 2 ? 0 : 11 - remainder;
    if (check2 != numbers[13]) {
      throw new BusinessException(ErrorMessage.JuridicalPerson.CNPJ_INVALID);
    }
  }
}
