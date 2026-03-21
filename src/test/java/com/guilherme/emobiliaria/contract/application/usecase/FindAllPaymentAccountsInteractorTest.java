package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.FindAllPaymentAccountsInput;
import com.guilherme.emobiliaria.contract.application.output.FindAllPaymentAccountsOutput;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindAllPaymentAccountsInteractorTest {

  private FakePaymentAccountRepository paymentAccountRepository;
  private FindAllPaymentAccountsInteractor interactor;

  @BeforeEach
  void setUp() {
    paymentAccountRepository = new FakePaymentAccountRepository();
    interactor = new FindAllPaymentAccountsInteractor(paymentAccountRepository);
  }

  private void createAccount(String bank) {
    new CreatePaymentAccountInteractor(paymentAccountRepository)
        .execute(new CreatePaymentAccountInput(bank, "1234-5", "12345-6", null));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When accounts exist, should return all accounts with correct total")
    void shouldReturnAllPaymentAccounts() {
      createAccount("Banco do Brasil");
      createAccount("Caixa Econômica");
      FindAllPaymentAccountsInput input =
          new FindAllPaymentAccountsInput(new PaginationInput(null, null));

      FindAllPaymentAccountsOutput output = interactor.execute(input);

      assertEquals(2, output.result().total());
      assertEquals(2, output.result().items().size());
    }

    @Test
    @DisplayName("When no accounts exist, should return empty result")
    void shouldReturnEmptyWhenNoAccounts() {
      FindAllPaymentAccountsInput input =
          new FindAllPaymentAccountsInput(new PaginationInput(null, null));

      FindAllPaymentAccountsOutput output = interactor.execute(input);

      assertEquals(0, output.result().total());
      assertEquals(0, output.result().items().size());
    }

    @Test
    @DisplayName("When pagination is applied, should return limited accounts")
    void shouldReturnLimitedAccountsWhenPaginationIsApplied() {
      createAccount("Banco do Brasil");
      createAccount("Caixa Econômica");
      createAccount("Itaú");
      FindAllPaymentAccountsInput input =
          new FindAllPaymentAccountsInput(new PaginationInput(2, 0));

      FindAllPaymentAccountsOutput output = interactor.execute(input);

      assertEquals(3, output.result().total());
      assertEquals(2, output.result().items().size());
    }
  }
}
