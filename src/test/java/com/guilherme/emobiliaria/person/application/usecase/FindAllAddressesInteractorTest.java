package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.FindAllAddressesInput;
import com.guilherme.emobiliaria.person.application.output.FindAllAddressesOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindAllAddressesInteractorTest {

  private FakeAddressRepository addressRepository;
  private FindAllAddressesInteractor interactor;

  @BeforeEach
  void setUp() {
    addressRepository = new FakeAddressRepository();
    interactor = new FindAllAddressesInteractor(addressRepository);
  }

  private void createAddress() {
    new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return all addresses with correct total")
    void shouldReturnAllAddresses() {
      createAddress();
      createAddress();

      FindAllAddressesOutput output = interactor.execute(
          new FindAllAddressesInput(new PaginationInput(null, null)));

      assertEquals(2, output.result().total());
      assertEquals(2, output.result().items().size());
    }
  }
}
