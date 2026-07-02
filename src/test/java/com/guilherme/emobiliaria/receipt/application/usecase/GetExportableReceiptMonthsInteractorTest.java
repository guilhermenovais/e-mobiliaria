package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.FakeReceiptRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetExportableReceiptMonthsInteractorTest {

  private final FakeReceiptRepository receiptRepository = new FakeReceiptRepository();
  private final GetExportableReceiptMonthsInteractor interactor =
      new GetExportableReceiptMonthsInteractor(receiptRepository);

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Contract validContract() {
    Person tenant =
        PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
            "529.982.247-25", "MG-1234567", validAddress());
    PaymentAccount paymentAccount =
        PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
    Property property =
        Property.create("Apto 1", "Apartamento", "1234567890", "0987654321", "IPTU-001",
            validAddress());
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, 150000, "Residencial",
        paymentAccount, property, tenant, List.of(tenant), List.of(), List.of());
  }

  private void createReceipt(LocalDate date) {
    receiptRepository.create(Receipt.create(date, date, date, date, 0, 0, null, validContract()));
  }

  @Test
  @DisplayName("When months have at least one receipt, should return them")
  void shouldReturnMonthsWithAtLeastOneReceipt() {
    createReceipt(LocalDate.of(2026, 6, 10));
    createReceipt(LocalDate.of(2026, 7, 1));

    List<YearMonth> months = interactor.execute().months();

    assertTrue(months.contains(YearMonth.of(2026, 6)));
    assertTrue(months.contains(YearMonth.of(2026, 7)));
  }

  @Test
  @DisplayName("When a month has zero receipts, should not be present")
  void shouldNotReturnMonthWithoutReceipts() {
    createReceipt(LocalDate.of(2026, 6, 10));

    List<YearMonth> months = interactor.execute().months();

    assertEquals(List.of(YearMonth.of(2026, 6)), months);
  }

  @Test
  @DisplayName("When repository is empty, should return empty list")
  void shouldReturnEmptyListWhenRepositoryEmpty() {
    List<YearMonth> months = interactor.execute().months();

    assertTrue(months.isEmpty());
  }
}
