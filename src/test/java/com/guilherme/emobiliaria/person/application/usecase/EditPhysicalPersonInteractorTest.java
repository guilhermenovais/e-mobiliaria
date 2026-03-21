package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.EditPhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.EditPhysicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EditPhysicalPersonInteractorTest {

  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeAddressRepository addressRepository;
  private EditPhysicalPersonInteractor interactor;

  @BeforeEach
  void setUp() {
    physicalPersonRepository = new FakePhysicalPersonRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new EditPhysicalPersonInteractor(physicalPersonRepository, addressRepository);
  }

  private Long createAddress() {
    return new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
  }

  private Long createPhysicalPerson(Long addressId) {
    return new CreatePhysicalPersonInteractor(physicalPersonRepository, addressRepository)
        .execute(new CreatePhysicalPersonInput("João Silva", "Brasileiro", CivilState.SINGLE,
            "Engenheiro", "529.982.247-25", "MG-1234567", addressId))
        .physicalPerson().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should update physical person when person and address exist")
    void shouldUpdatePhysicalPerson() {
      Long addressId = createAddress();
      Long personId = createPhysicalPerson(addressId);

      EditPhysicalPersonOutput output = interactor.execute(
          new EditPhysicalPersonInput(personId, "Maria Souza", "Brasileira", CivilState.MARRIED,
              "Advogada", "529.982.247-25", "SP-9876543", addressId));

      assertEquals("Maria Souza", output.physicalPerson().getName());
      assertEquals(CivilState.MARRIED, output.physicalPerson().getCivilState());
    }

    @Test
    @DisplayName("When physical person does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenPhysicalPersonNotFound() {
      Long addressId = createAddress();

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(
              new EditPhysicalPersonInput(999L, "Maria Souza", "Brasileira", CivilState.SINGLE,
                  "Advogada", "529.982.247-25", "SP-9876543", addressId)));
      assertEquals(ErrorMessage.PhysicalPerson.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When address does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenAddressNotFound() {
      Long addressId = createAddress();
      Long personId = createPhysicalPerson(addressId);

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(
              new EditPhysicalPersonInput(personId, "Maria Souza", "Brasileira", CivilState.SINGLE,
                  "Advogada", "529.982.247-25", "SP-9876543", 999L)));
      assertEquals(ErrorMessage.Address.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
