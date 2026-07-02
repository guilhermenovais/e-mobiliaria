package com.guilherme.emobiliaria.receipt.domain.entity;

import java.util.List;

public record ReceiptExportResult(int exportedCount, List<FailedExport> failures) {
  public record FailedExport(Long receiptId, String reason) {
  }
}
