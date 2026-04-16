package com.guilherme.emobiliaria.dashboard.application.usecase;

import com.guilherme.emobiliaria.dashboard.application.input.GetDashboardDataInput;
import com.guilherme.emobiliaria.dashboard.application.output.GetDashboardDataOutput;
import com.guilherme.emobiliaria.dashboard.domain.entity.DashboardData;
import com.guilherme.emobiliaria.dashboard.domain.entity.TopRentEntry;
import com.guilherme.emobiliaria.dashboard.domain.repository.FakeDashboardRepository;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetDashboardDataInteractorTest {

  private FakeDashboardRepository repository;
  private GetDashboardDataInteractor interactor;

  @BeforeEach
  void setUp() {
    repository = new FakeDashboardRepository();
    interactor = new GetDashboardDataInteractor(repository);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When repository returns data, should return the same data in the output")
    void shouldReturnDataWhenRepositorySucceeds() {
      DashboardData data = new DashboardData(
          150000,
          3,
          List.of(new TopRentEntry(1, "Casa Serra", "Ana Rodrigues", 50000)),
          List.of(),
          List.of(),
          List.of()
      );
      repository.setData(data);

      GetDashboardDataOutput output = interactor.execute(new GetDashboardDataInput(LocalDate.now()));

      assertNotNull(output);
      assertEquals(data, output.data());
    }

    @Test
    @DisplayName("When repository returns empty data, should return empty data in the output")
    void shouldReturnEmptyDataWhenRepositoryReturnsEmpty() {
      GetDashboardDataOutput output = interactor.execute(new GetDashboardDataInput(LocalDate.now()));

      assertNotNull(output);
      assertEquals(0, output.data().totalRevenueCents());
      assertEquals(0, output.data().activeContractCount());
      assertEquals(0, output.data().topRents().size());
      assertEquals(0, output.data().unpaidRents().size());
      assertEquals(0, output.data().vacantProperties().size());
      assertEquals(0, output.data().expiringContracts().size());
    }

    @Test
    @DisplayName("When repository fails, should propagate the exception")
    void shouldPropagateExceptionWhenRepositoryFails() {
      repository.failNext(() -> new PersistenceException(
          com.guilherme.emobiliaria.shared.exception.ErrorMessage.Dashboard.LOAD_ERROR,
          new RuntimeException("DB error")));

      assertThrows(PersistenceException.class,
          () -> interactor.execute(new GetDashboardDataInput(LocalDate.now())));
    }
  }
}
