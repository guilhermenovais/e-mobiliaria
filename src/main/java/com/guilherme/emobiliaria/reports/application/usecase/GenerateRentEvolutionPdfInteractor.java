package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.GenerateRentEvolutionPdfInput;
import com.guilherme.emobiliaria.reports.application.output.GenerateRentEvolutionPdfOutput;
import com.guilherme.emobiliaria.reports.domain.service.RentEvolutionFileService;
import jakarta.inject.Inject;

public class GenerateRentEvolutionPdfInteractor {

  private final RentEvolutionFileService rentEvolutionFileService;

  @Inject
  public GenerateRentEvolutionPdfInteractor(RentEvolutionFileService rentEvolutionFileService) {
    this.rentEvolutionFileService = rentEvolutionFileService;
  }

  public GenerateRentEvolutionPdfOutput execute(GenerateRentEvolutionPdfInput input) {
    return new GenerateRentEvolutionPdfOutput(rentEvolutionFileService.generate());
  }
}
