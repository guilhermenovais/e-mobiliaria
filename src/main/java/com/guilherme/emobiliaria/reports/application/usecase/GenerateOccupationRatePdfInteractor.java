package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.GenerateOccupationRatePdfInput;
import com.guilherme.emobiliaria.reports.application.output.GenerateOccupationRatePdfOutput;
import com.guilherme.emobiliaria.reports.domain.service.OccupationRateFileService;
import jakarta.inject.Inject;

public class GenerateOccupationRatePdfInteractor {

  private final OccupationRateFileService occupationRateFileService;

  @Inject
  public GenerateOccupationRatePdfInteractor(OccupationRateFileService occupationRateFileService) {
    this.occupationRateFileService = occupationRateFileService;
  }

  public GenerateOccupationRatePdfOutput execute(GenerateOccupationRatePdfInput input) {
    return new GenerateOccupationRatePdfOutput(occupationRateFileService.generate());
  }
}
