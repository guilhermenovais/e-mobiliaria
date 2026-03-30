package com.guilherme.emobiliaria.config.application.usecase;

import com.guilherme.emobiliaria.config.application.input.SetConfigInput;
import com.guilherme.emobiliaria.config.application.output.SetConfigOutput;
import com.guilherme.emobiliaria.config.domain.repository.ConfigRepository;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

public class SetConfigInteractor {

  private final ConfigRepository configRepository;
  private final PhysicalPersonRepository physicalPersonRepository;
  private final JuridicalPersonRepository juridicalPersonRepository;

  @Inject
  public SetConfigInteractor(ConfigRepository configRepository,
      PhysicalPersonRepository physicalPersonRepository,
      JuridicalPersonRepository juridicalPersonRepository) {
    this.configRepository = configRepository;
    this.physicalPersonRepository = physicalPersonRepository;
    this.juridicalPersonRepository = juridicalPersonRepository;
  }

  public SetConfigOutput execute(SetConfigInput input) {
    var config = configRepository.get();

    if (input.defaultLandlordId() == null) {
      config.setDefaultLandlord(null);
    } else {
      Person landlord = resolvePerson(input.defaultLandlordId(), input.defaultLandlordType());
      config.setDefaultLandlord(landlord);
    }

    return new SetConfigOutput(configRepository.set(config));
  }

  private Person resolvePerson(Long id, String type) {
    if ("PHYSICAL".equals(type)) {
      return physicalPersonRepository.findById(id)
          .orElseThrow(() -> new BusinessException(ErrorMessage.PhysicalPerson.NOT_FOUND));
    }
    return juridicalPersonRepository.findById(id)
        .orElseThrow(() -> new BusinessException(ErrorMessage.JuridicalPerson.NOT_FOUND));
  }
}
