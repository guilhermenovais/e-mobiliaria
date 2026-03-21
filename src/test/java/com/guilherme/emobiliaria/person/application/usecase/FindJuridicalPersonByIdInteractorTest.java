package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreateJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.FindJuridicalPersonByIdInput;
import com.guilherme.emobiliaria.person.application.output.FindJuridicalPersonByIdOutput;
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
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindJuridicalPersonByIdInteractorTest {

  private FakeJuridicalPersonRepository juridicalPersonRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeAddressRepository addressRepository;
  private FindJuridicalPersonByIdInteractor interactor;

  @BeforeEach
  void setUp() {
    juridicalPersonRepository = new FakeJuridicalPersonRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new FindJuridicalPersonByIdInteractor(juridicalPersonRepository);
  }

  private Long createJuridicalPerson() {
    Long addressId = new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
    Long representativeId = new CreatePhysicalPersonInteractor(physicalPersonRepository,
        addressRepository)
        .execute(new CreatePhysicalPersonInput("João Silva", "Brasileiro", CivilState.SINGLE,
            "Engenheiro", "529.982.247-25", "MG-1234567", addressId))
        .physicalPerson().getId();
    return new CreateJuridicalPersonInteractor(juridicalPersonRepository, physicalPersonRepository,
        addressRepository)
        .execute(new CreateJuridicalPersonInput("Empresa Teste Ltda", "11222333000181",
            representativeId, addressId))
        .juridicalPerson().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return juridical person when it exists")
    void shouldReturnJuridicalPerson() {
      Long id = createJuridicalPerson();

      FindJuridicalPersonByIdOutput output = interactor.execute(new FindJuridicalPersonByIdInput(id));

      assertEquals(id, output.juridicalPerson().getId());
      assertEquals("Empresa Teste Ltda", output.juridicalPerson().getCorporateName());
    }

    @Test
    @DisplayName("When juridical person does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenJuridicalPersonNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new FindJuridicalPersonByIdInput(999L)));
      assertEquals(ErrorMessage.JuridicalPerson.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
