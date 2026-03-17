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

class JuridicalPersonTest {

  private static final String VALID_CORPORATE_NAME = "Empresa Teste Ltda";
  private static final String VALID_CNPJ = "11222333000181";

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private PhysicalPerson validRepresentative() {
    return PhysicalPerson.create("João da Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
        "52998224725", "MG-12.345.678", validAddress());
  }

  private JuridicalPerson validJuridicalPerson() {
    return JuridicalPerson.create(VALID_CORPORATE_NAME, VALID_CNPJ, validRepresentative(),
        validAddress());
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When all fields are valid, should create JuridicalPerson with null id")
    void shouldCreateWhenAllFieldsAreValid() {
      JuridicalPerson person = validJuridicalPerson();

      assertNull(person.getId());
      assertEquals(VALID_CORPORATE_NAME, person.getCorporateName());
      assertEquals(VALID_CNPJ, person.getCnpj());
    }
  }

  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with id, should set id")
    void shouldRestoreWithId() {
      JuridicalPerson person = JuridicalPerson.restore(10L, VALID_CORPORATE_NAME, VALID_CNPJ,
          validRepresentative(), validAddress());

      assertEquals(10L, person.getId());
    }
  }

  @Nested
  class SetCorporateName {

    @Test
    @DisplayName("When corporate name is null, should throw BusinessException")
    void shouldThrowWhenCorporateNameIsNull() {
      JuridicalPerson person = validJuridicalPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setCorporateName(null));
      assertEquals(ErrorMessage.JuridicalPerson.CORPORATE_NAME_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When corporate name is blank, should throw BusinessException")
    void shouldThrowWhenCorporateNameIsBlank() {
      JuridicalPerson person = validJuridicalPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setCorporateName("   "));
      assertEquals(ErrorMessage.JuridicalPerson.CORPORATE_NAME_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When corporate name has 100 or more characters, should throw BusinessException")
    void shouldThrowWhenCorporateNameExceedsLimit() {
      JuridicalPerson person = validJuridicalPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setCorporateName("A".repeat(100)));
      assertEquals(ErrorMessage.JuridicalPerson.CORPORATE_NAME_TOO_LONG, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When corporate name has 99 characters, should set corporate name")
    void shouldSetCorporateNameAtMaxLength() {
      JuridicalPerson person = validJuridicalPerson();
      String name = "A".repeat(99);
      assertDoesNotThrow(() -> person.setCorporateName(name));
      assertEquals(name, person.getCorporateName());
    }
  }

  @Nested
  class SetCnpj {

    @Test
    @DisplayName("When CNPJ is null, should throw BusinessException")
    void shouldThrowWhenCnpjIsNull() {
      JuridicalPerson person = validJuridicalPerson();
      assertThrows(BusinessException.class, () -> person.setCnpj(null));
    }

    @ParameterizedTest(name = "CNPJ \"{0}\" is invalid")
    @ValueSource(strings = {"00000000000000", "11222333000182", "1234567890123"})
    @DisplayName("When CNPJ is invalid, should throw BusinessException")
    void shouldThrowWhenCnpjIsInvalid(String cnpj) {
      JuridicalPerson person = validJuridicalPerson();
      assertThrows(BusinessException.class, () -> person.setCnpj(cnpj));
    }

    @Test
    @DisplayName("When CNPJ is valid, should set normalized CNPJ")
    void shouldSetNormalizedCnpjWhenValid() {
      JuridicalPerson person = validJuridicalPerson();
      person.setCnpj("11.222.333/0001-81");
      assertEquals("11222333000181", person.getCnpj());
    }
  }

  @Nested
  class SetRepresentative {

    @Test
    @DisplayName("When representative is null, should throw BusinessException")
    void shouldThrowWhenRepresentativeIsNull() {
      JuridicalPerson person = validJuridicalPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setRepresentative(null));
      assertEquals(ErrorMessage.JuridicalPerson.REPRESENTATIVE_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When representative is valid, should set representative")
    void shouldSetRepresentativeWhenValid() {
      JuridicalPerson person = validJuridicalPerson();
      PhysicalPerson representative = validRepresentative();
      assertDoesNotThrow(() -> person.setRepresentative(representative));
      assertEquals(representative, person.getRepresentative());
    }
  }

  @Nested
  class SetAddress {

    @Test
    @DisplayName("When address is null, should throw BusinessException")
    void shouldThrowWhenAddressIsNull() {
      JuridicalPerson person = validJuridicalPerson();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> person.setAddress(null));
      assertEquals(ErrorMessage.JuridicalPerson.ADDRESS_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When address is valid, should set address")
    void shouldSetAddressWhenValid() {
      JuridicalPerson person = validJuridicalPerson();
      Address newAddress = validAddress();
      assertDoesNotThrow(() -> person.setAddress(newAddress));
      assertEquals(newAddress, person.getAddress());
    }
  }
}
