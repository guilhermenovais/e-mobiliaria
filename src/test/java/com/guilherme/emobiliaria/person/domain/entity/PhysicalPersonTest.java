package com.guilherme.emobiliaria.person.domain.entity;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PhysicalPersonTest {

  private static final String VALID_NAME = "João da Silva";
  private static final String VALID_NATIONALITY = "Brasileiro";
  private static final String VALID_OCCUPATION = "Engenheiro";
  private static final String VALID_CPF = "52998224725";
  private static final String VALID_ID_CARD = "MG-12.345.678";
  private static final CivilState VALID_CIVIL_STATE = CivilState.SINGLE;

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private PhysicalPerson validPerson() {
    return PhysicalPerson.create(VALID_NAME, VALID_NATIONALITY, VALID_CIVIL_STATE,
        VALID_OCCUPATION, VALID_CPF, VALID_ID_CARD, validAddress());
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When all fields are valid, should create PhysicalPerson with null id")
    void shouldCreateWhenAllFieldsAreValid() {
      PhysicalPerson person = validPerson();

      assertNull(person.getId());
      assertEquals(VALID_NAME, person.getName());
      assertEquals(VALID_NATIONALITY, person.getNationality());
      assertEquals(VALID_CIVIL_STATE, person.getCivilState());
      assertEquals(VALID_OCCUPATION, person.getOccupation());
      assertEquals(VALID_CPF, person.getCpf());
      assertEquals(VALID_ID_CARD, person.getIdCardNumber());
    }
  }

  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with id, should set id")
    void shouldRestoreWithId() {
      PhysicalPerson person = PhysicalPerson.restore(99L, VALID_NAME, VALID_NATIONALITY,
          VALID_CIVIL_STATE, VALID_OCCUPATION, VALID_CPF, VALID_ID_CARD, validAddress());

      assertEquals(99L, person.getId());
    }
  }

  @Nested
  class SetName {

    @Test
    @DisplayName("When name is null, should throw BusinessException")
    void shouldThrowWhenNameIsNull() {
      PhysicalPerson person = validPerson();
      BusinessException ex = assertThrows(BusinessException.class, () -> person.setName(null));
      assertEquals(ErrorMessage.PhysicalPerson.NAME_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When name is blank, should throw BusinessException")
    void shouldThrowWhenNameIsBlank() {
      PhysicalPerson person = validPerson();
      BusinessException ex = assertThrows(BusinessException.class, () -> person.setName("   "));
      assertEquals(ErrorMessage.PhysicalPerson.NAME_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When name has 100 or more characters, should throw BusinessException")
    void shouldThrowWhenNameExceedsLimit() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setName("A".repeat(100)));
      assertEquals(ErrorMessage.PhysicalPerson.NAME_TOO_LONG, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When name has 99 characters, should set name")
    void shouldSetNameAtMaxLength() {
      PhysicalPerson person = validPerson();
      String name = "A".repeat(99);
      assertDoesNotThrow(() -> person.setName(name));
      assertEquals(name, person.getName());
    }
  }

  @Nested
  class SetNationality {

    @Test
    @DisplayName("When nationality is null, should throw BusinessException")
    void shouldThrowWhenNationalityIsNull() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setNationality(null));
      assertEquals(ErrorMessage.PhysicalPerson.NATIONALITY_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When nationality is blank, should throw BusinessException")
    void shouldThrowWhenNationalityIsBlank() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setNationality("  "));
      assertEquals(ErrorMessage.PhysicalPerson.NATIONALITY_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When nationality has 20 or more characters, should throw BusinessException")
    void shouldThrowWhenNationalityExceedsLimit() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setNationality("A".repeat(20)));
      assertEquals(ErrorMessage.PhysicalPerson.NATIONALITY_TOO_LONG, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When nationality is valid, should set nationality")
    void shouldSetNationalityWhenValid() {
      PhysicalPerson person = validPerson();
      assertDoesNotThrow(() -> person.setNationality("Brasileiro"));
    }
  }

  @Nested
  class SetCivilState {

    @Test
    @DisplayName("When civil state is null, should throw BusinessException")
    void shouldThrowWhenCivilStateIsNull() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setCivilState(null));
      assertEquals(ErrorMessage.PhysicalPerson.CIVIL_STATE_NULL, ex.getErrorMessage());
    }

    @ParameterizedTest(name = "Civil state {0} is valid")
    @ValueSource(strings = {"SINGLE", "MARRIED", "DIVORCED", "WIDOWER"})
    @DisplayName("When civil state is a valid value, should set it")
    void shouldSetCivilStateWhenValid(String stateValue) {
      PhysicalPerson person = validPerson();
      CivilState state = CivilState.valueOf(stateValue);
      assertDoesNotThrow(() -> person.setCivilState(state));
      assertEquals(state, person.getCivilState());
    }
  }

  @Nested
  class SetOccupation {

    @Test
    @DisplayName("When occupation is null, should throw BusinessException")
    void shouldThrowWhenOccupationIsNull() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setOccupation(null));
      assertEquals(ErrorMessage.PhysicalPerson.OCCUPATION_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When occupation is blank, should throw BusinessException")
    void shouldThrowWhenOccupationIsBlank() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setOccupation("  "));
      assertEquals(ErrorMessage.PhysicalPerson.OCCUPATION_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When occupation has 100 or more characters, should throw BusinessException")
    void shouldThrowWhenOccupationExceedsLimit() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setOccupation("A".repeat(100)));
      assertEquals(ErrorMessage.PhysicalPerson.OCCUPATION_TOO_LONG, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When occupation is valid, should set occupation")
    void shouldSetOccupationWhenValid() {
      PhysicalPerson person = validPerson();
      assertDoesNotThrow(() -> person.setOccupation("Advogado"));
    }
  }

  @Nested
  class SetCpf {

    @Test
    @DisplayName("When CPF is null, should throw BusinessException")
    void shouldThrowWhenCpfIsNull() {
      PhysicalPerson person = validPerson();
      assertThrows(BusinessException.class, () -> person.setCpf(null));
    }

    @ParameterizedTest(name = "CPF \"{0}\" is invalid")
    @ValueSource(strings = {"11111111111", "52998224726", "1234567890"})
    @DisplayName("When CPF is invalid, should throw BusinessException")
    void shouldThrowWhenCpfIsInvalid(String cpf) {
      PhysicalPerson person = validPerson();
      assertThrows(BusinessException.class, () -> person.setCpf(cpf));
    }

    @Test
    @DisplayName("When CPF is valid, should set normalized CPF")
    void shouldSetNormalizedCpfWhenValid() {
      PhysicalPerson person = validPerson();
      person.setCpf("529.982.247-25");
      assertEquals("52998224725", person.getCpf());
    }
  }

  @Nested
  class SetIdCardNumber {

    @Test
    @DisplayName("When ID card number is null, should throw BusinessException")
    void shouldThrowWhenIdCardNumberIsNull() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setIdCardNumber(null));
      assertEquals(ErrorMessage.PhysicalPerson.ID_CARD_NUMBER_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When ID card number is blank, should throw BusinessException")
    void shouldThrowWhenIdCardNumberIsBlank() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setIdCardNumber("  "));
      assertEquals(ErrorMessage.PhysicalPerson.ID_CARD_NUMBER_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When ID card number has 20 or more characters, should throw BusinessException")
    void shouldThrowWhenIdCardNumberExceedsLimit() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setIdCardNumber("A".repeat(20)));
      assertEquals(ErrorMessage.PhysicalPerson.ID_CARD_NUMBER_TOO_LONG, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When ID card number is valid, should set it")
    void shouldSetIdCardNumberWhenValid() {
      PhysicalPerson person = validPerson();
      assertDoesNotThrow(() -> person.setIdCardNumber("12345678"));
      assertEquals("12345678", person.getIdCardNumber());
    }
  }

  @Nested
  class SetAddress {

    @Test
    @DisplayName("When address is null, should throw BusinessException")
    void shouldThrowWhenAddressIsNull() {
      PhysicalPerson person = validPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setAddress(null));
      assertEquals(ErrorMessage.PhysicalPerson.ADDRESS_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When address is valid, should set address")
    void shouldSetAddressWhenValid() {
      PhysicalPerson person = validPerson();
      Address newAddress = validAddress();
      assertDoesNotThrow(() -> person.setAddress(newAddress));
      assertEquals(newAddress, person.getAddress());
    }
  }
}
