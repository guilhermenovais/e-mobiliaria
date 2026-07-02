package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.guilherme.emobiliaria.receipt.domain.service.ReceiptExportService;
import com.guilherme.emobiliaria.shared.exception.ExportFolderNotWritableException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

public class FileSystemReceiptExportService implements ReceiptExportService {

  @Override
  public void writePdf(Path folder, String fileName, byte[] pdfBytes) {
    if (!Files.isWritable(folder)) {
      throw new ExportFolderNotWritableException();
    }
    try {
      Files.write(folder.resolve(fileName), pdfBytes, StandardOpenOption.CREATE,
          StandardOpenOption.TRUNCATE_EXISTING);
    } catch (IOException e) {
      throw new ExportFolderNotWritableException();
    }
  }
}
