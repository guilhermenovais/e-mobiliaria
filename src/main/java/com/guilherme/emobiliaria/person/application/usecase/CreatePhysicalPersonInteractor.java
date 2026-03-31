package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.CreatePhysicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class CreatePhysicalPersonInteractor {

  private final PhysicalPersonRepository physicalPersonRepository;
  private final AddressRepository addressRepository;

  @Inject
  public CreatePhysicalPersonInteractor(
      PhysicalPersonRepository physicalPersonRepository,
      AddressRepository addressRepository
  ) {
    this.physicalPersonRepository = physicalPersonRepository;
    this.addressRepository = addressRepository;
  }

  public CreatePhysicalPersonOutput execute(CreatePhysicalPersonInput input) {
    Address address = addressRepository.findById(input.addressId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    PhysicalPerson person = PhysicalPerson.create(
        input.name(),
        input.nationality(),
        input.civilState(),
        input.occupation(),
        input.cpf(),
        input.idCardNumber(),
        address
    );
    PhysicalPerson created = physicalPersonRepository.create(person);
    return new CreatePhysicalPersonOutput(created);
  }
}
