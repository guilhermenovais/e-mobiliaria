package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.EditPhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.EditPhysicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class EditPhysicalPersonInteractor {

  private final PhysicalPersonRepository physicalPersonRepository;
  private final AddressRepository addressRepository;

  public EditPhysicalPersonInteractor(
      PhysicalPersonRepository physicalPersonRepository,
      AddressRepository addressRepository
  ) {
    this.physicalPersonRepository = physicalPersonRepository;
    this.addressRepository = addressRepository;
  }

  public EditPhysicalPersonOutput execute(EditPhysicalPersonInput input) {
    PhysicalPerson person = physicalPersonRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PhysicalPerson.NOT_FOUND));
    Address address = addressRepository.findById(input.addressId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    person.setName(input.name());
    person.setNationality(input.nationality());
    person.setCivilState(input.civilState());
    person.setOccupation(input.occupation());
    person.setCpf(input.cpf());
    person.setIdCardNumber(input.idCardNumber());
    person.setAddress(address);
    PhysicalPerson updated = physicalPersonRepository.update(person);
    return new EditPhysicalPersonOutput(updated);
  }
}
