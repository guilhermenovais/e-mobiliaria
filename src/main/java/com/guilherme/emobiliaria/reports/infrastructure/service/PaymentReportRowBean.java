package com.guilherme.emobiliaria.reports.infrastructure.service;

import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRowStatus;
import com.guilherme.emobiliaria.shared.util.MoneyFormatter;
import com.guilherme.emobiliaria.shared.util.TaxIdFormatter;

import java.time.format.DateTimeFormatter;

public class PaymentReportRowBean {

  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final String propertyName;
  private final String primaryTenantName;
  private final String primaryTenantTaxId;
  private final String paymentDate;
  private final String rent;
  private final String period;
  private final String statusLabel;

  public PaymentReportRowBean(PaymentReportRow row) {
    this.propertyName = row.propertyName();
    this.primaryTenantName = row.primaryTenantName() != null ? row.primaryTenantName() : "";
    this.primaryTenantTaxId = TaxIdFormatter.format(row.primaryTenantTaxId());
    this.paymentDate = row.paymentDate() != null ? row.paymentDate().format(DATE_FMT) : "";
    this.rent = row.rent() != null ? MoneyFormatter.formatWithSymbol(row.rent()) : "";
    this.period = (row.periodStart() != null && row.periodEnd() != null) ?
        row.periodStart().format(DATE_FMT) + " – " + row.periodEnd().format(DATE_FMT) :
        "";
    this.statusLabel = switch (row.status()) {
      case PaymentReportRowStatus.PAID -> "PAGO";
      case PaymentReportRowStatus.UNPAID -> "EM ABERTO";
      case PaymentReportRowStatus.VACANT -> "IMÓVEL VAGO";
    };
  }

  public String getPropertyName() {
    return propertyName;
  }

  public String getPrimaryTenantName() {
    return primaryTenantName;
  }

  public String getPrimaryTenantTaxId() {
    return primaryTenantTaxId;
  }

  public String getPaymentDate() {
    return paymentDate;
  }

  public String getRent() {
    return rent;
  }

  public String getPeriod() {
    return period;
  }

  public String getStatusLabel() {
    return statusLabel;
  }
}
