package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.FindAddressByIdInput;
import com.guilherme.emobiliaria.person.application.output.FindAddressByIdOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class FindAddressByIdInteractor {

  private final AddressRepository addressRepository;

  public FindAddressByIdInteractor(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public FindAddressByIdOutput execute(FindAddressByIdInput input) {
    Address address = addressRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    return new FindAddressByIdOutput(address);
  }
}
