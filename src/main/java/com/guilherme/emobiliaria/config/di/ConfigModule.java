package com.guilherme.emobiliaria.config.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.config.domain.repository.ConfigRepository;
import com.guilherme.emobiliaria.config.infrastructure.repository.JdbcConfigRepository;

public class ConfigModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ConfigRepository.class).to(JdbcConfigRepository.class);
  }
}
