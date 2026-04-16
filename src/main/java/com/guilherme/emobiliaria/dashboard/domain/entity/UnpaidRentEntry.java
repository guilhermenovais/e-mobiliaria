package com.guilherme.emobiliaria.dashboard.domain.entity;

import java.time.LocalDate;

public record UnpaidRentEntry(String propertyName, String tenantName, int rentCents, LocalDate dueDate) {
}
