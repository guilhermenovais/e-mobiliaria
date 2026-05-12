package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.GetPaymentReportMonthsInput;
import com.guilherme.emobiliaria.reports.application.output.GetPaymentReportMonthsOutput;
import com.guilherme.emobiliaria.reports.domain.repository.FakeReportRepository;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GetPaymentReportMonthsInteractorTest {

  private GetPaymentReportMonthsInteractor interactor;
  private FakeReportRepository repository;

  @BeforeEach
  void setUp() {
    repository = new FakeReportRepository();
    interactor = new GetPaymentReportMonthsInteractor(repository);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When repository returns months, should wrap them in output")
    void shouldDelegateToRepositoryAndWrapOutput() {
      List<YearMonth> months = List.of(YearMonth.of(2026, 5), YearMonth.of(2026, 4));
      repository.setPaymentReportMonths(months);

      GetPaymentReportMonthsOutput output = interactor.execute(new GetPaymentReportMonthsInput());

      assertNotNull(output);
      assertEquals(2, output.months().size());
      assertEquals(YearMonth.of(2026, 5), output.months().get(0));
    }

    @Test
    @DisplayName("When repository returns current month only, should wrap it in output")
    void shouldReturnCurrentMonthForEmptyDatabase() {
      GetPaymentReportMonthsOutput output = interactor.execute(new GetPaymentReportMonthsInput());

      assertNotNull(output);
      assertEquals(1, output.months().size());
      assertEquals(YearMonth.now(), output.months().get(0));
    }

    @Test
    @DisplayName("When repository fails, should propagate exception")
    void shouldPropagateExceptionWhenRepositoryFails() {
      repository.failNext(
          () -> new PersistenceException(ErrorMessage.Report.LOAD_ERROR, new RuntimeException()));

      assertThrows(PersistenceException.class,
          () -> interactor.execute(new GetPaymentReportMonthsInput()));
    }
  }
}
