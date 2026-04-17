package com.guilherme.emobiliaria.inflation.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.inflation.domain.repository.InflationIndexRepository;
import com.guilherme.emobiliaria.inflation.infrastructure.repository.JdbcInflationIndexRepository;

public class InflationModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(InflationIndexRepository.class).to(JdbcInflationIndexRepository.class);
  }
}
