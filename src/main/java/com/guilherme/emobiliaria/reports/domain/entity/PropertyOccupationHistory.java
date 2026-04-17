package com.guilherme.emobiliaria.reports.domain.entity;

import java.time.YearMonth;
import java.util.List;

public record PropertyOccupationHistory(
    String propertyName,
    List<YearMonth> months,
    List<Boolean> occupied
) {}
