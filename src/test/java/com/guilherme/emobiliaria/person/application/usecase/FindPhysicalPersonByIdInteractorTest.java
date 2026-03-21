package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.FindPhysicalPersonByIdInput;
import com.guilherme.emobiliaria.person.application.output.FindPhysicalPersonByIdOutput;
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

class FindPhysicalPersonByIdInteractorTest {

  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeAddressRepository addressRepository;
  private FindPhysicalPersonByIdInteractor interactor;

  @BeforeEach
  void setUp() {
    physicalPersonRepository = new FakePhysicalPersonRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new FindPhysicalPersonByIdInteractor(physicalPersonRepository);
  }

  private Long createPhysicalPerson() {
    Long addressId = new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
    return new CreatePhysicalPersonInteractor(physicalPersonRepository, addressRepository)
        .execute(new CreatePhysicalPersonInput("João Silva", "Brasileiro", CivilState.SINGLE,
            "Engenheiro", "529.982.247-25", "MG-1234567", addressId))
        .physicalPerson().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return physical person when it exists")
    void shouldReturnPhysicalPerson() {
      Long id = createPhysicalPerson();

      FindPhysicalPersonByIdOutput output = interactor.execute(new FindPhysicalPersonByIdInput(id));

      assertEquals(id, output.physicalPerson().getId());
      assertEquals("João Silva", output.physicalPerson().getName());
    }

    @Test
    @DisplayName("When physical person does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenPhysicalPersonNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new FindPhysicalPersonByIdInput(999L)));
      assertEquals(ErrorMessage.PhysicalPerson.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
