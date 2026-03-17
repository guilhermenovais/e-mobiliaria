package com.guilherme.emobiliaria.person.domain.service;

import com.guilherme.emobiliaria.shared.fake.FakeImplementation;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class FakeAddressSearchService extends FakeImplementation implements AddressSearchService {
  private final Map<String, AddressSearchResult> results = new HashMap<>();

  public void register(String cep, AddressSearchResult result) {
    results.put(cep, result);
  }

  @Override
  public Optional<AddressSearchResult> search(String cep) {
    maybeFail();
    return Optional.ofNullable(results.get(cep));
  }
}
