package com.guilherme.emobiliaria.dashboard.domain.entity;

import java.util.List;

public record DashboardData(
    int totalRevenueCents,
    int activeContractCount,
    List<TopRentEntry> topRents,
    List<UnpaidRentEntry> unpaidRents,
    List<VacantPropertyEntry> vacantProperties,
    List<ExpiringContractEntry> expiringContracts
) {
}
