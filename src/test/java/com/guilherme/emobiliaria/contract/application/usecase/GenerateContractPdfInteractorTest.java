package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreateContractInput;
import com.guilherme.emobiliaria.contract.application.input.CreatePaymentAccountInput;
import com.guilherme.emobiliaria.contract.application.input.GenerateContractPdfInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.input.PersonReference.PersonType;
import com.guilherme.emobiliaria.contract.application.output.GenerateContractPdfOutput;
import com.guilherme.emobiliaria.contract.domain.repository.FakeContractRepository;
import com.guilherme.emobiliaria.contract.domain.repository.FakePaymentAccountRepository;
import com.guilherme.emobiliaria.contract.domain.service.FakeContractFileService;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
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

class GenerateContractPdfInteractorTest {

  private FakeContractRepository contractRepository;
  private FakePaymentAccountRepository paymentAccountRepository;
  private FakePropertyRepository propertyRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeContractFileService contractFileService;
  private GenerateContractPdfInteractor interactor;
  private CreateContractInteractor createInteractor;

  @BeforeEach
  void setUp() {
    contractRepository = new FakeContractRepository();
    paymentAccountRepository = new FakePaymentAccountRepository();
    propertyRepository = new FakePropertyRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    FakeJuridicalPersonRepository juridicalPersonRepository = new FakeJuridicalPersonRepository();
    contractFileService = new FakeContractFileService();
    interactor = new GenerateContractPdfInteractor(contractRepository, contractFileService);
    createInteractor = new CreateContractInteractor(contractRepository, paymentAccountRepository,
        propertyRepository, physicalPersonRepository, juridicalPersonRepository);
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
    CreateContractInput input = new CreateContractInput(LocalDate.of(2026, 1, 1),
        Period.ofMonths(12), 10, 150000, paymentAccountId, propertyId, personRef, List.of(personRef));
    return createInteractor.execute(input).contract().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When contract exists, should return non-empty pdf bytes")
    void shouldReturnPdfBytesWhenContractExists() {
      Long id = createContract();

      GenerateContractPdfOutput output = interactor.execute(new GenerateContractPdfInput(id));

      assertNotNull(output.pdfBytes());
      assertEquals(4, output.pdfBytes().length);
    }

    @Test
    @DisplayName("When contract does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowBusinessExceptionWhenContractNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new GenerateContractPdfInput(999L)));
      assertEquals(ErrorMessage.Contract.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
