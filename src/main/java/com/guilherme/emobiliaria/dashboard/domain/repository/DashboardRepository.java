package com.guilherme.emobiliaria.dashboard.domain.repository;

import com.guilherme.emobiliaria.dashboard.domain.entity.DashboardData;

import java.time.LocalDate;

public interface DashboardRepository {
  DashboardData load(LocalDate today);
}
