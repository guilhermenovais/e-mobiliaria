package com.guilherme.emobiliaria.property.application.usecase;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.property.application.input.CreatePropertyInput;
import com.guilherme.emobiliaria.property.application.output.CreatePropertyOutput;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class CreatePropertyInteractor {

  private final PropertyRepository propertyRepository;
  private final AddressRepository addressRepository;

  @Inject
  public CreatePropertyInteractor(
      PropertyRepository propertyRepository,
      AddressRepository addressRepository
  ) {
    this.propertyRepository = propertyRepository;
    this.addressRepository = addressRepository;
  }

  public CreatePropertyOutput execute(CreatePropertyInput input) {
    Address address = addressRepository.findById(input.addressId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    Property property = Property.create(
        input.name(),
        input.type(),
        input.cemig(),
        input.copasa(),
        input.iptu(),
        address
    );
    Property created = propertyRepository.create(property);
    return new CreatePropertyOutput(created);
  }
}
