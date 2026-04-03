package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.property.application.input.CreatePropertyInput;
import com.guilherme.emobiliaria.property.application.output.CreatePropertyOutput;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.property.domain.repository.FakePropertyRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CreatePropertyInteractorTest {

  private FakePropertyRepository propertyRepository;
  private FakeAddressRepository addressRepository;
  private CreatePropertyInteractor interactor;

  @BeforeEach
  void setUp() {
    propertyRepository = new FakePropertyRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new CreatePropertyInteractor(propertyRepository, addressRepository);
  }

  private Long createAddress() {
    return new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
  }

  private CreatePropertyInput validInput(Long addressId) {
    return new CreatePropertyInput("Apartamento Centro", "Apartamento", Purpose.RESIDENTIAL, "1234567890", "0987654321", "1122334455", addressId);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When address exists, should create property with a non-null id")
    void shouldCreatePropertyWhenAddressExists() {
      Long addressId = createAddress();

      CreatePropertyOutput output = interactor.execute(validInput(addressId));

      assertNotNull(output.property().getId());
      assertEquals("Apartamento Centro", output.property().getName());
      assertEquals(Purpose.RESIDENTIAL, output.property().getPurpose());
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
