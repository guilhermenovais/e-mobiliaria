package com.guilherme.emobiliaria.receipt.ui.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReceiptFormControllerDateCalculationTest {

  @Test
  @DisplayName("Should calculate start/end for provided example")
  void shouldCalculateStartEndForProvidedExample() {
    ReceiptFormController.PeriodInterval period = ReceiptFormController.calculatePeriod(
        LocalDate.of(2026, 4, 8), LocalDate.of(2026, 1, 17));

    assertEquals(LocalDate.of(2026, 4, 17), period.start());
    assertEquals(LocalDate.of(2026, 5, 16), period.end());
  }

  @Test
  @DisplayName("Should roll to next month when today is after contract day")
  void shouldRollToNextMonthWhenTodayIsAfterContractDay() {
    ReceiptFormController.PeriodInterval period = ReceiptFormController.calculatePeriod(
        LocalDate.of(2026, 4, 20), LocalDate.of(2026, 1, 17));

    assertEquals(LocalDate.of(2026, 5, 17), period.start());
    assertEquals(LocalDate.of(2026, 6, 16), period.end());
  }

  @Test
  @DisplayName("Should skip months without target day and keep exact day")
  void shouldSkipMonthsWithoutTargetDayAndKeepExactDay() {
    ReceiptFormController.PeriodInterval period = ReceiptFormController.calculatePeriod(
        LocalDate.of(2026, 4, 1), LocalDate.of(2026, 1, 31));

    assertEquals(LocalDate.of(2026, 5, 31), period.start());
    assertEquals(LocalDate.of(2026, 6, 29), period.end());
  }
}
