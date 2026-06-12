package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreateContractInput;
import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.input.PersonReference.PersonType;
import com.guilherme.emobiliaria.contract.application.usecase.CreateContractInteractor;
import com.guilherme.emobiliaria.contract.application.usecase.CreatePaymentAccountInteractor;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.FakeContractRepository;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.contract.domain.service.PaymentDueDateService;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.FakePropertyRepository;
import com.guilherme.emobiliaria.receipt.application.input.CreateReceiptInput;
import com.guilherme.emobiliaria.receipt.application.input.GetUnreceiptedDueDatesInput;
import com.guilherme.emobiliaria.receipt.application.output.GetUnreceiptedDueDatesOutput;
import com.guilherme.emobiliaria.receipt.domain.repository.FakeReceiptRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GetUnreceiptedDueDatesInteractorTest {

  private FakeReceiptRepository receiptRepository;
  private FakeContractRepository contractRepository;
  private GetUnreceiptedDueDatesInteractor interactor;
  private CreateReceiptInteractor createReceiptInteractor;
  private CreateContractInteractor createContractInteractor;

  @BeforeEach
  void setUp() {
    receiptRepository = new FakeReceiptRepository();
    contractRepository = new FakeContractRepository();
    FakePaymentAccountRepository paymentAccountRepository = new FakePaymentAccountRepository();
    FakePropertyRepository propertyRepository = new FakePropertyRepository();
    FakePhysicalPersonRepository physicalPersonRepository = new FakePhysicalPersonRepository();
    FakeJuridicalPersonRepository juridicalPersonRepository = new FakeJuridicalPersonRepository();
    PaymentDueDateService paymentDueDateService = new PaymentDueDateService();
    interactor = new GetUnreceiptedDueDatesInteractor(contractRepository, receiptRepository,
        paymentDueDateService);
    createReceiptInteractor = new CreateReceiptInteractor(receiptRepository, contractRepository);
    createContractInteractor =
        new CreateContractInteractor(contractRepository, paymentAccountRepository,
            propertyRepository, physicalPersonRepository, juridicalPersonRepository);
  }

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Long createContractWithPaymentDay(LocalDate startDate, int paymentDay) {
    Long paymentAccountId =
        new CreatePaymentAccountInteractor(new FakePaymentAccountRepository()).execute(
                new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", null))
            .paymentAccount().getId();
    FakePaymentAccountRepository accountRepo = new FakePaymentAccountRepository();
    Long accountId = accountRepo.create(
        com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount.create("Banco do Brasil",
            "1234-5", "12345-6", null)).getId();

    Property property =
        Property.create("Apto Centro", "Apartamento", "1234567890", "0987654321", "IPTU-001",
            validAddress());
    PhysicalPerson person =
        PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
            "529.982.247-25", "MG-1234567", validAddress());

    FakePropertyRepository propRepo = new FakePropertyRepository();
    FakePhysicalPersonRepository personRepo = new FakePhysicalPersonRepository();
    FakeJuridicalPersonRepository jurRepo = new FakeJuridicalPersonRepository();

    Long propertyId = propRepo.create(property).getId();
    Long personId = personRepo.create(person).getId();

    CreateContractInteractor localContractInteractor =
        new CreateContractInteractor(contractRepository, accountRepo, propRepo, personRepo,
            jurRepo);
    PersonReference ref = new PersonReference(personId, PersonType.PHYSICAL);
    return localContractInteractor.execute(
            new CreateContractInput(startDate, Period.ofMonths(120), paymentDay, 150000, "Residencial",
                accountId, propertyId, ref, List.of(ref), List.of(), List.of(), false)).contract()
        .getId();
  }

  private void createReceipt(Long contractId, LocalDate paymentDueDate) {
    createReceiptInteractor.execute(
        new CreateReceiptInput(LocalDate.of(2026, 1, 1), paymentDueDate, paymentDueDate,
            paymentDueDate.plusMonths(1).minusDays(1), 0, 0, null, contractId));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Contract with no receipts shows all due dates up to today's month")
    void shouldReturnAllDueDatesWhenNoReceipts() {
      // startDate=2026-01-10, paymentDay=15 → due dates from Jan 2026 onwards
      Long contractId = createContractWithPaymentDay(LocalDate.of(2026, 1, 10), 15);

      // today = 2026-03-15: should give Jan 15, Feb 15, Mar 15
      GetUnreceiptedDueDatesOutput output = interactor.execute(
          new GetUnreceiptedDueDatesInput(contractId, LocalDate.of(2026, 3, 15), null));

      assertEquals(
          List.of(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 15), LocalDate.of(2026, 3, 15)),
          output.dueDates());
    }

    @Test
    @DisplayName("Contract with some receipted dates shows only unreceipted dates")
    void shouldExcludeAlreadyReceiptedDates() {
      Long contractId = createContractWithPaymentDay(LocalDate.of(2026, 1, 10), 15);
      createReceipt(contractId, LocalDate.of(2026, 1, 15));
      createReceipt(contractId, LocalDate.of(2026, 2, 15));

      GetUnreceiptedDueDatesOutput output = interactor.execute(
          new GetUnreceiptedDueDatesInput(contractId, LocalDate.of(2026, 3, 15), null));

      assertEquals(List.of(LocalDate.of(2026, 3, 15)), output.dueDates());
    }

    @Test
    @DisplayName("All dates receipted returns empty list")
    void shouldReturnEmptyListWhenAllDatesReceipted() {
      Long contractId = createContractWithPaymentDay(LocalDate.of(2026, 1, 10), 15);
      createReceipt(contractId, LocalDate.of(2026, 1, 15));
      createReceipt(contractId, LocalDate.of(2026, 2, 15));
      createReceipt(contractId, LocalDate.of(2026, 3, 15));

      GetUnreceiptedDueDatesOutput output = interactor.execute(
          new GetUnreceiptedDueDatesInput(contractId, LocalDate.of(2026, 3, 15), null));

      assertTrue(output.dueDates().isEmpty());
    }

    @Test
    @DisplayName("Rescinded contract returns empty list")
    void shouldReturnEmptyListForRescindedContract() {
      Long contractId = createContractWithPaymentDay(LocalDate.of(2026, 1, 10), 15);
      Contract contract = contractRepository.findById(contractId).orElseThrow();
      contract.rescind(LocalDate.of(2026, 2, 1));
      contractRepository.update(contract);

      GetUnreceiptedDueDatesOutput output = interactor.execute(
          new GetUnreceiptedDueDatesInput(contractId, LocalDate.of(2026, 3, 15), null));

      assertTrue(output.dueDates().isEmpty());
    }

    @Test
    @DisplayName("excludeReceiptId re-includes that receipt's due date in the options (edit mode)")
    void shouldReincludeDueDateWhenExcludeReceiptIdProvided() {
      Long contractId = createContractWithPaymentDay(LocalDate.of(2026, 1, 10), 15);
      createReceipt(contractId, LocalDate.of(2026, 1, 15));
      Long receiptToEdit = createReceiptInteractor.execute(
              new CreateReceiptInput(LocalDate.of(2026, 2, 1), LocalDate.of(2026, 2, 15),
                  LocalDate.of(2026, 2, 15), LocalDate.of(2026, 3, 14), 0, 0, null, contractId))
          .receipt().getId();

      GetUnreceiptedDueDatesOutput output = interactor.execute(
          new GetUnreceiptedDueDatesInput(contractId, LocalDate.of(2026, 3, 15), receiptToEdit));

      // Jan 15 is taken (different receipt), Feb 15 is re-included (excludeReceiptId), Mar 15 is unreceipted
      assertEquals(List.of(LocalDate.of(2026, 2, 15), LocalDate.of(2026, 3, 15)),
          output.dueDates());
    }
  }
}
