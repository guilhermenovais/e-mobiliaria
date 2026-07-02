# Quickstart: Monthly Receipts PDF Export

## Manual verification (User Story 1 — Export All Receipts for a Month)

1. Run the app (`mvn javafx:run` or the existing launch task) and navigate to a contract's receipts, or the
   receipts list page directly.
2. Create at least 3 receipts with `date` values in the same month (e.g. July 2026), and at least 1 receipt with a
   `date` in a different month.
3. Click the new **Export Month** button next to "+ New Receipt".
4. In the dialog, open the month selector — confirm only months with at least one receipt appear (the different-month
   receipt's month should also appear, but a month with zero receipts must not).
5. Select the month with 3 receipts, click "Choose folder", pick an empty local folder, then confirm.
6. Verify: exactly 3 PDF files appear in the chosen folder, each named per the `Recibo_<ddMMyyyy>_<tenant>.pdf`
   convention, and a completion message reports "3 receipts exported."
7. Open each PDF and confirm it matches the receipt it corresponds to (correct tenant, amounts, period).

## Manual verification (User Story 2 — Overwrite Conflicting Files)

1. Re-run the export for the same month into the same folder without changing anything.
2. Verify the folder still contains exactly 3 files (no duplicates, no `(1)`-suffixed copies) and no overwrite
   confirmation prompt appears.
3. Edit one of the 3 receipts (e.g. change its discount), re-export into the same folder, and confirm the
   corresponding PDF's content reflects the edit.
4. Drop an unrelated file (e.g. `notes.txt`) into the destination folder, re-export, and confirm `notes.txt` is
   untouched afterward.

## Manual verification (Edge Cases)

1. Point the export at a folder without write permission (e.g. a read-only-mounted directory) — confirm a clear
   error is shown and no files are left in the folder in a partial/corrupt state.
2. Delete a receipt (via another instance or direct DB manipulation) between opening the dialog and confirming —
   confirm the export still completes for the remaining receipts and the deleted one is not silently counted as a
   success.
3. Cancel the folder chooser or the dialog before confirming — confirm no files are written anywhere.

## Automated test coverage to add

- `JdbcReceiptRepositoryTest` (or equivalent): `findAllReceiptMonths()` returns distinct, correctly-ordered
  `YearMonth`s; `findAllByMonth(YearMonth)` returns exactly the receipts whose `date` falls within the month
  (including first/last day boundary cases per Acceptance Scenario 5).
- `ExportReceiptsByMonthInteractorTest`: happy path (N receipts exported, count matches); per-receipt failure is
  skipped and reported while the rest still export; folder-unwritable case aborts and throws
  `ExportFolderNotWritableException` without partial writes recorded as successes.
- `ReceiptFileServiceImplTest` (or new test class): `defaultFileName(Receipt)` produces the expected sanitized,
  collision-tolerant name for representative receipts (physical-person tenant, juridical-person tenant, name with
  spaces/accents).
