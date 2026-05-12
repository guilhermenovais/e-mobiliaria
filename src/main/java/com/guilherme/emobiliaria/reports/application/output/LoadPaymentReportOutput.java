package com.guilherme.emobiliaria.reports.application.output;

import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;

import java.util.List;

public record LoadPaymentReportOutput(List<PaymentReportRow> rows) {
}
