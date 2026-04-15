package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.CreatePhysicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.entity.PersonFilter;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CreatePhysicalPersonInteractorTest {

  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeAddressRepository addressRepository;
  private CreatePhysicalPersonInteractor interactor;

  @BeforeEach
  void setUp() {
    physicalPersonRepository = new FakePhysicalPersonRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new CreatePhysicalPersonInteractor(physicalPersonRepository, addressRepository);
  }

  private Long createAddress() {
    return new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
  }

  private CreatePhysicalPersonInput validInput(Long addressId) {
    return new CreatePhysicalPersonInput("João Silva", "Brasileiro", CivilState.SINGLE,
        "Engenheiro", "529.982.247-25", "MG-1234567", addressId);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When address exists, should create physical person with a non-null id")
    void shouldCreatePhysicalPersonWhenAddressExists() {
      Long addressId = createAddress();

      CreatePhysicalPersonOutput output = interactor.execute(validInput(addressId));

      assertNotNull(output.physicalPerson().getId());
      assertEquals("João Silva", output.physicalPerson().getName());
      assertEquals("52998224725", output.physicalPerson().getCpf());
    }

    @Test
    @DisplayName("When address does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenAddressNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(validInput(999L)));
      assertEquals(ErrorMessage.Address.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When CPF already exists, should return existing physical person instead of creating duplicate")
    void shouldReuseExistingPhysicalPersonWhenCpfAlreadyExists() {
      Long firstAddressId = createAddress();
      CreatePhysicalPersonOutput first = interactor.execute(validInput(firstAddressId));
      Long firstId = first.physicalPerson().getId();

      Long secondAddressId = createAddress();
      physicalPersonRepository.failNext(
          () -> new PersistenceException(
              ErrorMessage.PhysicalPerson.NOT_FOUND,
              new SQLException("Unique index violation", "23505")));
      CreatePhysicalPersonOutput second = interactor.execute(validInput(secondAddressId));

      assertEquals(firstId, second.physicalPerson().getId());
      assertTrue(
          physicalPersonRepository.findAll(new com.guilherme.emobiliaria.shared.persistence.PaginationInput(100, 0), PersonFilter.NONE)
              .items().size() == 1);
    }
  }
}
