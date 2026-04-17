package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.GenerateOccupationRateReportInput;
import com.guilherme.emobiliaria.reports.application.output.GenerateOccupationRateReportOutput;
import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.repository.ReportRepository;
import com.guilherme.emobiliaria.reports.domain.service.ReportFileService;
import jakarta.inject.Inject;

public class GenerateOccupationRateReportInteractor {

  private final ReportRepository reportRepository;
  private final ReportFileService reportFileService;

  @Inject
  public GenerateOccupationRateReportInteractor(ReportRepository reportRepository,
      ReportFileService reportFileService) {
    this.reportRepository = reportRepository;
    this.reportFileService = reportFileService;
  }

  public GenerateOccupationRateReportOutput execute(GenerateOccupationRateReportInput input) {
    OccupationRateData data = reportRepository.loadOccupationRateData();
    byte[] pdfBytes = reportFileService.generateOccupationRatePdf(data);
    return new GenerateOccupationRateReportOutput(pdfBytes);
  }
}
