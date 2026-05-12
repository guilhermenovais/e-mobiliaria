package com.guilherme.emobiliaria.reports.domain.service;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;

import java.time.YearMonth;
import java.util.List;

public interface ReportFileService {
  byte[] generateRentEvolutionPdf(RentEvolutionData data);

  byte[] generateOccupationRatePdf(OccupationRateData data);

  byte[] generatePaymentReportPdf(List<PaymentReportRow> rows, YearMonth month);
}
