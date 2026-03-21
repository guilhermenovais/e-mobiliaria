package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.FindAddressByIdInput;
import com.guilherme.emobiliaria.person.application.output.FindAddressByIdOutput;
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

class FindAddressByIdInteractorTest {

  private FakeAddressRepository addressRepository;
  private FindAddressByIdInteractor interactor;

  @BeforeEach
  void setUp() {
    addressRepository = new FakeAddressRepository();
    interactor = new FindAddressByIdInteractor(addressRepository);
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
    @DisplayName("Should return address when it exists")
    void shouldReturnAddress() {
      Long id = createAddress();

      FindAddressByIdOutput output = interactor.execute(new FindAddressByIdInput(id));

      assertEquals(id, output.address().getId());
      assertEquals("01001000", output.address().getCep());
    }

    @Test
    @DisplayName("When address does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenAddressNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new FindAddressByIdInput(999L)));
      assertEquals(ErrorMessage.Address.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
