package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.reports.domain.entity.VacancyTableRow;

public class VacancyTableRowBean {

  private final VacancyTableRow row;

  public VacancyTableRowBean(VacancyTableRow row) {
    this.row = row;
  }

  public String getProperty_name() {
    return row.propertyName();
  }

  public int getVacant_months() {
    return row.vacantMonths();
  }

  public int getLongest_streak() {
    return row.longestStreak();
  }

  public String getLast_tenant_end() {
    return row.lastTenantEndMonth();
  }

  public String getCurrent_status() {
    return row.currentStatus();
  }
}
