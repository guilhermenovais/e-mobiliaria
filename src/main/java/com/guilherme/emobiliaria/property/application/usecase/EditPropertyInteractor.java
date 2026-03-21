package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.property.application.input.EditPropertyInput;
import com.guilherme.emobiliaria.property.application.output.EditPropertyOutput;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class EditPropertyInteractor {

  private final PropertyRepository propertyRepository;
  private final AddressRepository addressRepository;

  public EditPropertyInteractor(
      PropertyRepository propertyRepository,
      AddressRepository addressRepository
  ) {
    this.propertyRepository = propertyRepository;
    this.addressRepository = addressRepository;
  }

  public EditPropertyOutput execute(EditPropertyInput input) {
    Property property = propertyRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Property.NOT_FOUND));
    Address address = addressRepository.findById(input.addressId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    property.setName(input.name());
    property.setType(input.type());
    property.setPurpose(input.purpose());
    property.setRent(input.rent());
    property.setCemig(input.cemig());
    property.setCopasa(input.copasa());
    property.setIptu(input.iptu());
    property.setAddress(address);
    Property updated = propertyRepository.update(property);
    return new EditPropertyOutput(updated);
  }
}
