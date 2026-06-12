package com.guilherme.emobiliaria.receipt.ui.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptFormControllerDateCalculationTest {

  @Test
  @DisplayName("Should calculate start/end for provided example")
  void shouldCalculateStartEndForProvidedExample() {
    ReceiptFormController.PeriodInterval period =
        ReceiptFormController.calculatePeriod(LocalDate.of(2026, 4, 8), LocalDate.of(2026, 1, 17),
            false);

    assertEquals(LocalDate.of(2026, 4, 17), period.start());
    assertEquals(LocalDate.of(2026, 5, 16), period.end());
  }

  @Test
  @DisplayName("Should roll to next month when today is after contract day")
  void shouldRollToNextMonthWhenTodayIsAfterContractDay() {
    ReceiptFormController.PeriodInterval period =
        ReceiptFormController.calculatePeriod(LocalDate.of(2026, 4, 20), LocalDate.of(2026, 1, 17),
            false);

    assertEquals(LocalDate.of(2026, 5, 17), period.start());
    assertEquals(LocalDate.of(2026, 6, 16), period.end());
  }

  @Test
  @DisplayName(
      "Period start is next contract day after payment due date, end is start + 1 month - 1 day")
  void shouldCalculateStartEndBasedOnPaymentDueDate() {
    // Contract started 03/15/2026, payment due date 05/20/2026
    // → period start = 06/15/2026, period end = 07/14/2026
    ReceiptFormController.PeriodInterval period =
        ReceiptFormController.calculatePeriod(LocalDate.of(2026, 5, 20), LocalDate.of(2026, 3, 15),
            false);

    assertEquals(LocalDate.of(2026, 6, 15), period.start());
    assertEquals(LocalDate.of(2026, 7, 14), period.end());
  }

  @Test
  @DisplayName("Should skip months without target day and keep exact day")
  void shouldSkipMonthsWithoutTargetDayAndKeepExactDay() {
    ReceiptFormController.PeriodInterval period =
        ReceiptFormController.calculatePeriod(LocalDate.of(2026, 4, 1), LocalDate.of(2026, 1, 31),
            false);

    assertEquals(LocalDate.of(2026, 5, 31), period.start());
    assertEquals(LocalDate.of(2026, 6, 29), period.end());
  }

  @Test
  @DisplayName(
      "When delayed payment, period start is contract day before payment due date in the same month")
  void shouldCalculateStartEndBeforePaymentDueDateWhenDelayedPayment() {
    // Contract day 15, payment due date 05/20/2026
    // → period start = 05/15/2026, period end = 06/14/2026
    ReceiptFormController.PeriodInterval period =
        ReceiptFormController.calculatePeriod(LocalDate.of(2026, 5, 20), LocalDate.of(2026, 3, 15),
            true);

    assertEquals(LocalDate.of(2026, 5, 15), period.start());
    assertEquals(LocalDate.of(2026, 6, 14), period.end());
  }

  @Test
  @DisplayName(
      "When delayed payment and due date equals contract day, period start rolls back a month")
  void shouldRollBackToPreviousMonthWhenDelayedPaymentAndDueDateEqualsContractDay() {
    // Contract day 15, payment due date 05/15/2026
    // → period start = 04/15/2026, period end = 05/14/2026
    ReceiptFormController.PeriodInterval period =
        ReceiptFormController.calculatePeriod(LocalDate.of(2026, 5, 15), LocalDate.of(2026, 3, 15),
            true);

    assertEquals(LocalDate.of(2026, 4, 15), period.start());
    assertEquals(LocalDate.of(2026, 5, 14), period.end());
  }

  @Test
  @DisplayName("When delayed payment, should skip months without target day going backwards")
  void shouldSkipMonthsWithoutTargetDayGoingBackwardsWhenDelayedPayment() {
    // Contract day 31, payment due date 03/15/2026 → February has no 31st,
    // so period start rolls back to 01/31/2026, period end = 02/27/2026
    ReceiptFormController.PeriodInterval period =
        ReceiptFormController.calculatePeriod(LocalDate.of(2026, 3, 15), LocalDate.of(2026, 1, 31),
            true);

    assertEquals(LocalDate.of(2026, 1, 31), period.start());
    assertEquals(LocalDate.of(2026, 2, 27), period.end());
  }
}
