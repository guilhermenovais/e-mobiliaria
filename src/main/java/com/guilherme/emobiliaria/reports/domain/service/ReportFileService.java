package com.guilherme.emobiliaria.reports.domain.service;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;

public interface ReportFileService {
  byte[] generateRentEvolutionPdf(RentEvolutionData data);
  byte[] generateOccupationRatePdf(OccupationRateData data);
}
