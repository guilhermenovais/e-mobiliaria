package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.output.CreatePaymentAccountOutput;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreatePaymentAccountInteractorTest {

  private FakePaymentAccountRepository paymentAccountRepository;
  private CreatePaymentAccountInteractor interactor;

  @BeforeEach
  void setUp() {
    paymentAccountRepository = new FakePaymentAccountRepository();
    interactor = new CreatePaymentAccountInteractor(paymentAccountRepository);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When all fields are valid, should create and return payment account with id")
    void shouldCreatePaymentAccountWhenAllFieldsAreValid() {
      CreatePaymentAccountInput input =
          new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", "pix@bank.com");

      CreatePaymentAccountOutput output = interactor.execute(input);

      assertNotNull(output.paymentAccount().getId());
      assertEquals("Banco do Brasil", output.paymentAccount().getBank());
      assertEquals("1234-5", output.paymentAccount().getBankBranch());
      assertEquals("12345-6", output.paymentAccount().getAccountNumber());
      assertEquals("pix@bank.com", output.paymentAccount().getPixKey());
    }

    @Test
    @DisplayName("When pixKey is null, should create payment account without pixKey")
    void shouldCreatePaymentAccountWhenPixKeyIsNull() {
      CreatePaymentAccountInput input =
          new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", null);

      CreatePaymentAccountOutput output = interactor.execute(input);

      assertNotNull(output.paymentAccount().getId());
      assertEquals(null, output.paymentAccount().getPixKey());
    }

    @Test
    @DisplayName("When repository fails, should propagate exception")
    void shouldPropagateExceptionWhenRepositoryFails() {
      paymentAccountRepository.failNext(
          () -> new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, new RuntimeException()));
      CreatePaymentAccountInput input =
          new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", null);

      assertThrows(PersistenceException.class, () -> interactor.execute(input));
    }
  }
}
