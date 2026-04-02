package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.EditAddressInput;
import com.guilherme.emobiliaria.person.application.output.EditAddressOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class EditAddressInteractor {

  private final AddressRepository addressRepository;

  @Inject
  public EditAddressInteractor(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public EditAddressOutput execute(EditAddressInput input) {
    Address address = addressRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    address.setCep(input.cep());
    address.setAddress(input.address());
    address.setNumber(input.number());
    address.setComplement(input.complement());
    address.setNeighborhood(input.neighborhood());
    address.setCity(input.city());
    address.setState(input.state());
    Address updated = addressRepository.update(address);
    return new EditAddressOutput(updated);
  }
}
