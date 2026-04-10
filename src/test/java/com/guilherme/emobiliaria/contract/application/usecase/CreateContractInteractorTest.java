package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreateContractInput;
import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.input.PersonReference.PersonType;
import com.guilherme.emobiliaria.contract.application.output.CreateContractOutput;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreateContractInteractorTest {

  private FakeContractRepository contractRepository;
  private FakePaymentAccountRepository paymentAccountRepository;
  private FakePropertyRepository propertyRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeJuridicalPersonRepository juridicalPersonRepository;
  private CreateContractInteractor interactor;

  @BeforeEach
  void setUp() {
    contractRepository = new FakeContractRepository();
    paymentAccountRepository = new FakePaymentAccountRepository();
    propertyRepository = new FakePropertyRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    juridicalPersonRepository = new FakeJuridicalPersonRepository();
    interactor = new CreateContractInteractor(contractRepository, paymentAccountRepository,
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

  private CreateContractInput validInput(Long paymentAccountId, Long propertyId, Long personId) {
    PersonReference personRef = new PersonReference(personId, PersonType.PHYSICAL);
    return new CreateContractInput(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10,
        150000, "Residencial",
        paymentAccountId, propertyId, personRef, List.of(personRef), List.of(), List.of());
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When all dependencies exist, should create and return contract with id")
    void shouldCreateContractWhenAllDependenciesExist() {
      Long paymentAccountId = createPaymentAccount();
      Long propertyId = createProperty();
      Long personId = createPhysicalPerson();

      CreateContractOutput output = interactor.execute(validInput(paymentAccountId, propertyId, personId));

      assertNotNull(output.contract().getId());
      assertEquals(LocalDate.of(2026, 1, 1), output.contract().getStartDate());
      assertEquals(Period.ofMonths(12), output.contract().getDuration());
      assertEquals(10, output.contract().getPaymentDay());
    }

    @Test
    @DisplayName("When payment account does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenPaymentAccountNotFound() {
      Long propertyId = createProperty();
      Long personId = createPhysicalPerson();

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(validInput(999L, propertyId, personId)));
      assertEquals(ErrorMessage.PaymentAccount.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When property does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenPropertyNotFound() {
      Long paymentAccountId = createPaymentAccount();
      Long personId = createPhysicalPerson();

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(validInput(paymentAccountId, 999L, personId)));
      assertEquals(ErrorMessage.Property.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When landlord does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenLandlordNotFound() {
      Long paymentAccountId = createPaymentAccount();
      Long propertyId = createProperty();
      PersonReference missingPerson = new PersonReference(999L, PersonType.PHYSICAL);
      CreateContractInput input = new CreateContractInput(LocalDate.of(2026, 1, 1),
          Period.ofMonths(12), 10, 150000, "Residencial", paymentAccountId, propertyId, missingPerson,
          List.of(missingPerson), List.of(), List.of());

      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(input));
      assertEquals(ErrorMessage.PhysicalPerson.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When tenant does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenTenantNotFound() {
      Long paymentAccountId = createPaymentAccount();
      Long propertyId = createProperty();
      Long landlordId = createPhysicalPerson();
      PersonReference landlord = new PersonReference(landlordId, PersonType.PHYSICAL);
      PersonReference missingTenant = new PersonReference(999L, PersonType.PHYSICAL);
      CreateContractInput input = new CreateContractInput(LocalDate.of(2026, 1, 1),
          Period.ofMonths(12), 10, 150000, "Residencial", paymentAccountId, propertyId, landlord,
          List.of(missingTenant), List.of(), List.of());

      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(input));
      assertEquals(ErrorMessage.PhysicalPerson.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
