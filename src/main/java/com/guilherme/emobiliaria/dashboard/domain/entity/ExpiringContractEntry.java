package com.guilherme.emobiliaria.dashboard.domain.entity;

import java.time.LocalDate;

public record ExpiringContractEntry(
    LocalDate endDate,
    String propertyName,
    String tenantName,
    int daysLeft,
    UrgencyLevel urgency
) {
}
