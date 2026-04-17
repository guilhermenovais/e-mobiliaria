package com.guilherme.emobiliaria.reports.domain.entity;

import java.time.YearMonth;
import java.util.List;

public record RentEvolutionData(
    List<YearMonth> months,
    List<Long> monthlyTotalCents,
    List<PropertyRentHistory> propertyHistories
) {}
