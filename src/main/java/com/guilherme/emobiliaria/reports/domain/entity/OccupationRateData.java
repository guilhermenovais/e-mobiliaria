package com.guilherme.emobiliaria.reports.domain.entity;

import java.time.YearMonth;
import java.util.List;

public record OccupationRateData(List<YearMonth> months, List<Integer> occupiedCounts,
                                 int totalProperties,
                                 List<PropertyOccupationHistory> propertyHistories,
                                 double avgVacancyRate, int currentVacancyCount,
                                 int longestVacancyStreakMonths,
                                 String longestVacancyStreakPropertyName,
                                 List<VacancyTableRow> vacancyTableRows) {
}
