package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.EditAddressInput;
import com.guilherme.emobiliaria.person.application.output.EditAddressOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EditAddressInteractorTest {

  private FakeAddressRepository addressRepository;
  private EditAddressInteractor interactor;

  @BeforeEach
  void setUp() {
    addressRepository = new FakeAddressRepository();
    interactor = new EditAddressInteractor(addressRepository);
  }

  private Long createAddress() {
    return new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should update address and return updated fields")
    void shouldUpdateAddress() {
      Long id = createAddress();

      EditAddressOutput output = interactor.execute(
          new EditAddressInput(id, "20040020", "Avenida Rio Branco", "100", "Ap 5",
              "Centro", "Rio de Janeiro", BrazilianState.RJ));

      assertEquals("20040020", output.address().getCep());
      assertEquals("Avenida Rio Branco", output.address().getAddress());
      assertEquals("100", output.address().getNumber());
      assertEquals("Ap 5", output.address().getComplement());
      assertEquals("Centro", output.address().getNeighborhood());
      assertEquals("Rio de Janeiro", output.address().getCity());
      assertEquals(BrazilianState.RJ, output.address().getState());
    }

    @Test
    @DisplayName("When address does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenAddressNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(
              new EditAddressInput(999L, "01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
                  BrazilianState.SP)));
      assertEquals(ErrorMessage.Address.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
