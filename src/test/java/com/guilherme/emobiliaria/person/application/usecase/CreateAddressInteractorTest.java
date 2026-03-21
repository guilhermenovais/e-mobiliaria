package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.output.CreateAddressOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CreateAddressInteractorTest {

  private FakeAddressRepository addressRepository;
  private CreateAddressInteractor interactor;

  @BeforeEach
  void setUp() {
    addressRepository = new FakeAddressRepository();
    interactor = new CreateAddressInteractor(addressRepository);
  }

  private CreateAddressInput validInput() {
    return new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should create address and return it with a non-null id")
    void shouldCreateAddressWithId() {
      CreateAddressOutput output = interactor.execute(validInput());

      assertNotNull(output.address().getId());
      assertEquals("01001000", output.address().getCep());
      assertEquals("Praça da Sé", output.address().getAddress());
      assertEquals("1", output.address().getNumber());
      assertEquals("Sé", output.address().getNeighborhood());
      assertEquals("São Paulo", output.address().getCity());
      assertEquals(BrazilianState.SP, output.address().getState());
    }
  }
}
