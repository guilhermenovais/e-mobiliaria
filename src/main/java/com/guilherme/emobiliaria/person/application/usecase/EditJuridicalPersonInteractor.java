package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.EditJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.EditJuridicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

import java.util.List;

public class EditJuridicalPersonInteractor {

  private final JuridicalPersonRepository juridicalPersonRepository;
  private final PhysicalPersonRepository physicalPersonRepository;
  private final AddressRepository addressRepository;

  @Inject
  public EditJuridicalPersonInteractor(
      JuridicalPersonRepository juridicalPersonRepository,
      PhysicalPersonRepository physicalPersonRepository,
      AddressRepository addressRepository
  ) {
    this.juridicalPersonRepository = juridicalPersonRepository;
    this.physicalPersonRepository = physicalPersonRepository;
    this.addressRepository = addressRepository;
  }

  public EditJuridicalPersonOutput execute(EditJuridicalPersonInput input) {
    JuridicalPerson person = juridicalPersonRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.JuridicalPerson.NOT_FOUND));
    List<PhysicalPerson> representatives = input.representativeIds().stream()
        .map(id -> physicalPersonRepository.findById(id)
            .orElseThrow(() -> new BusinessException(ErrorMessage.PhysicalPerson.NOT_FOUND)))
        .toList();
    Address address = addressRepository.findById(input.addressId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    person.setCorporateName(input.corporateName());
    person.setCnpj(input.cnpj());
    person.setRepresentatives(representatives);
    person.setAddress(address);
    JuridicalPerson updated = juridicalPersonRepository.update(person);
    return new EditJuridicalPersonOutput(updated);
  }
}
