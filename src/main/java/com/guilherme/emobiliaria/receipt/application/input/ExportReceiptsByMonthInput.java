package com.guilherme.emobiliaria.receipt.application.input;

import java.nio.file.Path;
import java.time.YearMonth;

public record ExportReceiptsByMonthInput(YearMonth month, Path destinationFolder) {
}
