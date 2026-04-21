package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.reports.domain.entity.VacancyTableRow;

public class VacancyTableRowBean {

  private final VacancyTableRow row;
  private final String vacantLabel;
  private final String occupiedLabel;

  public VacancyTableRowBean(VacancyTableRow row) {
    this(row, "Vago", "Ocupado");
  }

  public VacancyTableRowBean(VacancyTableRow row, String vacantLabel, String occupiedLabel) {
    this.row = row;
    this.vacantLabel = vacantLabel;
    this.occupiedLabel = occupiedLabel;
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

  public boolean getIs_vacant() {
    return "Vago".equals(row.currentStatus());
  }

  public String getCurrent_status() {
    return getIs_vacant() ? vacantLabel : occupiedLabel;
  }
}
