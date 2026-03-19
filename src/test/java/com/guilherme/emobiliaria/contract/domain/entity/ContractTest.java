package com.guilherme.emobiliaria.contract.domain.entity;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ContractTest {

  private static final LocalDate VALID_START_DATE = LocalDate.of(2026, 1, 1);
  private static final Period VALID_DURATION = Period.ofMonths(12);
  private static final int VALID_PAYMENT_DAY = 10;

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private PaymentAccount validPaymentAccount() {
    return PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
  }

  private Property validProperty() {
    return Property.create("Apartamento Centro", "Apartamento", Purpose.RESIDENTIAL, 150000,
        "1234567890", "0987654321", "IPTU-001", validAddress());
  }

  private Person validPerson() {
    return PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
        "529.982.247-25", "MG-1234567", validAddress());
  }

  private Contract validContract() {
    return Contract.create(VALID_START_DATE, VALID_DURATION, VALID_PAYMENT_DAY,
        validPaymentAccount(), validProperty(), validPerson(), List.of(validPerson()));
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When all fields are valid, should create Contract with null id")
    void shouldCreateWhenAllFieldsAreValid() {
      Contract contract = validContract();

      assertNull(contract.getId());
      assertEquals(VALID_START_DATE, contract.getStartDate());
      assertEquals(VALID_DURATION, contract.getDuration());
      assertEquals(VALID_PAYMENT_DAY, contract.getPaymentDay());
    }
  }


  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with id, should set id")
    void shouldRestoreWithId() {
      Contract contract = Contract.restore(42L, VALID_START_DATE, VALID_DURATION, VALID_PAYMENT_DAY,
          validPaymentAccount(), validProperty(), validPerson(), List.of(validPerson()));

      assertEquals(42L, contract.getId());
    }
  }


  @Nested
  class SetStartDate {

    @Test
    @DisplayName("When startDate is null, should throw BusinessException")
    void shouldThrowWhenStartDateIsNull() {
      Contract contract = validContract();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> contract.setStartDate(null));
      assertEquals(ErrorMessage.Contract.START_DATE_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When startDate is valid, should set startDate")
    void shouldSetStartDateWhenValid() {
      Contract contract = validContract();
      LocalDate newDate = LocalDate.of(2026, 6, 1);
      assertDoesNotThrow(() -> contract.setStartDate(newDate));
      assertEquals(newDate, contract.getStartDate());
    }
  }


  @Nested
  class SetDuration {

    @Test
    @DisplayName("When duration is null, should throw BusinessException")
    void shouldThrowWhenDurationIsNull() {
      Contract contract = validContract();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> contract.setDuration(null));
      assertEquals(ErrorMessage.Contract.DURATION_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When duration is valid, should set duration")
    void shouldSetDurationWhenValid() {
      Contract contract = validContract();
      Period newDuration = Period.ofYears(2);
      assertDoesNotThrow(() -> contract.setDuration(newDuration));
      assertEquals(newDuration, contract.getDuration());
    }
  }


  @Nested
  class SetPaymentDay {

    @Test
    @DisplayName("When paymentDay is 0, should throw BusinessException")
    void shouldThrowWhenPaymentDayIsZero() {
      Contract contract = validContract();
      BusinessException ex = assertThrows(BusinessException.class, () -> contract.setPaymentDay(0));
      assertEquals(ErrorMessage.Contract.PAYMENT_DAY_INVALID, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When paymentDay is 32, should throw BusinessException")
    void shouldThrowWhenPaymentDayIsThirtyTwo() {
      Contract contract = validContract();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> contract.setPaymentDay(32));
      assertEquals(ErrorMessage.Contract.PAYMENT_DAY_INVALID, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When paymentDay is 1, should set paymentDay")
    void shouldSetPaymentDayWhenOne() {
      Contract contract = validContract();
      assertDoesNotThrow(() -> contract.setPaymentDay(1));
      assertEquals(1, contract.getPaymentDay());
    }

    @Test
    @DisplayName("When paymentDay is 31, should set paymentDay")
    void shouldSetPaymentDayWhenThirtyOne() {
      Contract contract = validContract();
      assertDoesNotThrow(() -> contract.setPaymentDay(31));
      assertEquals(31, contract.getPaymentDay());
    }
  }


  @Nested
  class SetPaymentAccount {

    @Test
    @DisplayName("When paymentAccount is null, should throw BusinessException")
    void shouldThrowWhenPaymentAccountIsNull() {
      Contract contract = validContract();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> contract.setPaymentAccount(null));
      assertEquals(ErrorMessage.Contract.PAYMENT_ACCOUNT_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When paymentAccount is valid, should set paymentAccount")
    void shouldSetPaymentAccountWhenValid() {
      Contract contract = validContract();
      PaymentAccount newAccount = validPaymentAccount();
      assertDoesNotThrow(() -> contract.setPaymentAccount(newAccount));
      assertEquals(newAccount, contract.getPaymentAccount());
    }
  }


  @Nested
  class SetProperty {

    @Test
    @DisplayName("When property is null, should throw BusinessException")
    void shouldThrowWhenPropertyIsNull() {
      Contract contract = validContract();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> contract.setProperty(null));
      assertEquals(ErrorMessage.Contract.PROPERTY_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When property is valid, should set property")
    void shouldSetPropertyWhenValid() {
      Contract contract = validContract();
      Property newProperty = validProperty();
      assertDoesNotThrow(() -> contract.setProperty(newProperty));
      assertEquals(newProperty, contract.getProperty());
    }
  }


  @Nested
  class SetLandlord {

    @Test
    @DisplayName("When landlord is null, should throw BusinessException")
    void shouldThrowWhenLandlordIsNull() {
      Contract contract = validContract();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> contract.setLandlord(null));
      assertEquals(ErrorMessage.Contract.LANDLORD_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When landlord is valid, should set landlord")
    void shouldSetLandlordWhenValid() {
      Contract contract = validContract();
      Person newLandlord = validPerson();
      assertDoesNotThrow(() -> contract.setLandlord(newLandlord));
      assertEquals(newLandlord, contract.getLandlord());
    }
  }


  @Nested
  class SetTenants {

    @Test
    @DisplayName("When tenants is null, should throw BusinessException")
    void shouldThrowWhenTenantsIsNull() {
      Contract contract = validContract();
      BusinessException ex = assertThrows(BusinessException.class, () -> contract.setTenants(null));
      assertEquals(ErrorMessage.Contract.TENANTS_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When tenants is empty, should throw BusinessException")
    void shouldThrowWhenTenantsIsEmpty() {
      Contract contract = validContract();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> contract.setTenants(List.of()));
      assertEquals(ErrorMessage.Contract.TENANTS_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When tenants is valid, should set tenants")
    void shouldSetTenantsWhenValid() {
      Contract contract = validContract();
      List<Person> newTenants = List.of(validPerson());
      assertDoesNotThrow(() -> contract.setTenants(newTenants));
      assertEquals(newTenants, contract.getTenants());
    }
  }
}
