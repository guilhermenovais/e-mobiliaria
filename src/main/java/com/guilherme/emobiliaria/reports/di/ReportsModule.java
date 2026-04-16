package com.guilherme.emobiliaria.reports.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.reports.domain.service.OccupationRateFileService;
import com.guilherme.emobiliaria.reports.domain.service.RentEvolutionFileService;
import com.guilherme.emobiliaria.reports.infrastructure.service.OccupationRateFileServiceImpl;
import com.guilherme.emobiliaria.reports.infrastructure.service.RentEvolutionFileServiceImpl;

public class ReportsModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(RentEvolutionFileService.class).to(RentEvolutionFileServiceImpl.class);
    bind(OccupationRateFileService.class).to(OccupationRateFileServiceImpl.class);
  }
}
