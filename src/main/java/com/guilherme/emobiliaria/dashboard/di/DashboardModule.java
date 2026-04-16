package com.guilherme.emobiliaria.dashboard.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.dashboard.domain.repository.DashboardRepository;
import com.guilherme.emobiliaria.dashboard.infrastructure.repository.JdbcDashboardRepository;

public class DashboardModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DashboardRepository.class).to(JdbcDashboardRepository.class);
  }
}
