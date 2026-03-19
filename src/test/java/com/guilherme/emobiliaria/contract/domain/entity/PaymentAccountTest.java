package com.guilherme.emobiliaria.contract.domain.entity;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentAccountTest {

  private static final String VALID_BANK = "Banco do Brasil";
  private static final String VALID_BANK_BRANCH = "1234-5";
  private static final String VALID_ACCOUNT_NUMBER = "12345-6";
  private static final String VALID_PIX_KEY = "pix@email.com";

  private PaymentAccount validPaymentAccount() {
    return PaymentAccount.create(VALID_BANK, VALID_BANK_BRANCH, VALID_ACCOUNT_NUMBER,
        VALID_PIX_KEY);
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When all fields are valid, should create PaymentAccount with null id")
    void shouldCreateWhenAllFieldsAreValid() {
      PaymentAccount account = validPaymentAccount();

      assertNull(account.getId());
      assertEquals(VALID_BANK, account.getBank());
      assertEquals(VALID_BANK_BRANCH, account.getBankBranch());
      assertEquals(VALID_ACCOUNT_NUMBER, account.getAccountNumber());
      assertEquals(VALID_PIX_KEY, account.getPixKey());
    }

    @Test
    @DisplayName("When pixKey is null, should create PaymentAccount with null pixKey")
    void shouldCreateWhenPixKeyIsNull() {
      PaymentAccount account =
          PaymentAccount.create(VALID_BANK, VALID_BANK_BRANCH, VALID_ACCOUNT_NUMBER, null);

      assertNull(account.getPixKey());
    }
  }


  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with id, should set id")
    void shouldRestoreWithId() {
      PaymentAccount account =
          PaymentAccount.restore(42L, VALID_BANK, VALID_BANK_BRANCH, VALID_ACCOUNT_NUMBER,
              VALID_PIX_KEY);

      assertEquals(42L, account.getId());
    }
  }


  @Nested
  class SetBank {

    @Test
    @DisplayName("When bank is null, should throw BusinessException")
    void shouldThrowWhenBankIsNull() {
      PaymentAccount account = validPaymentAccount();
      BusinessException ex = assertThrows(BusinessException.class, () -> account.setBank(null));
      assertEquals(ErrorMessage.PaymentAccount.BANK_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When bank is blank, should throw BusinessException")
    void shouldThrowWhenBankIsBlank() {
      PaymentAccount account = validPaymentAccount();
      BusinessException ex = assertThrows(BusinessException.class, () -> account.setBank("   "));
      assertEquals(ErrorMessage.PaymentAccount.BANK_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When bank is valid, should set bank")
    void shouldSetBankWhenValid() {
      PaymentAccount account = validPaymentAccount();
      assertDoesNotThrow(() -> account.setBank("Caixa Econômica Federal"));
      assertEquals("Caixa Econômica Federal", account.getBank());
    }
  }


  @Nested
  class SetBankBranch {

    @Test
    @DisplayName("When bankBranch is null, should throw BusinessException")
    void shouldThrowWhenBankBranchIsNull() {
      PaymentAccount account = validPaymentAccount();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> account.setBankBranch(null));
      assertEquals(ErrorMessage.PaymentAccount.BANK_BRANCH_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When bankBranch is blank, should throw BusinessException")
    void shouldThrowWhenBankBranchIsBlank() {
      PaymentAccount account = validPaymentAccount();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> account.setBankBranch("  "));
      assertEquals(ErrorMessage.PaymentAccount.BANK_BRANCH_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When bankBranch is valid, should set bankBranch")
    void shouldSetBankBranchWhenValid() {
      PaymentAccount account = validPaymentAccount();
      assertDoesNotThrow(() -> account.setBankBranch("9999-0"));
      assertEquals("9999-0", account.getBankBranch());
    }
  }


  @Nested
  class SetAccountNumber {

    @Test
    @DisplayName("When accountNumber is null, should throw BusinessException")
    void shouldThrowWhenAccountNumberIsNull() {
      PaymentAccount account = validPaymentAccount();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> account.setAccountNumber(null));
      assertEquals(ErrorMessage.PaymentAccount.ACCOUNT_NUMBER_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When accountNumber is blank, should throw BusinessException")
    void shouldThrowWhenAccountNumberIsBlank() {
      PaymentAccount account = validPaymentAccount();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> account.setAccountNumber("  "));
      assertEquals(ErrorMessage.PaymentAccount.ACCOUNT_NUMBER_EMPTY, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When accountNumber is valid, should set accountNumber")
    void shouldSetAccountNumberWhenValid() {
      PaymentAccount account = validPaymentAccount();
      assertDoesNotThrow(() -> account.setAccountNumber("99999-0"));
      assertEquals("99999-0", account.getAccountNumber());
    }
  }
}
