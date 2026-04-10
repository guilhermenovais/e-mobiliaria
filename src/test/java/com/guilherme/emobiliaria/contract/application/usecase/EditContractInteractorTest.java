package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreateContractInput;
import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.EditContractInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.input.PersonReference.PersonType;
import com.guilherme.emobiliaria.contract.application.output.CreateContractOutput;
import com.guilherme.emobiliaria.contract.application.output.EditContractOutput;
import com.guilherme.emobiliaria.contract.domain.repository.FakeContractRepository;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.FakePropertyRepository;
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

class EditContractInteractorTest {

  private FakeContractRepository contractRepository;
  private FakePaymentAccountRepository paymentAccountRepository;
  private FakePropertyRepository propertyRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeJuridicalPersonRepository juridicalPersonRepository;
  private EditContractInteractor interactor;
  private CreateContractInteractor createInteractor;

  @BeforeEach
  void setUp() {
    contractRepository = new FakeContractRepository();
    paymentAccountRepository = new FakePaymentAccountRepository();
    propertyRepository = new FakePropertyRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    juridicalPersonRepository = new FakeJuridicalPersonRepository();
    interactor = new EditContractInteractor(contractRepository, paymentAccountRepository,
        propertyRepository, physicalPersonRepository, juridicalPersonRepository);
    createInteractor = new CreateContractInteractor(contractRepository, paymentAccountRepository,
        propertyRepository, physicalPersonRepository, juridicalPersonRepository);
  }

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Long createPaymentAccount() {
    return new CreatePaymentAccountInteractor(paymentAccountRepository)
        .execute(new CreatePaymentAccountInput("Banco do Brasil", "1234-5", "12345-6", null))
        .paymentAccount().getId();
  }

  private Long createProperty() {
    Property property = Property.create("Apto Centro", "Apartamento",
        "1234567890", "0987654321", "IPTU-001", validAddress());
    return propertyRepository.create(property).getId();
  }

  private Long createPhysicalPerson() {
    PhysicalPerson person = PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE,
        "Engenheiro", "529.982.247-25", "MG-1234567", validAddress());
    return physicalPersonRepository.create(person).getId();
  }

  private Long createContract(Long paymentAccountId, Long propertyId, Long personId) {
    PersonReference personRef = new PersonReference(personId, PersonType.PHYSICAL);
    CreateContractInput input = new CreateContractInput(LocalDate.of(2026, 1, 1),
        Period.ofMonths(12), 10, 150000, "Residencial", paymentAccountId, propertyId, personRef,
        List.of(personRef), List.of(), List.of());
    CreateContractOutput output = createInteractor.execute(input);
    return output.contract().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When contract and all dependencies exist, should update and return contract")
    void shouldUpdateContractWhenAllDependenciesExist() {
      Long paymentAccountId = createPaymentAccount();
      Long propertyId = createProperty();
      Long personId = createPhysicalPerson();
      Long contractId = createContract(paymentAccountId, propertyId, personId);
      PersonReference personRef = new PersonReference(personId, PersonType.PHYSICAL);
      EditContractInput input = new EditContractInput(contractId, LocalDate.of(2026, 6, 1),
          Period.ofYears(2), 15, 150000, "Residencial", paymentAccountId, propertyId, personRef,
          List.of(personRef), List.of(), List.of());

      EditContractOutput output = interactor.execute(input);

      assertEquals(contractId, output.contract().getId());
      assertEquals(LocalDate.of(2026, 6, 1), output.contract().getStartDate());
      assertEquals(Period.ofYears(2), output.contract().getDuration());
      assertEquals(15, output.contract().getPaymentDay());
    }

    @Test
    @DisplayName("When contract does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowBusinessExceptionWhenContractNotFound() {
      Long paymentAccountId = createPaymentAccount();
      Long propertyId = createProperty();
      Long personId = createPhysicalPerson();
      PersonReference personRef = new PersonReference(personId, PersonType.PHYSICAL);
      EditContractInput input = new EditContractInput(999L, LocalDate.of(2026, 1, 1),
          Period.ofMonths(12), 10, 150000, "Residencial", paymentAccountId, propertyId, personRef,
          List.of(personRef), List.of(), List.of());

      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(input));
      assertEquals(ErrorMessage.Contract.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When payment account does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenPaymentAccountNotFound() {
      Long paymentAccountId = createPaymentAccount();
      Long propertyId = createProperty();
      Long personId = createPhysicalPerson();
      Long contractId = createContract(paymentAccountId, propertyId, personId);
      PersonReference personRef = new PersonReference(personId, PersonType.PHYSICAL);
      EditContractInput input = new EditContractInput(contractId, LocalDate.of(2026, 1, 1),
          Period.ofMonths(12), 10, 150000, "Residencial", 999L, propertyId, personRef, List.of(personRef),
          List.of(), List.of());

      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(input));
      assertEquals(ErrorMessage.PaymentAccount.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When property does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenPropertyNotFound() {
      Long paymentAccountId = createPaymentAccount();
      Long propertyId = createProperty();
      Long personId = createPhysicalPerson();
      Long contractId = createContract(paymentAccountId, propertyId, personId);
      PersonReference personRef = new PersonReference(personId, PersonType.PHYSICAL);
      EditContractInput input = new EditContractInput(contractId, LocalDate.of(2026, 1, 1),
          Period.ofMonths(12), 10, 150000, "Residencial", paymentAccountId, 999L, personRef, List.of(personRef),
          List.of(), List.of());

      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(input));
      assertEquals(ErrorMessage.Property.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
