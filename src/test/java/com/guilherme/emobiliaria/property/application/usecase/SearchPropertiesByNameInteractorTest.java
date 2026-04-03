package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.property.application.input.CreatePropertyInput;
import com.guilherme.emobiliaria.property.application.input.SearchPropertiesByNameInput;
import com.guilherme.emobiliaria.property.application.output.SearchPropertiesByNameOutput;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.property.domain.repository.FakePropertyRepository;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SearchPropertiesByNameInteractorTest {

  private FakePropertyRepository propertyRepository;
  private FakeAddressRepository addressRepository;
  private SearchPropertiesByNameInteractor interactor;

  @BeforeEach
  void setUp() {
    propertyRepository = new FakePropertyRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new SearchPropertiesByNameInteractor(propertyRepository);
  }

  private void createProperty(String name) {
    Long addressId = new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
    new CreatePropertyInteractor(propertyRepository, addressRepository)
        .execute(new CreatePropertyInput(name, "Apartamento", Purpose.RESIDENTIAL, "1234567890", "0987654321", "1122334455", addressId));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return properties matching the query")
    void shouldReturnMatchingProperties() {
      createProperty("Apartamento Centro");
      createProperty("Casa Jardim");

      SearchPropertiesByNameOutput output = interactor.execute(
          new SearchPropertiesByNameInput("Apartamento", new PaginationInput(10, 0)));

      assertEquals(1, output.result().total());
      assertEquals("Apartamento Centro", output.result().items().getFirst().getName());
    }

    @Test
    @DisplayName("Should return empty result when no property matches the query")
    void shouldReturnEmptyWhenNoMatch() {
      createProperty("Apartamento Centro");

      SearchPropertiesByNameOutput output = interactor.execute(
          new SearchPropertiesByNameInput("Cobertura", new PaginationInput(10, 0)));

      assertEquals(0, output.result().total());
    }
  }
}
