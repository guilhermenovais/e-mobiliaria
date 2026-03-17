package com.guilherme.emobiliaria.person.domain.service;

import java.util.Optional;

public interface AddressSearchService {

  Optional<AddressSearchResult> search(String cep);
}
