package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.CreatePhysicalPersonOutput;
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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
  }
}
