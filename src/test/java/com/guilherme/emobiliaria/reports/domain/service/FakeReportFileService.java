package com.guilherme.emobiliaria.reports.domain.service;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

public class FakeReportFileService extends FakeImplementation implements ReportFileService {

  private static final byte[] DUMMY_PDF = new byte[]{0x25, 0x50, 0x44, 0x46};

  @Override
  public byte[] generateRentEvolutionPdf(RentEvolutionData data) {
    maybeFail();
    return DUMMY_PDF;
  }

  @Override
  public byte[] generateOccupationRatePdf(OccupationRateData data) {
    maybeFail();
    return DUMMY_PDF;
  }
}
