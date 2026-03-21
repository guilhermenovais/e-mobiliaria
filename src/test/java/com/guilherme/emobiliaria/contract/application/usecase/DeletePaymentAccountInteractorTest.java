package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.DeletePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.output.CreatePaymentAccountOutput;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DeletePaymentAccountInteractorTest {

  private FakePaymentAccountRepository paymentAccountRepository;
  private DeletePaymentAccountInteractor interactor;

  @BeforeEach
  void setUp() {
    paymentAccountRepository = new FakePaymentAccountRepository();
    interactor = new DeletePaymentAccountInteractor(paymentAccountRepository);
  }

  private Long createAccount() {
    CreatePaymentAccountOutput output = new CreatePaymentAccountInteractor(paymentAccountRepository)
        .execute(new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", null));
    return output.paymentAccount().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When account exists, should delete without throwing exception")
    void shouldDeletePaymentAccountWhenAccountExists() {
      Long id = createAccount();

      assertDoesNotThrow(() -> interactor.execute(new DeletePaymentAccountInput(id)));
    }

    @Test
    @DisplayName("When account does not exist, should throw PersistenceException")
    void shouldThrowWhenAccountNotFound() {
      assertThrows(PersistenceException.class,
          () -> interactor.execute(new DeletePaymentAccountInput(999L)));
    }
  }
}
