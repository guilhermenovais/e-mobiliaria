# Phase 0 Research: Monthly Receipts PDF Export

## 1. Default PDF file name

**Decision**: `Recibo_<ddMMyyyy-receipt-date>_<tenant-name-sanitized>.pdf`, e.g.
`Recibo_05072026_Joao_da_Silva.pdf`. Tenant name comes from
`TemplateFormatter.personName(contract.getTenants().getFirst())`
(same helper `ReceiptTemplate` already uses for the PDF body), sanitized by replacing whitespace with `_` and
stripping characters invalid on Windows filesystems (`\/:*?"<>|`). If a name collision occurs within the same
export run (two receipts resolve to the same file name), the later one processed overwrites the earlier one — this
is explicitly accepted in the spec's Edge Cases section, so no dedup/suffixing logic is needed.

**Rationale**: The spec's assumption states the default name should match "the same file-naming convention already
used elsewhere in the app when a single receipt is generated as a PDF." Investigation of `ReceiptListController.
handleGeneratePdf` shows this assumption does not hold literally today — the single-receipt flow writes to a
throwaway temp file (`recibo_<id>_.pdf`) via `File.createTempFile` and opens it with `Desktop.getDesktop().open`; it
is never given a human-meaningful name because it's never "saved" anywhere the user sees the file name. There is
therefore no existing convention to reuse verbatim. This feature introduces the first real default-naming
convention for receipt PDFs, exposed as a new `ReceiptFileService.defaultFileName(Receipt)` method. Using the
receipt date + tenant name keeps files identifiable and sortable in a folder listing, and mirrors the two data
points a bulk export needs to disambiguate by (FR-006).

**Alternatives considered**:

- Reuse the temp-file naming pattern (`recibo_<id>_`) — rejected: not human-readable, doesn't satisfy "default file
  name" in a way a user exporting dozens of files could visually distinguish.
- Include the contract/property name instead of tenant — rejected: tenant name is the primary identifying data on
  the receipt itself (`RECEIPT_TEXT` parameter starts with "received from <tenant>"), consistent with how the PDF
  content already centers the tenant.

## 2. Destination folder selection

**Decision**: `javafx.stage.DirectoryChooser`, invoked from `ExportReceiptsDialog` on the JavaFX Application
Thread, same as any other JavaFX native chooser.

**Rationale**: The spec's assumption says the folder should be chosen "consistent with other file operations in
the app (e.g., backup and restore)." Backup/restore was checked (`backup/ui/controller/BackupRestoreController.java`)
and it does *not* use a folder-chooser dialog — backup destinations come from `WindowsDriveDetectionService`
(auto-detected removable drives), and restore reads backups from those same detected drives. Neither flow lets the
user free-pick an arbitrary folder, so there is no precedent to copy directly. The codebase's actual precedent for
"let the user pick a filesystem location" is `javafx.stage.FileChooser`, used in
`receipt/ui/component/ProofDropZonePane.java` to pick proof files. `DirectoryChooser` is the direct JavaFX sibling
of `FileChooser` for picking folders instead of files, requires no new dependency (it's part of `javafx-controls`,
already a project dependency), and is the standard JavaFX API for this exact task.

**Alternatives considered**:

- `javax.swing.filechooser.FileSystemView` (used by `WindowsDriveDetectionService` for drive enumeration) —
  rejected: that class enumerates roots, it doesn't provide a folder-picker UI; would require building a custom
  tree dialog for no benefit over the native `DirectoryChooser`.
- Reuse `FileChooser` with a directory-only filter — rejected: `FileChooser` cannot restrict selection to
  directories; `DirectoryChooser` is the correct API for this.

## 3. Determining which months have receipts

**Decision**: Add `List<YearMonth> findAllReceiptMonths()` to `ReceiptRepository`, implemented in
`JdbcReceiptRepository` as `SELECT DISTINCT FORMATDATETIME(date, 'yyyy-MM') AS ym FROM receipts ORDER BY ym DESC`,
parsed into `YearMonth` values. Add `List<Receipt> findAllByMonth(YearMonth month)` for the actual export,
implemented as a `date BETWEEN ? AND ?` query using `month.atDay(1)` / `month.atEndOfMonth()`, reusing the existing
`map(rs, conn)` row mapper (same N+1 contract-loading pattern already used by `findAllByContractId` and `search` —
not optimized further here since receipt volumes are small and this matches existing repository conventions).

**Rationale**: `ReceiptRepository` currently has no way to query by date/month at all — every existing query is
scoped by `contractId`. FR-003 requires the month selector to only list months with at least one receipt, and FR-005
requires exporting by receipt date across all contracts, so two new read methods are the minimal necessary addition.
Filtering in SQL (rather than loading all receipts into memory and grouping in Java) avoids pulling the entire
receipts table for what is just a combo-box population step.

**Alternatives considered**:

- Compute distinct months in Java by loading all receipts and grouping by `YearMonth.from(date)` — rejected: works,
  but does a full table + full contract-graph load (each receipt load triggers ~6 nested queries per
  `loadContract`) just to populate a dropdown; the SQL `DISTINCT` query avoids that entirely.

## 4. Failure handling per receipt

**Decision**: `ExportReceiptsByMonthInteractor` iterates the month's receipts, wrapping each
`receiptFileService.generateReceiptPdf(receipt)` + `receiptExportService.writePdf(...)` pair in a try/catch. On
success, increment a counter. On any `RuntimeException` other than one signaling the destination folder itself is
unwritable, record a `FailedExport(receiptId, reason)` and continue to the next receipt. A folder-level write
failure (caught once, e.g. via a pre-flight `Files.isWritable(folder)` check before the loop, or the first
`AccessDeniedException`/`IOException` thrown by the writer) aborts the whole operation and surfaces
`ExportFolderNotWritableException` — this matches the clarified answer in spec.md ("skip the failing receipt,
continue... " for per-receipt failures) plus the pre-existing Edge Case distinguishing folder-unwritable (hard
abort) from per-receipt failures (skip and continue).

**Rationale**: Directly implements the spec's clarification session answer and the Edge Cases section. A pre-flight
writability check (`Files.isWritable`) lets the interactor fail fast with one clear error instead of the first of
N receipts silently absorbing the folder permission error into the per-receipt failure list.

**Alternatives considered**:

- Treat every I/O failure, including folder-level, as a per-receipt skip — rejected: explicitly contradicted by the
  Edge Cases section ("the export fails and the user is shown a clear error message" for the unwritable-folder
  case, as opposed to "that receipt is skipped" for other failures).
