package com.guilherme.emobiliaria.reports.domain.entity;

public record VacancyTableRow(String propertyName, int vacantMonths, int longestStreak,
                              String lastTenantEndMonth, String currentStatus) {
}
