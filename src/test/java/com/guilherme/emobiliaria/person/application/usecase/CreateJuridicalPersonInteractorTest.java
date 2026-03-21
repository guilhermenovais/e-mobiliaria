package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreateJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.CreateJuridicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
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

class CreateJuridicalPersonInteractorTest {

  private FakeJuridicalPersonRepository juridicalPersonRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeAddressRepository addressRepository;
  private CreateJuridicalPersonInteractor interactor;

  @BeforeEach
  void setUp() {
    juridicalPersonRepository = new FakeJuridicalPersonRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new CreateJuridicalPersonInteractor(juridicalPersonRepository,
        physicalPersonRepository, addressRepository);
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
    @DisplayName("When representative and address exist, should create juridical person with a non-null id")
    void shouldCreateJuridicalPersonWhenDependenciesExist() {
      Long addressId = createAddress();
      Long representativeId = createPhysicalPerson(addressId);

      CreateJuridicalPersonOutput output = interactor.execute(
          new CreateJuridicalPersonInput("Empresa Teste Ltda", "11222333000181", representativeId,
              addressId));

      assertNotNull(output.juridicalPerson().getId());
      assertEquals("Empresa Teste Ltda", output.juridicalPerson().getCorporateName());
    }

    @Test
    @DisplayName("When representative does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenRepresentativeNotFound() {
      Long addressId = createAddress();

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(
              new CreateJuridicalPersonInput("Empresa Teste Ltda", "11222333000181", 999L,
                  addressId)));
      assertEquals(ErrorMessage.PhysicalPerson.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When address does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenAddressNotFound() {
      Long addressId = createAddress();
      Long representativeId = createPhysicalPerson(addressId);

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(
              new CreateJuridicalPersonInput("Empresa Teste Ltda", "11222333000181", representativeId,
                  999L)));
      assertEquals(ErrorMessage.Address.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
