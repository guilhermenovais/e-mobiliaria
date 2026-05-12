package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.LoadPaymentReportInput;
import com.guilherme.emobiliaria.reports.application.output.LoadPaymentReportOutput;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRowStatus;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoadPaymentReportInteractorTest {

  private LoadPaymentReportInteractor interactor;
  private FakeReportRepository repository;

  @BeforeEach
  void setUp() {
    repository = new FakeReportRepository();
    interactor = new LoadPaymentReportInteractor(repository);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When repository returns rows, should wrap them in output")
    void shouldReturnRowsFromRepository() {
      PaymentReportRow row =
          new PaymentReportRow("Imóvel A", "João Silva", "12345678901", null, null, null, null,
              PaymentReportRowStatus.VACANT);
      repository.setPaymentReportRows(List.of(row));

      LoadPaymentReportOutput output =
          interactor.execute(new LoadPaymentReportInput(YearMonth.now()));

      assertNotNull(output);
      assertEquals(1, output.rows().size());
      assertEquals("Imóvel A", output.rows().get(0).propertyName());
    }

    @Test
    @DisplayName("When repository returns empty list, should return empty output")
    void shouldReturnEmptyOutputWhenNoRows() {
      LoadPaymentReportOutput output =
          interactor.execute(new LoadPaymentReportInput(YearMonth.now()));

      assertNotNull(output);
      assertTrue(output.rows().isEmpty());
    }

    @Test
    @DisplayName("When repository fails, should propagate exception")
    void shouldPropagateExceptionWhenRepositoryFails() {
      repository.failNext(
          () -> new PersistenceException(ErrorMessage.Report.LOAD_ERROR, new RuntimeException()));

      assertThrows(PersistenceException.class,
          () -> interactor.execute(new LoadPaymentReportInput(YearMonth.now())));
    }
  }
}
