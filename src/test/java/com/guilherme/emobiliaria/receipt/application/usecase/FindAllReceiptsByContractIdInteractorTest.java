package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreateContractInput;
import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.input.PersonReference.PersonType;
import com.guilherme.emobiliaria.contract.application.usecase.CreateContractInteractor;
import com.guilherme.emobiliaria.contract.application.usecase.CreatePaymentAccountInteractor;
import com.guilherme.emobiliaria.contract.domain.repository.FakeContractRepository;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.property.domain.repository.FakePropertyRepository;
import com.guilherme.emobiliaria.receipt.application.input.CreateReceiptInput;
import com.guilherme.emobiliaria.receipt.application.input.FindAllReceiptsByContractIdInput;
import com.guilherme.emobiliaria.receipt.application.output.FindAllReceiptsByContractIdOutput;
import com.guilherme.emobiliaria.receipt.domain.repository.FakeReceiptRepository;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindAllReceiptsByContractIdInteractorTest {

  private FakeReceiptRepository receiptRepository;
  private FakeContractRepository contractRepository;
  private FakePaymentAccountRepository paymentAccountRepository;
  private FakePropertyRepository propertyRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FindAllReceiptsByContractIdInteractor interactor;
  private CreateReceiptInteractor createReceiptInteractor;
  private CreateContractInteractor createContractInteractor;

  @BeforeEach
  void setUp() {
    receiptRepository = new FakeReceiptRepository();
    contractRepository = new FakeContractRepository();
    paymentAccountRepository = new FakePaymentAccountRepository();
    propertyRepository = new FakePropertyRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    FakeJuridicalPersonRepository juridicalPersonRepository = new FakeJuridicalPersonRepository();
    interactor = new FindAllReceiptsByContractIdInteractor(receiptRepository);
    createReceiptInteractor = new CreateReceiptInteractor(receiptRepository, contractRepository);
    createContractInteractor = new CreateContractInteractor(contractRepository,
        paymentAccountRepository, propertyRepository, physicalPersonRepository,
        juridicalPersonRepository);
  }

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Long createContract() {
    Long paymentAccountId = new CreatePaymentAccountInteractor(paymentAccountRepository)
        .execute(new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", null))
        .paymentAccount().getId();
    Property property = Property.create("Apto Centro", "Apartamento", Purpose.RESIDENTIAL,
        "1234567890", "0987654321", "IPTU-001", validAddress());
    Long propertyId = propertyRepository.create(property).getId();
    PhysicalPerson person = PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE,
        "Engenheiro", "529.982.247-25", "MG-1234567", validAddress());
    Long personId = physicalPersonRepository.create(person).getId();
    PersonReference personRef = new PersonReference(personId, PersonType.PHYSICAL);
    return createContractInteractor.execute(new CreateContractInput(LocalDate.of(2026, 1, 1),
        Period.ofMonths(12), 10, 150000, paymentAccountId, propertyId, personRef,
        List.of(personRef))).contract().getId();
  }

  private void createReceipt(Long contractId, LocalDate date) {
    createReceiptInteractor.execute(
        new CreateReceiptInput(date, date, date.plusDays(29), 0, 0, null, contractId));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When receipts exist for the given contract, should return only those receipts")
    void shouldReturnOnlyReceiptsForGivenContract() {
      Long contractA = createContract();
      Long contractB = createContract();
      createReceipt(contractA, LocalDate.of(2026, 1, 1));
      createReceipt(contractA, LocalDate.of(2026, 2, 1));
      createReceipt(contractB, LocalDate.of(2026, 1, 1));

      FindAllReceiptsByContractIdOutput output = interactor.execute(
          new FindAllReceiptsByContractIdInput(contractA, new PaginationInput(null, null)));

      assertEquals(2, output.result().total());
      assertEquals(2, output.result().items().size());
    }

    @Test
    @DisplayName("When no receipts exist for the given contract, should return empty result")
    void shouldReturnEmptyWhenNoReceiptsForContract() {
      Long contractId = createContract();

      FindAllReceiptsByContractIdOutput output = interactor.execute(
          new FindAllReceiptsByContractIdInput(contractId, new PaginationInput(null, null)));

      assertEquals(0, output.result().total());
      assertEquals(0, output.result().items().size());
    }
  }
}
