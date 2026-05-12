package com.guilherme.emobiliaria.receipt.application.output;

import java.time.LocalDate;
import java.util.List;

public record GetUnreceiptedDueDatesOutput(List<LocalDate> dueDates) {
}
