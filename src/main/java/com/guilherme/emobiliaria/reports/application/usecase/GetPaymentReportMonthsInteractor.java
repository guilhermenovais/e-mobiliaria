package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.GetPaymentReportMonthsInput;
import com.guilherme.emobiliaria.reports.application.output.GetPaymentReportMonthsOutput;
import com.guilherme.emobiliaria.reports.domain.repository.ReportRepository;
import jakarta.inject.Inject;

public class GetPaymentReportMonthsInteractor {

  private final ReportRepository reportRepository;

  @Inject
  public GetPaymentReportMonthsInteractor(ReportRepository reportRepository) {
    this.reportRepository = reportRepository;
  }

  public GetPaymentReportMonthsOutput execute(GetPaymentReportMonthsInput input) {
    return new GetPaymentReportMonthsOutput(reportRepository.loadPaymentReportMonths());
  }
}
