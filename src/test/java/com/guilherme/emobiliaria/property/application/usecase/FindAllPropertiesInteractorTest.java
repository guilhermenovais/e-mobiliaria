package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.property.application.input.CreatePropertyInput;
import com.guilherme.emobiliaria.property.application.input.FindAllPropertiesInput;
import com.guilherme.emobiliaria.property.application.output.FindAllPropertiesOutput;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.property.domain.repository.FakePropertyRepository;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindAllPropertiesInteractorTest {

  private FakePropertyRepository propertyRepository;
  private FakeAddressRepository addressRepository;
  private FindAllPropertiesInteractor interactor;

  @BeforeEach
  void setUp() {
    propertyRepository = new FakePropertyRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new FindAllPropertiesInteractor(propertyRepository);
  }

  private void createProperty(String name) {
    Long addressId = new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
    new CreatePropertyInteractor(propertyRepository, addressRepository)
        .execute(new CreatePropertyInput(name, "Apartamento", Purpose.RESIDENTIAL,
            150000, "1234567890", "0987654321", "1122334455", addressId));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return all properties with correct total")
    void shouldReturnAllProperties() {
      createProperty("Apartamento A");
      createProperty("Apartamento B");

      FindAllPropertiesOutput output = interactor.execute(
          new FindAllPropertiesInput(new PaginationInput(10, 0)));

      assertEquals(2, output.result().total());
      assertEquals(2, output.result().items().size());
    }

    @Test
    @DisplayName("Should return empty result when no properties exist")
    void shouldReturnEmptyWhenNoProperties() {
      FindAllPropertiesOutput output = interactor.execute(
          new FindAllPropertiesInput(new PaginationInput(10, 0)));

      assertEquals(0, output.result().total());
    }
  }
}
