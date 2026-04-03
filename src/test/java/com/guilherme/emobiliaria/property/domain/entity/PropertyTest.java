package com.guilherme.emobiliaria.property.domain.entity;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
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

class PropertyTest {

  private static final String VALID_NAME = "Apartamento Centro";
  private static final String VALID_TYPE = "Apartamento";
  private static final Purpose VALID_PURPOSE = Purpose.RESIDENTIAL;
  private static final String VALID_CEMIG = "1234567890";
  private static final String VALID_COPASA = "0987654321";
  private static final String VALID_IPTU = "IPTU-001";

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Property validProperty() {
    return Property.create(VALID_NAME, VALID_TYPE, VALID_PURPOSE, VALID_CEMIG,
        VALID_COPASA, VALID_IPTU, validAddress());
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When all fields are valid, should create Property with null id")
    void shouldCreateWhenAllFieldsAreValid() {
      Property property = validProperty();

      assertNull(property.getId());
      assertEquals(VALID_NAME, property.getName());
      assertEquals(VALID_TYPE, property.getType());
      assertEquals(VALID_PURPOSE, property.getPurpose());
      assertEquals(VALID_CEMIG, property.getCemig());
      assertEquals(VALID_COPASA, property.getCopasa());
      assertEquals(VALID_IPTU, property.getIptu());
    }
  }


  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with id, should set id")
    void shouldRestoreWithId() {
      Property property =
          Property.restore(42L, VALID_NAME, VALID_TYPE, VALID_PURPOSE, VALID_CEMIG,
              VALID_COPASA, VALID_IPTU, validAddress());

      assertEquals(42L, property.getId());
    }
  }


  @Nested
  class SetName {

    @Test
    @DisplayName("When name is null, should throw BusinessException")
    void shouldThrowWhenNameIsNull() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setName(null));
      assertEquals(ErrorMessage.Property.NAME_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When name is blank, should throw BusinessException")
    void shouldThrowWhenNameIsBlank() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setName("   "));
      assertEquals(ErrorMessage.Property.NAME_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When name is valid, should set name")
    void shouldSetNameWhenValid() {
      Property property = validProperty();
      assertDoesNotThrow(() -> property.setName("Casa na Praia"));
      assertEquals("Casa na Praia", property.getName());
    }
  }


  @Nested
  class SetType {

    @Test
    @DisplayName("When type is null, should throw BusinessException")
    void shouldThrowWhenTypeIsNull() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setType(null));
      assertEquals(ErrorMessage.Property.TYPE_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When type is blank, should throw BusinessException")
    void shouldThrowWhenTypeIsBlank() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setType("  "));
      assertEquals(ErrorMessage.Property.TYPE_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When type is valid, should set type")
    void shouldSetTypeWhenValid() {
      Property property = validProperty();
      assertDoesNotThrow(() -> property.setType("Casa"));
      assertEquals("Casa", property.getType());
    }
  }


  @Nested
  class SetPurpose {

    @Test
    @DisplayName("When purpose is null, should throw BusinessException")
    void shouldThrowWhenPurposeIsNull() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setPurpose(null));
      assertEquals(ErrorMessage.Property.PURPOSE_NULL, ex.getErrorMessage());
    }

    @ParameterizedTest(name = "Purpose {0} is valid")
    @ValueSource(strings = {"RESIDENTIAL", "COMMERCIAL"})
    @DisplayName("When purpose is a valid value, should set it")
    void shouldSetPurposeWhenValid(String purposeValue) {
      Property property = validProperty();
      Purpose purpose = Purpose.valueOf(purposeValue);
      assertDoesNotThrow(() -> property.setPurpose(purpose));
      assertEquals(purpose, property.getPurpose());
    }
  }


  @Nested
  class SetCemig {

    @Test
    @DisplayName("When cemig is null, should throw BusinessException")
    void shouldThrowWhenCemigIsNull() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setCemig(null));
      assertEquals(ErrorMessage.Property.CEMIG_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When cemig is blank, should throw BusinessException")
    void shouldThrowWhenCemigIsBlank() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setCemig("  "));
      assertEquals(ErrorMessage.Property.CEMIG_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When cemig is valid, should set cemig")
    void shouldSetCemigWhenValid() {
      Property property = validProperty();
      assertDoesNotThrow(() -> property.setCemig("9999999999"));
      assertEquals("9999999999", property.getCemig());
    }
  }


  @Nested
  class SetCopasa {

    @Test
    @DisplayName("When copasa is null, should throw BusinessException")
    void shouldThrowWhenCopasaIsNull() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setCopasa(null));
      assertEquals(ErrorMessage.Property.COPASA_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When copasa is blank, should throw BusinessException")
    void shouldThrowWhenCopasaIsBlank() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setCopasa("  "));
      assertEquals(ErrorMessage.Property.COPASA_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When copasa is valid, should set copasa")
    void shouldSetCopasaWhenValid() {
      Property property = validProperty();
      assertDoesNotThrow(() -> property.setCopasa("1111111111"));
      assertEquals("1111111111", property.getCopasa());
    }
  }


  @Nested
  class SetIptu {

    @Test
    @DisplayName("When iptu is null, should throw BusinessException")
    void shouldThrowWhenIptuIsNull() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setIptu(null));
      assertEquals(ErrorMessage.Property.IPTU_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When iptu is blank, should throw BusinessException")
    void shouldThrowWhenIptuIsBlank() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setIptu("  "));
      assertEquals(ErrorMessage.Property.IPTU_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When iptu is valid, should set iptu")
    void shouldSetIptuWhenValid() {
      Property property = validProperty();
      assertDoesNotThrow(() -> property.setIptu("IPTU-999"));
      assertEquals("IPTU-999", property.getIptu());
    }
  }


  @Nested
  class SetAddress {

    @Test
    @DisplayName("When address is null, should throw BusinessException")
    void shouldThrowWhenAddressIsNull() {
      Property property = validProperty();
      BusinessException ex = assertThrows(BusinessException.class, () -> property.setAddress(null));
      assertEquals(ErrorMessage.Property.ADDRESS_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When address is valid, should set address")
    void shouldSetAddressWhenValid() {
      Property property = validProperty();
      Address newAddress = validAddress();
      assertDoesNotThrow(() -> property.setAddress(newAddress));
      assertEquals(newAddress, property.getAddress());
    }
  }
}
