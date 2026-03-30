package com.guilherme.emobiliaria.config.application.usecase;

import com.guilherme.emobiliaria.config.application.input.SetConfigInput;
import com.guilherme.emobiliaria.config.application.output.SetConfigOutput;
import com.guilherme.emobiliaria.config.domain.repository.FakeConfigRepository;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SetConfigInteractorTest {

  private FakeConfigRepository configRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeJuridicalPersonRepository juridicalPersonRepository;
  private SetConfigInteractor interactor;

  @BeforeEach
  void setUp() {
    configRepository = new FakeConfigRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    juridicalPersonRepository = new FakeJuridicalPersonRepository();
    interactor = new SetConfigInteractor(configRepository, physicalPersonRepository,
        juridicalPersonRepository);
  }

  private Address sampleAddress() {
    return Address.restore(1L, "01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private PhysicalPerson samplePhysicalPerson() {
    return PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
        "529.982.247-25", "MG-1234567", sampleAddress());
  }

  private JuridicalPerson sampleJuridicalPerson(PhysicalPerson representative) {
    return JuridicalPerson.create("Empresa LTDA", "11.222.333/0001-81", representative,
        sampleAddress());
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When input has null defaultLandlordId, should clear defaultLandlord")
    void shouldClearDefaultLandlordWhenInputIdIsNull() {
      SetConfigOutput output = interactor.execute(new SetConfigInput(null, null));

      assertNull(output.config().getDefaultLandlord());
    }

    @Test
    @DisplayName("When input has a PHYSICAL type, should set a PhysicalPerson as defaultLandlord")
    void shouldSetPhysicalPersonAsDefaultLandlord() {
      PhysicalPerson person = physicalPersonRepository.create(samplePhysicalPerson());

      SetConfigOutput output = interactor.execute(new SetConfigInput(person.getId(), "PHYSICAL"));

      assertEquals(person.getId(), output.config().getDefaultLandlord().getId());
    }

    @Test
    @DisplayName("When input has a JURIDICAL type, should set a JuridicalPerson as defaultLandlord")
    void shouldSetJuridicalPersonAsDefaultLandlord() {
      PhysicalPerson representative = physicalPersonRepository.create(samplePhysicalPerson());
      JuridicalPerson person = juridicalPersonRepository.create(
          sampleJuridicalPerson(representative));

      SetConfigOutput output = interactor.execute(new SetConfigInput(person.getId(), "JURIDICAL"));

      assertEquals(person.getId(), output.config().getDefaultLandlord().getId());
    }

    @Test
    @DisplayName("When PHYSICAL person is not found, should throw BusinessException")
    void shouldThrowWhenPhysicalPersonNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new SetConfigInput(999L, "PHYSICAL")));

      assertEquals(ErrorMessage.PhysicalPerson.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When JURIDICAL person is not found, should throw BusinessException")
    void shouldThrowWhenJuridicalPersonNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new SetConfigInput(999L, "JURIDICAL")));

      assertEquals(ErrorMessage.JuridicalPerson.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
