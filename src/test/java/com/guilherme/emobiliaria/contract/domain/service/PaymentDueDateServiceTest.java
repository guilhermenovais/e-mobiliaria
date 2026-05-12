package com.guilherme.emobiliaria.contract.domain.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PaymentDueDateServiceTest {

  private final PaymentDueDateService service = new PaymentDueDateService();

  @Test
  @DisplayName("paymentDay=15, startDate=2026-01-10, today=2026-05-04 → 5 due dates through May 15")
  void shouldReturnDueDatesUpToCurrentMonth() {
    List<LocalDate> result =
        service.computeDueDates(LocalDate.of(2026, 1, 10), 15, LocalDate.of(2026, 5, 4));

    assertEquals(
        List.of(LocalDate.of(2026, 1, 15), LocalDate.of(2026, 2, 15), LocalDate.of(2026, 3, 15),
            LocalDate.of(2026, 4, 15), LocalDate.of(2026, 5, 15)), result);
  }

  @Test
  @DisplayName("paymentDay=5, startDate=2026-03-26, today=2026-05-04 → [2026-04-05, 2026-05-05]")
  void shouldSkipMarchBecauseStartDateIsAfterPaymentDay() {
    List<LocalDate> result =
        service.computeDueDates(LocalDate.of(2026, 3, 26), 5, LocalDate.of(2026, 5, 4));

    assertEquals(List.of(LocalDate.of(2026, 4, 5), LocalDate.of(2026, 5, 5)), result);
  }

  @Test
  @DisplayName("paymentDay=31 in February → clamped to 2026-02-28 (non-leap year)")
  void shouldClampDay31ToEndOfFebruary() {
    List<LocalDate> result =
        service.computeDueDates(LocalDate.of(2026, 2, 1), 31, LocalDate.of(2026, 2, 28));

    assertEquals(List.of(LocalDate.of(2026, 2, 28)), result);
  }

  @Test
  @DisplayName("startDate equals the payment day date → first due date is that same day")
  void shouldReturnStartDateAsFirstDueDateWhenStartDateEqualsPaymentDay() {
    List<LocalDate> result =
        service.computeDueDates(LocalDate.of(2026, 3, 15), 15, LocalDate.of(2026, 3, 15));

    assertEquals(List.of(LocalDate.of(2026, 3, 15)), result);
  }

  @Test
  @DisplayName("today before firstDueDate month → empty list")
  void shouldReturnEmptyListWhenTodayIsBeforeFirstDueDateMonth() {
    List<LocalDate> result =
        service.computeDueDates(LocalDate.of(2026, 3, 20), 15, LocalDate.of(2026, 3, 14));

    assertTrue(result.isEmpty());
  }
}
