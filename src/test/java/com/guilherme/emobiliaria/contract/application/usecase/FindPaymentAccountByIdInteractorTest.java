package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.FindPaymentAccountByIdInput;
import com.guilherme.emobiliaria.contract.application.output.CreatePaymentAccountOutput;
import com.guilherme.emobiliaria.contract.application.output.FindPaymentAccountByIdOutput;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindPaymentAccountByIdInteractorTest {

  private FakePaymentAccountRepository paymentAccountRepository;
  private FindPaymentAccountByIdInteractor interactor;

  @BeforeEach
  void setUp() {
    paymentAccountRepository = new FakePaymentAccountRepository();
    interactor = new FindPaymentAccountByIdInteractor(paymentAccountRepository);
  }

  private Long createAccount() {
    CreatePaymentAccountOutput output = new CreatePaymentAccountInteractor(paymentAccountRepository)
        .execute(new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", null));
    return output.paymentAccount().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When account exists, should return the payment account")
    void shouldReturnPaymentAccountWhenFound() {
      Long id = createAccount();

      FindPaymentAccountByIdOutput output =
          interactor.execute(new FindPaymentAccountByIdInput(id));

      assertEquals(id, output.paymentAccount().getId());
      assertEquals("Banco do Brasil", output.paymentAccount().getBank());
    }

    @Test
    @DisplayName("When account does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowBusinessExceptionWhenNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new FindPaymentAccountByIdInput(999L)));
      assertEquals(ErrorMessage.PaymentAccount.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
