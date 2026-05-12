package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.GeneratePaymentReportPdfInput;
import com.guilherme.emobiliaria.reports.application.output.GeneratePaymentReportPdfOutput;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;
import com.guilherme.emobiliaria.reports.domain.repository.ReportRepository;
import com.guilherme.emobiliaria.reports.domain.service.ReportFileService;
import jakarta.inject.Inject;

import java.util.List;

public class GeneratePaymentReportPdfInteractor {

  private final ReportRepository reportRepository;
  private final ReportFileService reportFileService;

  @Inject
  public GeneratePaymentReportPdfInteractor(ReportRepository reportRepository,
      ReportFileService reportFileService) {
    this.reportRepository = reportRepository;
    this.reportFileService = reportFileService;
  }

  public GeneratePaymentReportPdfOutput execute(GeneratePaymentReportPdfInput input) {
    List<PaymentReportRow> rows = reportRepository.loadPaymentReportData(input.month());
    byte[] pdf = reportFileService.generatePaymentReportPdf(rows, input.month());
    return new GeneratePaymentReportPdfOutput(pdf);
  }
}
