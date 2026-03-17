package com.guilherme.emobiliaria.person.domain.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CnpjValidationServiceTest {

  private final CnpjValidationService service = new CnpjValidationService();

  @Nested
  class Validate {

    @Test
    @DisplayName("When CNPJ is null, should throw BusinessException")
    void shouldThrowWhenCnpjIsNull() {
      assertThrows(BusinessException.class, () -> service.validate(null));
    }

    @ParameterizedTest(name = "CNPJ \"{0}\" has invalid length")
    @ValueSource(strings = {"1234567890123", "123456789012345", "abcdefghijklmn"})
    @DisplayName("When CNPJ has invalid length, should throw BusinessException")
    void shouldThrowWhenCnpjHasInvalidLength(String cnpj) {
      assertThrows(BusinessException.class, () -> service.validate(cnpj));
    }

    @ParameterizedTest(name = "CNPJ \"{0}\" has all same digits")
    @ValueSource(strings = {"00000000000000", "11111111111111", "22222222222222",
        "99999999999999"})
    @DisplayName("When CNPJ has all same digits, should throw BusinessException")
    void shouldThrowWhenCnpjHasAllSameDigits(String cnpj) {
      assertThrows(BusinessException.class, () -> service.validate(cnpj));
    }

    @ParameterizedTest(name = "CNPJ \"{0}\" has invalid check digits")
    @ValueSource(strings = {"11222333000182", "45997418000154"})
    @DisplayName("When CNPJ has invalid check digits, should throw BusinessException")
    void shouldThrowWhenCnpjHasInvalidCheckDigits(String cnpj) {
      assertThrows(BusinessException.class, () -> service.validate(cnpj));
    }

    @ParameterizedTest(name = "CNPJ \"{0}\" is valid")
    @ValueSource(strings = {"11222333000181", "45997418000153"})
    @DisplayName("When CNPJ is valid without formatting, should not throw")
    void shouldNotThrowWhenCnpjIsValid(String cnpj) {
      assertDoesNotThrow(() -> service.validate(cnpj));
    }

    @ParameterizedTest(name = "Formatted CNPJ \"{0}\" is valid")
    @ValueSource(strings = {"11.222.333/0001-81", "45.997.418/0001-53"})
    @DisplayName("When CNPJ is valid with standard formatting, should not throw")
    void shouldNotThrowWhenFormattedCnpjIsValid(String cnpj) {
      assertDoesNotThrow(() -> service.validate(cnpj));
    }
  }
}
