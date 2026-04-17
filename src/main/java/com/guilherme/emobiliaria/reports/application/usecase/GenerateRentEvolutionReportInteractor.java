package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.GenerateRentEvolutionReportInput;
import com.guilherme.emobiliaria.reports.application.output.GenerateRentEvolutionReportOutput;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.reports.domain.repository.ReportRepository;
import com.guilherme.emobiliaria.reports.domain.service.ReportFileService;
import jakarta.inject.Inject;

public class GenerateRentEvolutionReportInteractor {

  private final ReportRepository reportRepository;
  private final ReportFileService reportFileService;

  @Inject
  public GenerateRentEvolutionReportInteractor(ReportRepository reportRepository,
      ReportFileService reportFileService) {
    this.reportRepository = reportRepository;
    this.reportFileService = reportFileService;
  }

  public GenerateRentEvolutionReportOutput execute(GenerateRentEvolutionReportInput input) {
    RentEvolutionData data = reportRepository.loadRentEvolutionData();
    byte[] pdfBytes = reportFileService.generateRentEvolutionPdf(data);
    return new GenerateRentEvolutionReportOutput(pdfBytes);
  }
}
