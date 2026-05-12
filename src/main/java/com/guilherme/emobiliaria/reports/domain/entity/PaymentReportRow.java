package com.guilherme.emobiliaria.reports.domain.entity;

import java.time.LocalDate;

public record PaymentReportRow(String propertyName, String primaryTenantName,
                               String primaryTenantTaxId, LocalDate paymentDate, Integer rent,
                               LocalDate periodStart, LocalDate periodEnd,
                               PaymentReportRowStatus status) {
}
