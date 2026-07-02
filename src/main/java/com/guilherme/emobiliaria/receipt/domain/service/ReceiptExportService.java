package com.guilherme.emobiliaria.receipt.domain.service;

import com.guilherme.emobiliaria.shared.exception.ExportFolderNotWritableException;

import java.nio.file.Path;

public interface ReceiptExportService {
  void writePdf(Path folder, String fileName, byte[] pdfBytes)
      throws ExportFolderNotWritableException;
}
