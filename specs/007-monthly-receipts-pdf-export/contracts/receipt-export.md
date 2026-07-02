# Contract: Receipt Monthly Export Use Cases

This is a desktop application with no external API; the "contract" here is the interface the `receipt.ui` layer
depends on, exposed by the `receipt.application.usecase` package. UI code (`ExportReceiptsDialog`,
`ReceiptListController`) must only depend on these two interactors and their input/output records — never on the
repository or infrastructure service directly.

## `GetExportableReceiptMonthsInteractor`

```java
public GetExportableReceiptMonthsOutput execute();
```

- **Input**: none.
- **Output**: `GetExportableReceiptMonthsOutput(List<YearMonth> months)` — months that have at least one receipt,
  ordered most-recent-first (matches `findAllReceiptMonths()` SQL ordering).
- **Errors**: none expected under normal operation (empty list is a valid, non-error result — the dialog should
  disable the confirm action / show no selectable months rather than treat it as a failure).
- **Called by**: `ExportReceiptsDialog` when it is constructed, to populate the month `ComboBox<YearMonth>`.

## `ExportReceiptsByMonthInteractor`

```java
public ExportReceiptsByMonthOutput execute(ExportReceiptsByMonthInput input);
```

- **Input**: `ExportReceiptsByMonthInput(YearMonth month, Path destinationFolder)`.
- **Output**: `ExportReceiptsByMonthOutput(ReceiptExportResult result)` where `result.exportedCount()` is the
  number of PDFs successfully written and `result.failures()` lists any receipts skipped due to a
  generation/write failure (receipt ID + reason).
- **Preconditions**: `destinationFolder` must already exist and be a directory (the UI's `DirectoryChooser` only
  ever returns existing directories, so the interactor does not need to create one).
- **Errors**:
    - `ExportFolderNotWritableException` — thrown (not swallowed) when `destinationFolder` is not writable, or the
      export fails before any file could be written for that reason. The whole operation aborts; no partial
      `ReceiptExportResult` is returned in this case, per FR-010 and the spec's Edge Cases section.
    - Any other per-receipt PDF generation or write failure is **not** thrown — it is captured as a
      `ReceiptExportResult.FailedExport` entry and the loop continues, per FR-012 and the spec's clarification
      answer.
- **Postconditions**: for every receipt whose date falls in `month` and which did not fail, exactly one file named
  per `ReceiptFileService.defaultFileName(Receipt)` exists in `destinationFolder`, overwriting any pre-existing
  file of the same name (FR-007). Files in `destinationFolder` that do not match any exported receipt's name are
  left untouched (FR-008). No file is written before this method is invoked (FR-011) — the dialog only calls this
  interactor after the user confirms.
- **Called by**: `ExportReceiptsDialog`'s confirm action, on a background `javafx.concurrent.Task` (not the FX
  Application Thread), matching the existing pattern in `ReceiptListController.handleGeneratePdf` /
  `BackupRestoreController.onBackup`.

## Domain service contracts introduced

### `ReceiptFileService.defaultFileName(Receipt receipt): String`

Added alongside the existing `generateReceiptPdf(Receipt): byte[]`. Returns the file name (including `.pdf`
extension, no path) a receipt's PDF should use whenever it is saved to disk with a human-meaningful name — used by
both this feature and available for future reuse by the single-receipt "Generate PDF" action if that action is
ever changed to save-to-disk instead of open-from-temp-file.

### `ReceiptExportService.writePdf(Path folder, String fileName, byte[] pdfBytes): void`

New domain service interface, `infrastructure`-implemented as `FileSystemReceiptExportService`, wrapping
`Files.write(folder.resolve(fileName), pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)`.
Throws `ExportFolderNotWritableException` on `IOException`/`AccessDeniedException` at the folder level (the
interactor is responsible for distinguishing folder-level failures from per-file failures per the Phase 0 research
decision — see `research.md` §4).
