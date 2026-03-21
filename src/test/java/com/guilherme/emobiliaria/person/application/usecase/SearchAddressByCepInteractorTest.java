package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.SearchAddressByCepInput;
import com.guilherme.emobiliaria.person.application.output.SearchAddressByCepOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchResult;
import com.guilherme.emobiliaria.person.domain.service.FakeAddressSearchService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SearchAddressByCepInteractorTest {

  private FakeAddressSearchService addressSearchService;
  private SearchAddressByCepInteractor interactor;

  @BeforeEach
  void setUp() {
    addressSearchService = new FakeAddressSearchService();
    interactor = new SearchAddressByCepInteractor(addressSearchService);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return result when CEP is registered")
    void shouldReturnResultWhenCepIsRegistered() {
      AddressSearchResult expected = new AddressSearchResult(BrazilianState.SP, "São Paulo", "Sé",
          "Praça da Sé");
      addressSearchService.register("01001000", expected);

      SearchAddressByCepOutput output = interactor.execute(new SearchAddressByCepInput("01001000"));

      assertEquals(BrazilianState.SP, output.result().state());
      assertEquals("São Paulo", output.result().city());
      assertEquals("Sé", output.result().neighborhood());
      assertEquals("Praça da Sé", output.result().address());
    }

    @Test
    @DisplayName("When CEP is not registered, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenCepNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new SearchAddressByCepInput("99999999")));
      assertEquals(ErrorMessage.Address.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
