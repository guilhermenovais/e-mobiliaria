package com.guilherme.emobiliaria.dashboard.application.usecase;

import com.guilherme.emobiliaria.dashboard.application.input.GetDashboardDataInput;
import com.guilherme.emobiliaria.dashboard.application.output.GetDashboardDataOutput;
import com.guilherme.emobiliaria.dashboard.domain.repository.DashboardRepository;
import jakarta.inject.Inject;

public class GetDashboardDataInteractor {

  private final DashboardRepository dashboardRepository;

  @Inject
  public GetDashboardDataInteractor(DashboardRepository dashboardRepository) {
    this.dashboardRepository = dashboardRepository;
  }

  public GetDashboardDataOutput execute(GetDashboardDataInput input) {
    return new GetDashboardDataOutput(dashboardRepository.load(input.today()));
  }
}
