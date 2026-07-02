# Implementation Plan: Monthly Receipts PDF Export

**Branch**: `007-monthly-receipts-pdf-export` | **Date**: 2026-07-02 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/007-monthly-receipts-pdf-export/spec.md`

## Summary

Add an "Export Month" action to the receipts list page that lets the user pick a calendar month (restricted to
months that actually have receipts) and a destination folder, then generates one PDF per receipt whose receipt
date falls in that month, saving each into the chosen folder under its default file name (overwriting on
conflict). Receipts that fail to render are skipped and reported by name alongside the success count; the whole
export aborts only if the destination folder itself is not writable.

## Technical Context

**Language/Version**: Java 24
**Primary Dependencies**: JavaFX 21, Google Guice 7, JasperReports 7.0.3 + OpenPDF (via existing
`PdfGenerationService`), HikariCP 7 / H2 (existing `ReceiptRepository`)
**Storage**: H2 embedded database (source of receipts); local filesystem (export destination, user-chosen folder)
**Testing**: JUnit 5 (maven-surefire-plugin)
**Target Platform**: Windows desktop (jpackage native installer)
**Project Type**: Desktop app (JavaFX + Guice), package-by-feature
**Performance Goals**: Exporting a typical month (dozens of receipts) completes within a few seconds; UI thread
must stay responsive (PDF generation and file I/O run off the FX Application Thread)
**Constraints**: Must not write any file until the user explicitly confirms (FR-011); a single receipt's PDF
generation failure must not abort the rest of the export (FR-012); overwrite-without-prompt is required behavior,
not a bug
**Scale/Scope**: Single dialog + one new repository query pair + one new use case + one new domain service method
on the existing `receipt` feature; no new persisted entities

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The project constitution file (`.specify/memory/constitution.md`) is still the unfilled template — no concrete
project-specific gates are defined there. Proceeding on the basis of the architectural rules in
`docs/general-instructions.md` (package-by-feature + internal layers, Guice constructor injection, no business
rules outside the domain layer):

- ✅ Package-by-Feature: all new code lives inside the existing `receipt` feature package; no new top-level feature
  needed.
- ✅ Domain/application purity: the export use case orchestrates the existing `ReceiptRepository` and
  `ReceiptFileService`; a new `ReceiptExportService` domain interface owns the file-system write/overwrite logic,
  implemented in `infrastructure`.
- ✅ Infrastructure enforces no business rules: month filtering and per-receipt failure aggregation live in the
  application-layer interactor, not in the infrastructure writer.
- ✅ Constructor injection with `@Inject` throughout; no field injection.
- ✅ `GuiceFxmlLoader` is not needed here — the export dialog is built programmatically (`javafx.scene.control.Dialog`),
  consistent with `ProofSelectionDialog` in the same feature.
- ✅ No new persistence/repository entity; two additive read methods on the existing `ReceiptRepository`.

No violations to justify in Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/007-monthly-receipts-pdf-export/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
├── contracts/           # Phase 1 output
│   └── receipt-export.md
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
src/main/java/com/guilherme/emobiliaria/
├── receipt/
│   ├── domain/
│   │   ├── entity/
│   │   │   └── ReceiptExportResult.java          # NEW: record(exportedCount, List<FailedExport>)
│   │   ├── repository/
│   │   │   └── ReceiptRepository.java            # MODIFIED: + findAllReceiptMonths(), findAllByMonth(YearMonth)
│   │   └── service/
│   │       ├── ReceiptFileService.java           # MODIFIED: + defaultFileName(Receipt)
│   │       └── ReceiptExportService.java         # NEW: writePdf(Path folder, String fileName, byte[] pdf)
│   │
│   ├── application/
│   │   ├── input/
│   │   │   └── ExportReceiptsByMonthInput.java   # NEW: record(YearMonth month, Path destinationFolder)
│   │   ├── output/
│   │   │   ├── ExportReceiptsByMonthOutput.java  # NEW: record(ReceiptExportResult result)
│   │   │   └── GetExportableReceiptMonthsOutput.java # NEW: record(List<YearMonth> months)
│   │   └── usecase/
│   │       ├── ExportReceiptsByMonthInteractor.java   # NEW
│   │       └── GetExportableReceiptMonthsInteractor.java # NEW
│   │
│   ├── infrastructure/
│   │   ├── repository/
│   │   │   └── JdbcReceiptRepository.java        # MODIFIED: implement new query methods
│   │   └── service/
│   │       ├── ReceiptFileServiceImpl.java       # MODIFIED: implement defaultFileName(Receipt)
│   │       └── FileSystemReceiptExportService.java # NEW: Files.write with REPLACE_EXISTING
│   │
│   ├── di/
│   │   └── ReceiptModule.java                    # MODIFIED: bind ReceiptExportService
│   │
│   └── ui/
│       └── controller/
│           ├── ReceiptListController.java        # MODIFIED: + "Export Month" button/handler
│           └── ExportReceiptsDialog.java         # NEW: month ComboBox + DirectoryChooser trigger + confirm
│
└── shared/
    └── exception/
        ├── ErrorMessage.java                     # MODIFIED: + Receipt.EXPORT_FOLDER_NOT_WRITABLE
        └── ExportFolderNotWritableException.java # NEW

src/main/resources/
├── messages.properties                           # MODIFIED: + receipt.export.* keys
├── messages_pt_BR.properties                     # MODIFIED: + receipt.export.* keys
└── com/guilherme/emobiliaria/receipt/ui/view/
    └── receipt-list-view.fxml                    # MODIFIED: add fx:id export button next to newButton
```

**Structure Decision**: Single-project, package-by-feature. Everything is additive inside the existing `receipt`
feature; no new feature package, no new FXML file for the dialog itself (built programmatically like
`ProofSelectionDialog`), only a small addition to the existing list view FXML for the trigger button.

## Complexity Tracking

> No constitution violations. Table intentionally omitted.
