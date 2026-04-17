package com.guilherme.emobiliaria.reports.domain.entity;

import java.time.YearMonth;
import java.util.List;

public record PropertyRentHistory(
    String propertyName,
    List<YearMonth> months,
    List<Long> actualCents,
    List<Long> ipcaAdjustedCents,
    List<Long> igpmAdjustedCents
) {}
