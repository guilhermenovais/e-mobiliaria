package com.guilherme.emobiliaria.reports.domain.repository;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;

public interface ReportRepository {
  RentEvolutionData loadRentEvolutionData();
  OccupationRateData loadOccupationRateData();
}
