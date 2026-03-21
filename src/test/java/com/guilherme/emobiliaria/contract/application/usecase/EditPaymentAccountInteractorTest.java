package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.EditPaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.output.CreatePaymentAccountOutput;
import com.guilherme.emobiliaria.contract.application.output.EditPaymentAccountOutput;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EditPaymentAccountInteractorTest {

  private FakePaymentAccountRepository paymentAccountRepository;
  private EditPaymentAccountInteractor interactor;

  @BeforeEach
  void setUp() {
    paymentAccountRepository = new FakePaymentAccountRepository();
    interactor = new EditPaymentAccountInteractor(paymentAccountRepository);
  }

  private Long createAccount() {
    CreatePaymentAccountOutput output = new CreatePaymentAccountInteractor(paymentAccountRepository)
        .execute(new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", null));
    return output.paymentAccount().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When account exists, should update and return the account with new values")
    void shouldUpdatePaymentAccountWhenAccountExists() {
      Long id = createAccount();
      EditPaymentAccountInput input =
          new EditPaymentAccountInput(id, "Caixa Econômica", "9876-5", "98765-4", "new@pix.com");

      EditPaymentAccountOutput output = interactor.execute(input);

      assertEquals(id, output.paymentAccount().getId());
      assertEquals("Caixa Econômica", output.paymentAccount().getBank());
      assertEquals("9876-5", output.paymentAccount().getBankBranch());
      assertEquals("98765-4", output.paymentAccount().getAccountNumber());
      assertEquals("new@pix.com", output.paymentAccount().getPixKey());
    }

    @Test
    @DisplayName("When account does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowBusinessExceptionWhenAccountNotFound() {
      EditPaymentAccountInput input =
          new EditPaymentAccountInput(999L, "Banco do Brasil", "1234-5", "12345-6", null);

      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(input));
      assertEquals(ErrorMessage.PaymentAccount.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
