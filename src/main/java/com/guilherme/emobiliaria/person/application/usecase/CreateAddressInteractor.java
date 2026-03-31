package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.output.CreateAddressOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import jakarta.inject.Inject;

public class CreateAddressInteractor {

  private final AddressRepository addressRepository;

  @Inject
  public CreateAddressInteractor(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public CreateAddressOutput execute(CreateAddressInput input) {
    Address address = Address.create(
        input.cep(),
        input.address(),
        input.number(),
        input.complement(),
        input.neighborhood(),
        input.city(),
        input.state()
    );
    Address created = addressRepository.create(address);
    return new CreateAddressOutput(created);
  }
}
