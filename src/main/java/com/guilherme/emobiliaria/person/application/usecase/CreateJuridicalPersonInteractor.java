package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.CreateJuridicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class CreateJuridicalPersonInteractor {

  private final JuridicalPersonRepository juridicalPersonRepository;
  private final PhysicalPersonRepository physicalPersonRepository;
  private final AddressRepository addressRepository;

  @Inject
  public CreateJuridicalPersonInteractor(
      JuridicalPersonRepository juridicalPersonRepository,
      PhysicalPersonRepository physicalPersonRepository,
      AddressRepository addressRepository
  ) {
    this.juridicalPersonRepository = juridicalPersonRepository;
    this.physicalPersonRepository = physicalPersonRepository;
    this.addressRepository = addressRepository;
  }

  public CreateJuridicalPersonOutput execute(CreateJuridicalPersonInput input) {
    PhysicalPerson representative = physicalPersonRepository.findById(input.representativeId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PhysicalPerson.NOT_FOUND));
    Address address = addressRepository.findById(input.addressId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Address.NOT_FOUND));
    JuridicalPerson person = JuridicalPerson.create(
        input.corporateName(),
        input.cnpj(),
        representative,
        address
    );
    JuridicalPerson created = juridicalPersonRepository.create(person);
    return new CreateJuridicalPersonOutput(created);
  }
}
