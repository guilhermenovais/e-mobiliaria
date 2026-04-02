package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.output.CreatePhysicalPersonOutput;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import jakarta.inject.Inject;

import java.sql.SQLException;

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
    try {
      PhysicalPerson created = physicalPersonRepository.create(person);
      return new CreatePhysicalPersonOutput(created);
    } catch (PersistenceException e) {
      if (isUniqueCpfViolation(e)) {
        PhysicalPerson existing = physicalPersonRepository.findByCpf(input.cpf())
            .orElseThrow(() -> e);
        return new CreatePhysicalPersonOutput(existing);
      }
      throw e;
    }
  }

  private boolean isUniqueCpfViolation(PersistenceException e) {
    Throwable original = e.getOriginalException();
    if (!(original instanceof SQLException sqlException)) {
      return false;
    }
    return "23505".equals(sqlException.getSQLState());
  }
}
