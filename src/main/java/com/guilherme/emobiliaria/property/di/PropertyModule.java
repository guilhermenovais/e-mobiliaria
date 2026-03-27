package com.guilherme.emobiliaria.property.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.property.infrastructure.repository.JdbcPropertyRepository;

public class PropertyModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PropertyRepository.class).to(JdbcPropertyRepository.class);
  }
}
