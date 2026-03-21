package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.property.application.input.CreatePropertyInput;
import com.guilherme.emobiliaria.property.application.input.EditPropertyInput;
import com.guilherme.emobiliaria.property.application.output.EditPropertyOutput;
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

class EditPropertyInteractorTest {

  private FakePropertyRepository propertyRepository;
  private FakeAddressRepository addressRepository;
  private EditPropertyInteractor interactor;

  @BeforeEach
  void setUp() {
    propertyRepository = new FakePropertyRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new EditPropertyInteractor(propertyRepository, addressRepository);
  }

  private Long createAddress() {
    return new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
  }

  private Long createProperty(Long addressId) {
    return new CreatePropertyInteractor(propertyRepository, addressRepository)
        .execute(new CreatePropertyInput("Apartamento Centro", "Apartamento", Purpose.RESIDENTIAL,
            150000, "1234567890", "0987654321", "1122334455", addressId))
        .property().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should update property and return updated values")
    void shouldEditProperty() {
      Long addressId = createAddress();
      Long propertyId = createProperty(addressId);

      EditPropertyOutput output = interactor.execute(new EditPropertyInput(
          propertyId, "Sala Comercial", "Sala", Purpose.COMMERCIAL,
          200000, "1111111111", "2222222222", "3333333333", addressId));

      assertEquals("Sala Comercial", output.property().getName());
      assertEquals(Purpose.COMMERCIAL, output.property().getPurpose());
    }

    @Test
    @DisplayName("When property does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenPropertyNotFound() {
      Long addressId = createAddress();

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new EditPropertyInput(
              999L, "Sala Comercial", "Sala", Purpose.COMMERCIAL,
              200000, "1111111111", "2222222222", "3333333333", addressId)));
      assertEquals(ErrorMessage.Property.NOT_FOUND, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When address does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenAddressNotFound() {
      Long addressId = createAddress();
      Long propertyId = createProperty(addressId);

      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new EditPropertyInput(
              propertyId, "Sala Comercial", "Sala", Purpose.COMMERCIAL,
              200000, "1111111111", "2222222222", "3333333333", 999L)));
      assertEquals(ErrorMessage.Address.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
