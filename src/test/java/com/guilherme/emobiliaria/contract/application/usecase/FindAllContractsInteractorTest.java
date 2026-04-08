package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreateContractInput;
import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.FindAllContractsInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.input.PersonReference.PersonType;
import com.guilherme.emobiliaria.contract.application.output.FindAllContractsOutput;
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
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindAllContractsInteractorTest {

  private FakeContractRepository contractRepository;
  private FakePaymentAccountRepository paymentAccountRepository;
  private FakePropertyRepository propertyRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FindAllContractsInteractor interactor;
  private CreateContractInteractor createInteractor;

  @BeforeEach
  void setUp() {
    contractRepository = new FakeContractRepository();
    paymentAccountRepository = new FakePaymentAccountRepository();
    propertyRepository = new FakePropertyRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    FakeJuridicalPersonRepository juridicalPersonRepository = new FakeJuridicalPersonRepository();
    interactor = new FindAllContractsInteractor(contractRepository);
    createInteractor = new CreateContractInteractor(contractRepository, paymentAccountRepository,
        propertyRepository, physicalPersonRepository, juridicalPersonRepository);
  }

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private void createContract(LocalDate startDate) {
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
    createInteractor.execute(new CreateContractInput(startDate, Period.ofMonths(12), 10,
        150000,
        paymentAccountId, propertyId, personRef, List.of(personRef), List.of(), List.of()));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When contracts exist, should return all contracts with correct total")
    void shouldReturnAllContracts() {
      createContract(LocalDate.of(2026, 1, 1));
      createContract(LocalDate.of(2026, 3, 1));
      FindAllContractsInput input = new FindAllContractsInput(new PaginationInput(null, null));

      FindAllContractsOutput output = interactor.execute(input);

      assertEquals(2, output.result().total());
      assertEquals(2, output.result().items().size());
    }

    @Test
    @DisplayName("When no contracts exist, should return empty result")
    void shouldReturnEmptyWhenNoContracts() {
      FindAllContractsInput input = new FindAllContractsInput(new PaginationInput(null, null));

      FindAllContractsOutput output = interactor.execute(input);

      assertEquals(0, output.result().total());
      assertEquals(0, output.result().items().size());
    }

    @Test
    @DisplayName("When pagination is applied, should return limited contracts")
    void shouldReturnLimitedContractsWhenPaginationIsApplied() {
      createContract(LocalDate.of(2026, 1, 1));
      createContract(LocalDate.of(2026, 3, 1));
      createContract(LocalDate.of(2026, 6, 1));
      FindAllContractsInput input = new FindAllContractsInput(new PaginationInput(2, 0));

      FindAllContractsOutput output = interactor.execute(input);

      assertEquals(3, output.result().total());
      assertEquals(2, output.result().items().size());
    }
  }
}
