package com.guilherme.emobiliaria.person.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchService;
import com.guilherme.emobiliaria.person.infrastructure.repository.JdbcAddressRepository;
import com.guilherme.emobiliaria.person.infrastructure.repository.JdbcJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.infrastructure.repository.JdbcPhysicalPersonRepository;
import com.guilherme.emobiliaria.person.infrastructure.service.ViaCepAddressSearchService;

public class PersonModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(AddressRepository.class).to(JdbcAddressRepository.class);
    bind(PhysicalPersonRepository.class).to(JdbcPhysicalPersonRepository.class);
    bind(JuridicalPersonRepository.class).to(JdbcJuridicalPersonRepository.class);
    bind(AddressSearchService.class).to(ViaCepAddressSearchService.class);
  }
}
