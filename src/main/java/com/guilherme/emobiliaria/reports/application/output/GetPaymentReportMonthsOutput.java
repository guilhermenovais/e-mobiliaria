package com.guilherme.emobiliaria.reports.application.output;

import java.time.YearMonth;
import java.util.List;

public record GetPaymentReportMonthsOutput(List<YearMonth> months) {
}
