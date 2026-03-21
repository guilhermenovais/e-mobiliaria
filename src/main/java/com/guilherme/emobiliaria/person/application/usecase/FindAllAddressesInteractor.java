package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.FindAllAddressesInput;
import com.guilherme.emobiliaria.person.application.output.FindAllAddressesOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public class FindAllAddressesInteractor {

  private final AddressRepository addressRepository;

  public FindAllAddressesInteractor(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public FindAllAddressesOutput execute(FindAllAddressesInput input) {
    PagedResult<Address> result = addressRepository.findAll(input.pagination());
    return new FindAllAddressesOutput(result);
  }
}
