package com.guilherme.emobiliaria.reports.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.reports.domain.repository.ReportRepository;
import com.guilherme.emobiliaria.reports.domain.service.ReportFileService;
import com.guilherme.emobiliaria.reports.infrastructure.repository.JdbcReportRepository;
import com.guilherme.emobiliaria.reports.infrastructure.service.ReportFileServiceImpl;

public class ReportsModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ReportRepository.class).to(JdbcReportRepository.class);
    bind(ReportFileService.class).to(ReportFileServiceImpl.class);
  }
}
