package com.guilherme.emobiliaria.reports.domain.repository;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;

import java.time.YearMonth;
import java.util.List;

public interface ReportRepository {
  RentEvolutionData loadRentEvolutionData();

  OccupationRateData loadOccupationRateData();

  List<YearMonth> loadPaymentReportMonths();

  List<PaymentReportRow> loadPaymentReportData(YearMonth month);
}
