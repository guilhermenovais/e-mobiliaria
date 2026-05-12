package com.guilherme.emobiliaria.contract.domain.service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

public class PaymentDueDateService {

  public List<LocalDate> computeDueDates(LocalDate startDate, int paymentDay, LocalDate today) {
    int clampedDay = Math.min(paymentDay, startDate.lengthOfMonth());
    LocalDate candidate = startDate.withDayOfMonth(clampedDay);
    if (candidate.isBefore(startDate)) {
      YearMonth nextMonth = YearMonth.from(startDate).plusMonths(1);
      candidate = nextMonth.atDay(Math.min(paymentDay, nextMonth.lengthOfMonth()));
    }

    YearMonth todayMonth = YearMonth.from(today);
    List<LocalDate> result = new ArrayList<>();
    LocalDate current = candidate;
    while (!YearMonth.from(current).isAfter(todayMonth)) {
      result.add(current);
      YearMonth nextMonth = YearMonth.from(current).plusMonths(1);
      current = nextMonth.atDay(Math.min(paymentDay, nextMonth.lengthOfMonth()));
    }
    return result;
  }
}
