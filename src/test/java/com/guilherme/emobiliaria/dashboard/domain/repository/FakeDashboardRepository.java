package com.guilherme.emobiliaria.dashboard.domain.repository;

import com.guilherme.emobiliaria.dashboard.domain.entity.DashboardData;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

import java.time.LocalDate;
import java.util.List;

public class FakeDashboardRepository extends FakeImplementation implements DashboardRepository {

  private DashboardData data = new DashboardData(0, 0, List.of(), List.of(), List.of(), List.of());

  public void setData(DashboardData data) {
    this.data = data;
  }

  @Override
  public DashboardData load(LocalDate today) {
    maybeFail();
    return data;
  }
}
