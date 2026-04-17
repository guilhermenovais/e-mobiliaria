package com.guilherme.emobiliaria.reports.domain.repository;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

import java.time.YearMonth;
import java.util.List;

public class FakeReportRepository extends FakeImplementation implements ReportRepository {

  private RentEvolutionData rentEvolutionData = new RentEvolutionData(
      List.of(YearMonth.of(2026, 1)),
      List.of(150000L),
      List.of()
  );

  private OccupationRateData occupationRateData = new OccupationRateData(
      List.of(YearMonth.of(2026, 1)),
      List.of(1),
      2,
      List.of()
  );

  public void setRentEvolutionData(RentEvolutionData data) {
    this.rentEvolutionData = data;
  }

  public void setOccupationRateData(OccupationRateData data) {
    this.occupationRateData = data;
  }

  @Override
  public RentEvolutionData loadRentEvolutionData() {
    maybeFail();
    return rentEvolutionData;
  }

  @Override
  public OccupationRateData loadOccupationRateData() {
    maybeFail();
    return occupationRateData;
  }
}
