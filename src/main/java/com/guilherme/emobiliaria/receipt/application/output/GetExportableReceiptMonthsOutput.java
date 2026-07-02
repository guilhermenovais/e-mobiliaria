package com.guilherme.emobiliaria.receipt.application.output;

import java.time.YearMonth;
import java.util.List;

public record GetExportableReceiptMonthsOutput(List<YearMonth> months) {
}
