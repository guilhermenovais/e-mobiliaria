package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.LoadPaymentReportInput;
import com.guilherme.emobiliaria.reports.application.output.LoadPaymentReportOutput;
import com.guilherme.emobiliaria.reports.domain.repository.ReportRepository;
import jakarta.inject.Inject;

public class LoadPaymentReportInteractor {

  private final ReportRepository reportRepository;

  @Inject
  public LoadPaymentReportInteractor(ReportRepository reportRepository) {
    this.reportRepository = reportRepository;
  }

  public LoadPaymentReportOutput execute(LoadPaymentReportInput input) {
    return new LoadPaymentReportOutput(reportRepository.loadPaymentReportData(input.month()));
  }
}
