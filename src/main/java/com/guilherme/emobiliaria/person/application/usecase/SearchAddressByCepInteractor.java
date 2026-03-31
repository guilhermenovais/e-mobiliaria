package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.SearchAddressByCepInput;
import com.guilherme.emobiliaria.person.application.output.SearchAddressByCepOutput;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchResult;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class SearchAddressByCepInteractor {

  private final AddressSearchService addressSearchService;

  @Inject
  public SearchAddressByCepInteractor(AddressSearchService addressSearchService) {
    this.addressSearchService = addressSearchService;
  }

  public SearchAddressByCepOutput execute(SearchAddressByCepInput input) {
    AddressSearchResult result = addressSearchService.search(input.cep())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    return new SearchAddressByCepOutput(result);
  }
}
