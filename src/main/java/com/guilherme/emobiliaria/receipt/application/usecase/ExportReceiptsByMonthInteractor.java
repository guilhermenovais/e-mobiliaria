package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.receipt.application.input.ExportReceiptsByMonthInput;
import com.guilherme.emobiliaria.receipt.application.output.ExportReceiptsByMonthOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.entity.ReceiptExportResult;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptExportService;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptFileService;
import com.guilherme.emobiliaria.shared.exception.ExportFolderNotWritableException;
import jakarta.inject.Inject;

import java.util.ArrayList;
import java.util.List;

public class ExportReceiptsByMonthInteractor {

  private final ReceiptRepository receiptRepository;
  private final ReceiptFileService receiptFileService;
  private final ReceiptExportService receiptExportService;

  @Inject
  public ExportReceiptsByMonthInteractor(ReceiptRepository receiptRepository,
      ReceiptFileService receiptFileService, ReceiptExportService receiptExportService) {
    this.receiptRepository = receiptRepository;
    this.receiptFileService = receiptFileService;
    this.receiptExportService = receiptExportService;
  }

  public ExportReceiptsByMonthOutput execute(ExportReceiptsByMonthInput input) {
    List<Receipt> receipts = receiptRepository.findAllByMonth(input.month());

    int exportedCount = 0;
    List<ReceiptExportResult.FailedExport> failures = new ArrayList<>();

    for (Receipt receipt : receipts) {
      try {
        byte[] pdfBytes = receiptFileService.generateReceiptPdf(receipt);
        String fileName = receiptFileService.defaultFileName(receipt);
        receiptExportService.writePdf(input.destinationFolder(), fileName, pdfBytes);
        exportedCount++;
      } catch (ExportFolderNotWritableException e) {
        throw e;
      } catch (RuntimeException e) {
        failures.add(new ReceiptExportResult.FailedExport(receipt.getId(), e.getMessage()));
      }
    }

    return new ExportReceiptsByMonthOutput(new ReceiptExportResult(exportedCount, failures));
  }
}
