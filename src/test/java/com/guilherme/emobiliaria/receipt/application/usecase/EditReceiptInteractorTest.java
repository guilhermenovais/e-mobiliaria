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
import com.guilherme.emobiliaria.receipt.application.input.EditReceiptInput;
import com.guilherme.emobiliaria.receipt.application.output.EditReceiptOutput;
import com.guilherme.emobiliaria.receipt.domain.repository.FakeReceiptRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EditReceiptInteractorTest {

  private FakeReceiptRepository receiptRepository;
  private FakeContractRepository contractRepository;
  private FakePaymentAccountRepository paymentAccountRepository;
  private FakePropertyRepository propertyRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private EditReceiptInteractor interactor;
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
    interactor = new EditReceiptInteractor(receiptRepository, contractRepository);
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

  private Long createReceipt(Long contractId) {
    return createReceiptInteractor.execute(new CreateReceiptInput(LocalDate.of(2026, 3, 1),
            LocalDate.of(2026, 3, 1), LocalDate.of(2026, 3, 31), 0, 0, null, contractId)).receipt()
        .getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When receipt and contract exist, should update and return the receipt")
    void shouldUpdateReceiptWhenAllDependenciesExist() {
      Long contractId = createContract();
      Long receiptId = createReceipt(contractId);

      EditReceiptOutput output = interactor.execute(new EditReceiptInput(receiptId,
          LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), 500, 100,
          null, contractId));

      assertEquals(receiptId, output.receipt().getId());
      assertEquals(LocalDate.of(2026, 4, 1), output.receipt().getDate());
      assertEquals(500, output.receipt().getDiscount());
      assertEquals(100, output.receipt().getFine());
    }

    @Test
    @DisplayName("When receipt does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenReceiptNotFound() {
      Long contractId = createContract();

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new EditReceiptInput(999L, LocalDate.of(2026, 4, 1),
              LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), 0, 0, null, contractId)));

      assertEquals(ErrorMessage.Receipt.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When contract does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenContractNotFound() {
      Long contractId = createContract();
      Long receiptId = createReceipt(contractId);

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new EditReceiptInput(receiptId, LocalDate.of(2026, 4, 1),
              LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 30), 0, 0, null, 999L)));

      assertEquals(ErrorMessage.Contract.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
