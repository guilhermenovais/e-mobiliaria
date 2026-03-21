package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.DeleteAddressInput;
import com.guilherme.emobiliaria.person.application.output.DeleteAddressOutput;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class DeleteAddressInteractor {

  private final AddressRepository addressRepository;

  public DeleteAddressInteractor(AddressRepository addressRepository) {
    this.addressRepository = addressRepository;
  }

  public DeleteAddressOutput execute(DeleteAddressInput input) {
    addressRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    addressRepository.delete(input.id());
    return new DeleteAddressOutput();
  }
}
