package com.guilherme.emobiliaria.person.domain.service;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class CpfValidationServiceTest {

  private final CpfValidationService service = new CpfValidationService();

  @Nested
  class Validate {

    @Test
    @DisplayName("When CPF is null, should throw BusinessException")
    void shouldThrowWhenCpfIsNull() {
      assertThrows(BusinessException.class, () -> service.validate(null));
    }

    @ParameterizedTest(name = "CPF \"{0}\" has invalid length or format")
    @ValueSource(strings = {"1234567890", "123456789012", "0000000000", "abcdefghijk"})
    @DisplayName("When CPF has invalid length or non-digit characters, should throw BusinessException")
    void shouldThrowWhenCpfHasInvalidLength(String cpf) {
      assertThrows(BusinessException.class, () -> service.validate(cpf));
    }

    @ParameterizedTest(name = "CPF \"{0}\" has all same digits")
    @ValueSource(strings = {"00000000000", "11111111111", "22222222222", "33333333333",
        "44444444444", "55555555555", "66666666666", "77777777777", "88888888888", "99999999999"})
    @DisplayName("When CPF has all same digits, should throw BusinessException")
    void shouldThrowWhenCpfHasAllSameDigits(String cpf) {
      assertThrows(BusinessException.class, () -> service.validate(cpf));
    }

    @ParameterizedTest(name = "CPF \"{0}\" has invalid check digits")
    @ValueSource(strings = {"52998224726", "11144477736", "52998224700"})
    @DisplayName("When CPF has invalid check digits, should throw BusinessException")
    void shouldThrowWhenCpfHasInvalidCheckDigits(String cpf) {
      assertThrows(BusinessException.class, () -> service.validate(cpf));
    }

    @ParameterizedTest(name = "CPF \"{0}\" is valid")
    @ValueSource(strings = {"52998224725", "11144477735"})
    @DisplayName("When CPF is valid without formatting, should not throw")
    void shouldNotThrowWhenCpfIsValid(String cpf) {
      assertDoesNotThrow(() -> service.validate(cpf));
    }

    @ParameterizedTest(name = "Formatted CPF \"{0}\" is valid")
    @ValueSource(strings = {"529.982.247-25", "111.444.777-35"})
    @DisplayName("When CPF is valid with standard formatting, should not throw")
    void shouldNotThrowWhenFormattedCpfIsValid(String cpf) {
      assertDoesNotThrow(() -> service.validate(cpf));
    }
  }
}
