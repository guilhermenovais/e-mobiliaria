package com.guilherme.emobiliaria.reports.application.usecase;

import com.guilherme.emobiliaria.reports.application.input.GeneratePaymentReportPdfInput;
import com.guilherme.emobiliaria.reports.application.output.GeneratePaymentReportPdfOutput;
import com.guilherme.emobiliaria.reports.domain.repository.FakeReportRepository;
import com.guilherme.emobiliaria.reports.domain.service.FakeReportFileService;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.YearMonth;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GeneratePaymentReportPdfInteractorTest {

  private GeneratePaymentReportPdfInteractor interactor;
  private FakeReportRepository repository;
  private FakeReportFileService fileService;

  @BeforeEach
  void setUp() {
    repository = new FakeReportRepository();
    fileService = new FakeReportFileService();
    interactor = new GeneratePaymentReportPdfInteractor(repository, fileService);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When repository and service succeed, should return PDF bytes")
    void shouldReturnPdfBytesWhenSuccessful() {
      GeneratePaymentReportPdfOutput output =
          interactor.execute(new GeneratePaymentReportPdfInput(YearMonth.now()));

      assertNotNull(output);
      assertNotNull(output.pdfBytes());
    }

    @Test
    @DisplayName("When repository fails, should propagate exception")
    void shouldPropagateExceptionWhenRepositoryFails() {
      repository.failNext(
          () -> new PersistenceException(ErrorMessage.Report.LOAD_ERROR, new RuntimeException()));

      assertThrows(PersistenceException.class,
          () -> interactor.execute(new GeneratePaymentReportPdfInput(YearMonth.now())));
    }

    @Test
    @DisplayName("When file service fails, should propagate exception")
    void shouldPropagateExceptionWhenFileServiceFails() {
      fileService.failNext(() -> new RuntimeException("pdf generation error"));

      assertThrows(RuntimeException.class,
          () -> interactor.execute(new GeneratePaymentReportPdfInput(YearMonth.now())));
    }
  }
}
