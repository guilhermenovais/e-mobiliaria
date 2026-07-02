package com.guilherme.emobiliaria.receipt.domain.service;

import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class FakeReceiptExportService extends FakeImplementation implements ReceiptExportService {

  private final Map<Path, byte[]> writtenFiles = new LinkedHashMap<>();

  @Override
  public void writePdf(Path folder, String fileName, byte[] pdfBytes) {
    maybeFail();
    writtenFiles.put(folder.resolve(fileName), pdfBytes);
  }

  public Map<Path, byte[]> writtenFiles() {
    return writtenFiles;
  }
}
