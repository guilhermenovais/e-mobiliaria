package com.guilherme.emobiliaria.person.domain.entity;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class AddressTest {

  private static final String VALID_CEP = "01001000";
  private static final String VALID_STREET = "Praça da Sé";
  private static final String VALID_NUMBER = "1";
  private static final String VALID_NEIGHBORHOOD = "Sé";
  private static final String VALID_CITY = "São Paulo";
  private static final BrazilianState VALID_STATE = BrazilianState.SP;

  private Address validAddress() {
    return Address.create(VALID_CEP, VALID_STREET, VALID_NUMBER, null, VALID_NEIGHBORHOOD,
        VALID_CITY, VALID_STATE);
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When all fields are valid, should create address")
    void shouldCreateWhenAllFieldsAreValid() {
      Address address = validAddress();

      assertEquals(VALID_CEP, address.getCep());
      assertEquals(VALID_STREET, address.getAddress());
      assertEquals(VALID_NUMBER, address.getNumber());
      assertNull(address.getComplement());
      assertEquals(VALID_NEIGHBORHOOD, address.getNeighborhood());
      assertEquals(VALID_CITY, address.getCity());
      assertEquals(VALID_STATE, address.getState());
      assertNull(address.getId());
    }
  }

  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with id, should set id")
    void shouldRestoreWithId() {
      Address address = Address.restore(42L, VALID_CEP, VALID_STREET, VALID_NUMBER, null,
          VALID_NEIGHBORHOOD, VALID_CITY, VALID_STATE);

      assertEquals(42L, address.getId());
    }
  }

  @Nested
  class SetCep {

    @Test
    @DisplayName("When CEP is null, should throw BusinessException")
    void shouldThrowWhenCepIsNull() {
      Address address = validAddress();
      BusinessException ex = assertThrows(BusinessException.class, () -> address.setCep(null));
      assertEquals(ErrorMessage.Address.CEP_REQUIRED, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When CEP is blank, should throw BusinessException")
    void shouldThrowWhenCepIsBlank() {
      Address address = validAddress();
      BusinessException ex = assertThrows(BusinessException.class, () -> address.setCep("   "));
      assertEquals(ErrorMessage.Address.CEP_REQUIRED, ex.getErrorMessage());
    }

    @ParameterizedTest(name = "CEP \"{0}\" is invalid")
    @ValueSource(strings = {"1234567", "123456789", "0100100A", "01001-00X"})
    @DisplayName("When CEP has invalid format, should throw BusinessException")
    void shouldThrowWhenCepIsInvalid(String cep) {
      Address address = validAddress();
      BusinessException ex = assertThrows(BusinessException.class, () -> address.setCep(cep));
      assertEquals(ErrorMessage.Address.CEP_INVALID, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When CEP has mask format, should normalize and store digits only")
    void shouldNormalizeCepWithMask() {
      Address address = validAddress();
      address.setCep("01001-000");
      assertEquals("01001000", address.getCep());
    }

    @Test
    @DisplayName("When CEP is 8 digits, should set CEP")
    void shouldSetCepWhenValid() {
      Address address = validAddress();
      address.setCep("01001000");
      assertEquals("01001000", address.getCep());
    }
  }

  @Nested
  class SetAddress {

    @Test
    @DisplayName("When street is null, should throw BusinessException")
    void shouldThrowWhenStreetIsNull() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setAddress(null));
      assertEquals(ErrorMessage.Address.STREET_REQUIRED, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When street is blank, should throw BusinessException")
    void shouldThrowWhenStreetIsBlank() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setAddress("   "));
      assertEquals(ErrorMessage.Address.STREET_REQUIRED, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When street is shorter than 3 characters, should throw BusinessException")
    void shouldThrowWhenStreetIsTooShort() {
      Address address = validAddress();
      BusinessException ex = assertThrows(BusinessException.class, () -> address.setAddress("Ab"));
      assertEquals(ErrorMessage.Address.STREET_INVALID_LENGTH, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When street exceeds 150 characters, should throw BusinessException")
    void shouldThrowWhenStreetIsTooLong() {
      Address address = validAddress();
      String longStreet = "A".repeat(151);
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setAddress(longStreet));
      assertEquals(ErrorMessage.Address.STREET_INVALID_LENGTH, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When street contains invalid characters, should throw BusinessException")
    void shouldThrowWhenStreetHasInvalidCharacters() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setAddress("Rua @Inválida!"));
      assertEquals(ErrorMessage.Address.STREET_INVALID_CHARACTERS, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When street is valid, should set street")
    void shouldSetStreetWhenValid() {
      Address address = validAddress();
      address.setAddress("Rua das Flores, 10");
      assertEquals("Rua das Flores, 10", address.getAddress());
    }
  }

  @Nested
  class SetNumber {

    @Test
    @DisplayName("When number is null, should throw BusinessException")
    void shouldThrowWhenNumberIsNull() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setNumber(null));
      assertEquals(ErrorMessage.Address.NUMBER_REQUIRED, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When number exceeds 10 characters, should throw BusinessException")
    void shouldThrowWhenNumberIsTooLong() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setNumber("12345678901"));
      assertEquals(ErrorMessage.Address.NUMBER_TOO_LONG, ex.getErrorMessage());
    }

    @ParameterizedTest(name = "Number \"{0}\" is invalid")
    @ValueSource(strings = {"12.3", "101,B", "A@1"})
    @DisplayName("When number has invalid characters, should throw BusinessException")
    void shouldThrowWhenNumberIsInvalid(String number) {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setNumber(number));
      assertEquals(ErrorMessage.Address.NUMBER_INVALID, ex.getErrorMessage());
    }

    @ParameterizedTest(name = "Number \"{0}\" is valid")
    @ValueSource(strings = {"123", "12A", "101-B", "S/N", "SN", "s/n", "sn", "1A/2B"})
    @DisplayName("When number is valid, should set number")
    void shouldSetNumberWhenValid(String number) {
      Address address = validAddress();
      assertDoesNotThrow(() -> address.setNumber(number));
    }
  }

  @Nested
  class SetComplement {

    @Test
    @DisplayName("When complement is null, should set null")
    void shouldSetNullComplement() {
      Address address = validAddress();
      assertDoesNotThrow(() -> address.setComplement(null));
      assertNull(address.getComplement());
    }

    @Test
    @DisplayName("When complement is blank, should throw BusinessException")
    void shouldThrowWhenComplementIsBlank() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setComplement("   "));
      assertEquals(ErrorMessage.Address.COMPLEMENT_WHITESPACE_ONLY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When complement exceeds 100 characters, should throw BusinessException")
    void shouldThrowWhenComplementIsTooLong() {
      Address address = validAddress();
      String longComplement = "A".repeat(101);
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setComplement(longComplement));
      assertEquals(ErrorMessage.Address.COMPLEMENT_TOO_LONG, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When complement contains invalid characters, should throw BusinessException")
    void shouldThrowWhenComplementHasInvalidCharacters() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setComplement("Apto @5!"));
      assertEquals(ErrorMessage.Address.COMPLEMENT_INVALID_CHARACTERS, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When complement is valid, should set complement")
    void shouldSetComplementWhenValid() {
      Address address = validAddress();
      address.setComplement("Apto 42");
      assertEquals("Apto 42", address.getComplement());
    }
  }

  @Nested
  class SetNeighborhood {

    @Test
    @DisplayName("When neighborhood is null, should throw BusinessException")
    void shouldThrowWhenNeighborhoodIsNull() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setNeighborhood(null));
      assertEquals(ErrorMessage.Address.NEIGHBORHOOD_REQUIRED, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When neighborhood is shorter than 2 characters, should throw BusinessException")
    void shouldThrowWhenNeighborhoodIsTooShort() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setNeighborhood("A"));
      assertEquals(ErrorMessage.Address.NEIGHBORHOOD_INVALID_LENGTH, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When neighborhood exceeds 100 characters, should throw BusinessException")
    void shouldThrowWhenNeighborhoodIsTooLong() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setNeighborhood("A".repeat(101)));
      assertEquals(ErrorMessage.Address.NEIGHBORHOOD_INVALID_LENGTH, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When neighborhood contains invalid characters, should throw BusinessException")
    void shouldThrowWhenNeighborhoodHasInvalidCharacters() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setNeighborhood("Bairro1"));
      assertEquals(ErrorMessage.Address.NEIGHBORHOOD_INVALID_CHARACTERS, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When neighborhood is valid, should set neighborhood")
    void shouldSetNeighborhoodWhenValid() {
      Address address = validAddress();
      address.setNeighborhood("Vila Madalena");
      assertEquals("Vila Madalena", address.getNeighborhood());
    }
  }

  @Nested
  class SetCity {

    @Test
    @DisplayName("When city is null, should throw BusinessException")
    void shouldThrowWhenCityIsNull() {
      Address address = validAddress();
      BusinessException ex = assertThrows(BusinessException.class, () -> address.setCity(null));
      assertEquals(ErrorMessage.Address.CITY_REQUIRED, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When city is shorter than 2 characters, should throw BusinessException")
    void shouldThrowWhenCityIsTooShort() {
      Address address = validAddress();
      BusinessException ex = assertThrows(BusinessException.class, () -> address.setCity("A"));
      assertEquals(ErrorMessage.Address.CITY_INVALID_LENGTH, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When city exceeds 100 characters, should throw BusinessException")
    void shouldThrowWhenCityIsTooLong() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setCity("A".repeat(101)));
      assertEquals(ErrorMessage.Address.CITY_INVALID_LENGTH, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When city contains invalid characters, should throw BusinessException")
    void shouldThrowWhenCityHasInvalidCharacters() {
      Address address = validAddress();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> address.setCity("São Paulo2"));
      assertEquals(ErrorMessage.Address.CITY_INVALID_CHARACTERS, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When city is valid, should set city")
    void shouldSetCityWhenValid() {
      Address address = validAddress();
      address.setCity("Belo Horizonte");
      assertEquals("Belo Horizonte", address.getCity());
    }
  }

  @Nested
  class SetState {

    @Test
    @DisplayName("When state is null, should throw BusinessException")
    void shouldThrowWhenStateIsNull() {
      Address address = validAddress();
      BusinessException ex = assertThrows(BusinessException.class, () -> address.setState(null));
      assertEquals(ErrorMessage.Address.STATE_REQUIRED, ex.getErrorMessage());
    }

    @ParameterizedTest(name = "State {0} is valid")
    @ValueSource(strings = {"AC", "AL", "AP", "AM", "BA", "CE", "DF", "ES", "GO", "MA", "MT",
        "MS", "MG", "PA", "PB", "PR", "PE", "PI", "RJ", "RN", "RS", "RO", "RR", "SC", "SP",
        "SE", "TO"})
    @DisplayName("When state is a valid BrazilianState, should set state")
    void shouldSetStateWhenValid(String stateCode) {
      Address address = validAddress();
      BrazilianState state = BrazilianState.valueOf(stateCode);
      assertDoesNotThrow(() -> address.setState(state));
      assertEquals(state, address.getState());
    }
  }
}
