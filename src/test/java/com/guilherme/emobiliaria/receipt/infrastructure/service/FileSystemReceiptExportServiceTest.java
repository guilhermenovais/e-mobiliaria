package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.guilherme.emobiliaria.shared.exception.ExportFolderNotWritableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FileSystemReceiptExportServiceTest {

  private final FileSystemReceiptExportService service = new FileSystemReceiptExportService();


  @Nested
  class WritePdf {

    @Test
    @DisplayName(
        "When a file with the same name already exists, should overwrite it without creating a second file")
    void shouldOverwriteExistingFileWithSameName(@TempDir Path folder) throws IOException {
      Path target = folder.resolve("recibo.pdf");
      Files.write(target, "old content".getBytes(StandardCharsets.UTF_8));

      byte[] newContent = "new content".getBytes(StandardCharsets.UTF_8);
      service.writePdf(folder, "recibo.pdf", newContent);

      assertArrayEquals(newContent, Files.readAllBytes(target));
      try (var stream = Files.list(folder)) {
        assertEquals(1, stream.count());
      }
    }

    @Test
    @DisplayName("When an unrelated file exists in the folder, should leave it untouched")
    void shouldLeaveUnrelatedFileUntouched(@TempDir Path folder) throws IOException {
      Path unrelated = folder.resolve("notes.txt");
      Files.write(unrelated, "keep me".getBytes(StandardCharsets.UTF_8));

      service.writePdf(folder, "recibo.pdf", "content".getBytes(StandardCharsets.UTF_8));

      assertEquals("keep me", Files.readString(unrelated, StandardCharsets.UTF_8));
    }

    @Test
    @DisplayName(
        "When the destination folder has no write permission, should throw ExportFolderNotWritableException")
    void shouldThrowWhenFolderNotWritable(@TempDir Path folder) {
      boolean writableChanged = folder.toFile().setWritable(false);
      org.junit.jupiter.api.Assumptions.assumeTrue(writableChanged,
          "Skipping: platform does not support revoking write permission");

      try {
        assertThrows(ExportFolderNotWritableException.class,
            () -> service.writePdf(folder, "recibo.pdf",
                "content".getBytes(StandardCharsets.UTF_8)));
      } finally {
        folder.toFile().setWritable(true);
      }
    }
  }
}
