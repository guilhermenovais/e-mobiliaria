package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.property.application.input.CreatePropertyInput;
import com.guilherme.emobiliaria.property.application.input.FindPropertyByIdInput;
import com.guilherme.emobiliaria.property.application.output.FindPropertyByIdOutput;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.property.domain.repository.FakePropertyRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FindPropertyByIdInteractorTest {

  private FakePropertyRepository propertyRepository;
  private FakeAddressRepository addressRepository;
  private FindPropertyByIdInteractor interactor;

  @BeforeEach
  void setUp() {
    propertyRepository = new FakePropertyRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new FindPropertyByIdInteractor(propertyRepository);
  }

  private Long createProperty() {
    Long addressId = new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
    return new CreatePropertyInteractor(propertyRepository, addressRepository)
        .execute(new CreatePropertyInput("Apartamento Centro", "Apartamento", Purpose.RESIDENTIAL, "1234567890", "0987654321", "1122334455", addressId))
        .property().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return the property when it exists")
    void shouldFindPropertyById() {
      Long id = createProperty();

      FindPropertyByIdOutput output = interactor.execute(new FindPropertyByIdInput(id));

      assertEquals(id, output.property().getId());
      assertEquals("Apartamento Centro", output.property().getName());
    }

    @Test
    @DisplayName("When property does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenPropertyNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new FindPropertyByIdInput(999L)));
      assertEquals(ErrorMessage.Property.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
