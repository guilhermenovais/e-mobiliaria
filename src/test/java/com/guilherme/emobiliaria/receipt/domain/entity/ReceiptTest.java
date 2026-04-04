package com.guilherme.emobiliaria.receipt.domain.entity;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
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

class ReceiptTest {

  private static final LocalDate VALID_DATE = LocalDate.of(2026, 3, 1);
  private static final LocalDate VALID_INTERVAL_START = LocalDate.of(2026, 2, 1);
  private static final LocalDate VALID_INTERVAL_END = LocalDate.of(2026, 2, 28);

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Person validPerson() {
    return PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
        "529.982.247-25", "MG-1234567", validAddress());
  }

  private Property validProperty() {
    return Property.create("Apartamento Centro", "Apartamento", Purpose.RESIDENTIAL,
        "1234567890", "0987654321", "IPTU-001", validAddress());
  }

  private Contract validContract() {
    PaymentAccount paymentAccount =
        PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, 150000, paymentAccount,
        validProperty(), validPerson(), List.of(validPerson()));
  }

  private Receipt validReceipt() {
    return Receipt.create(VALID_DATE, VALID_INTERVAL_START, VALID_INTERVAL_END, 0, 0, null,
        validContract());
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When all fields are valid, should create Receipt with null id")
    void shouldCreateWithNullIdWhenAllFieldsAreValid() {
      Receipt receipt = validReceipt();

      assertNull(receipt.getId());
      assertEquals(VALID_DATE, receipt.getDate());
      assertEquals(VALID_INTERVAL_START, receipt.getIntervalStart());
      assertEquals(VALID_INTERVAL_END, receipt.getIntervalEnd());
    }
  }


  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with id, should set id")
    void shouldRestoreWithId() {
      Receipt receipt =
          Receipt.restore(42L, VALID_DATE, VALID_INTERVAL_START, VALID_INTERVAL_END, 0, 0, null,
              validContract());

      assertEquals(42L, receipt.getId());
    }
  }


  @Nested
  class SetDate {

    @Test
    @DisplayName("When date is null, should throw BusinessException")
    void shouldThrowWhenDateIsNull() {
      Receipt receipt = validReceipt();
      BusinessException ex = assertThrows(BusinessException.class, () -> receipt.setDate(null));
      assertEquals(ErrorMessage.Receipt.DATE_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When date is valid, should set date")
    void shouldSetDateWhenValid() {
      Receipt receipt = validReceipt();
      LocalDate newDate = LocalDate.of(2026, 4, 1);
      assertDoesNotThrow(() -> receipt.setDate(newDate));
      assertEquals(newDate, receipt.getDate());
    }
  }


  @Nested
  class SetIntervalStart {

    @Test
    @DisplayName("When intervalStart is null, should throw BusinessException")
    void shouldThrowWhenIntervalStartIsNull() {
      Receipt receipt = validReceipt();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> receipt.setIntervalStart(null));
      assertEquals(ErrorMessage.Receipt.INTERVAL_START_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When intervalStart is after intervalEnd, should throw BusinessException")
    void shouldThrowWhenIntervalStartIsAfterIntervalEnd() {
      Receipt receipt = validReceipt();
      LocalDate afterEnd = VALID_INTERVAL_END.plusDays(1);
      BusinessException ex =
          assertThrows(BusinessException.class, () -> receipt.setIntervalStart(afterEnd));
      assertEquals(ErrorMessage.Receipt.INTERVAL_START_AFTER_INTERVAL_END, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When intervalStart is valid, should set intervalStart")
    void shouldSetIntervalStartWhenValid() {
      Receipt receipt = validReceipt();
      LocalDate newStart = LocalDate.of(2026, 2, 5);
      assertDoesNotThrow(() -> receipt.setIntervalStart(newStart));
      assertEquals(newStart, receipt.getIntervalStart());
    }
  }


  @Nested
  class SetIntervalEnd {

    @Test
    @DisplayName("When intervalEnd is null, should throw BusinessException")
    void shouldThrowWhenIntervalEndIsNull() {
      Receipt receipt = validReceipt();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> receipt.setIntervalEnd(null));
      assertEquals(ErrorMessage.Receipt.INTERVAL_END_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When intervalEnd is before intervalStart, should throw BusinessException")
    void shouldThrowWhenIntervalEndIsBeforeIntervalStart() {
      Receipt receipt = validReceipt();
      LocalDate beforeStart = VALID_INTERVAL_START.minusDays(1);
      BusinessException ex =
          assertThrows(BusinessException.class, () -> receipt.setIntervalEnd(beforeStart));
      assertEquals(ErrorMessage.Receipt.INTERVAL_END_BEFORE_INTERVAL_START, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When intervalEnd is valid, should set intervalEnd")
    void shouldSetIntervalEndWhenValid() {
      Receipt receipt = validReceipt();
      LocalDate newEnd = LocalDate.of(2026, 2, 20);
      assertDoesNotThrow(() -> receipt.setIntervalEnd(newEnd));
      assertEquals(newEnd, receipt.getIntervalEnd());
    }
  }


  @Nested
  class SetDiscount {

    @Test
    @DisplayName("When discount is negative, should throw BusinessException")
    void shouldThrowWhenDiscountIsNegative() {
      Receipt receipt = validReceipt();
      BusinessException ex = assertThrows(BusinessException.class, () -> receipt.setDiscount(-1));
      assertEquals(ErrorMessage.Receipt.DISCOUNT_NEGATIVE, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When discount is zero, should set discount")
    void shouldSetDiscountWhenZero() {
      Receipt receipt = validReceipt();
      assertDoesNotThrow(() -> receipt.setDiscount(0));
      assertEquals(0, receipt.getDiscount());
    }

    @Test
    @DisplayName("When discount is positive, should set discount")
    void shouldSetDiscountWhenPositive() {
      Receipt receipt = validReceipt();
      assertDoesNotThrow(() -> receipt.setDiscount(500));
      assertEquals(500, receipt.getDiscount());
    }
  }


  @Nested
  class SetFine {

    @Test
    @DisplayName("When fine is negative, should throw BusinessException")
    void shouldThrowWhenFineIsNegative() {
      Receipt receipt = validReceipt();
      BusinessException ex = assertThrows(BusinessException.class, () -> receipt.setFine(-1));
      assertEquals(ErrorMessage.Receipt.FINE_NEGATIVE, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When fine is zero, should set fine")
    void shouldSetFineWhenZero() {
      Receipt receipt = validReceipt();
      assertDoesNotThrow(() -> receipt.setFine(0));
      assertEquals(0, receipt.getFine());
    }

    @Test
    @DisplayName("When fine is positive, should set fine")
    void shouldSetFineWhenPositive() {
      Receipt receipt = validReceipt();
      assertDoesNotThrow(() -> receipt.setFine(1000));
      assertEquals(1000, receipt.getFine());
    }
  }


  @Nested
  class SetContract {

    @Test
    @DisplayName("When contract is null, should throw BusinessException")
    void shouldThrowWhenContractIsNull() {
      Receipt receipt = validReceipt();
      BusinessException ex = assertThrows(BusinessException.class, () -> receipt.setContract(null));
      assertEquals(ErrorMessage.Receipt.CONTRACT_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When contract is valid, should set contract")
    void shouldSetContractWhenValid() {
      Receipt receipt = validReceipt();
      Contract newContract = validContract();
      assertDoesNotThrow(() -> receipt.setContract(newContract));
      assertEquals(newContract, receipt.getContract());
    }
  }
}
