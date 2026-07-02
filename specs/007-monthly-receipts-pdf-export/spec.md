# Feature Specification: Monthly Receipts PDF Export

**Feature Branch**: `007-monthly-receipts-pdf-export`
**Created**: 2026-07-02
**Status**: Draft
**Input**: User description: "On the receipts page, I want a new functionality to allow the generation of the PDFs of
all receipts of a given month. When clicking the button, the user should be able to select the month and a folder,
and when confirming, all pdf's should be saved on that folder, with their default names. If the folder already
contains a file which name's conflict with one being save, it should be overwriten. Receipts should be included based
on their Receipt Date. Only months that have at least one receipt should be available for selection by the user."

## Clarifications

### Session 2026-07-02

- Q: If generating the PDF for one specific receipt fails for a reason other than the destination folder being
  unwritable (e.g., malformed or missing data needed to render it), should the whole export abort, or should that
  receipt be skipped so the rest of the export still completes? → A: Skip the failing receipt, continue exporting the
  rest, and report which ones failed alongside the success count.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Export All Receipts for a Month (Priority: P1)

A user on the receipts page wants a PDF file for every receipt issued in a given month, without opening and
downloading each receipt individually. The user clicks a new "Export Month" button, picks a month from a list of
months that actually have receipts, picks a destination folder, and confirms. The system generates a PDF for every
receipt whose receipt date falls in that month and saves each one into the chosen folder using its default file name.

**Why this priority**: This is the entire feature — without it, there is no bulk export capability at all.

**Independent Test**: Can be fully tested by creating several receipts with receipt dates in the same month (and at
least one in a different month), triggering the export for that month, and confirming that only the PDFs for the
matching receipts appear in the chosen folder.

**Acceptance Scenarios**:

1. **Given** the receipts page is displayed, **When** the user clicks the "Export Month" button, **Then** a dialog
   opens showing a month selector and a way to choose a destination folder.
2. **Given** the export dialog is open, **When** the user opens the month selector, **Then** only months that have at
   least one receipt are listed as options.
3. **Given** the user has selected a month with three receipts and a destination folder, **When** the user confirms
   the export, **Then** three PDF files are created in the chosen folder, one per receipt, each using its default
   file name.
4. **Given** the export has completed, **When** the user inspects the result, **Then** the system shows a confirmation
   indicating how many receipts were exported.
5. **Given** a receipt has its receipt date on the first day of a month and another receipt has its receipt date on
   the last day of the same month, **When** that month is exported, **Then** both receipts are included.

---

### User Story 2 - Overwrite Conflicting Files (Priority: P2)

A user re-runs the export for a month they already exported before (e.g., after correcting a receipt), pointing to
the same folder. The user expects the corrected PDFs to replace the old ones without being asked to resolve each
conflict individually.

**Why this priority**: Without this, re-exporting into a previously used folder would either fail or silently skip
files, producing stale or incomplete results. It is secondary to the core export capability but necessary for the
feature to be usable in practice.

**Independent Test**: Can be fully tested by exporting a month's receipts into a folder, modifying a receipt, changing
the destination file's content out of band (or simply re-exporting), and confirming the file in the folder reflects
the newest export without any conflict prompt.

**Acceptance Scenarios**:

1. **Given** the destination folder already contains a file whose name matches the default name of a receipt being
   exported, **When** the export runs, **Then** the existing file is overwritten with the new PDF without prompting
   the user.
2. **Given** the destination folder contains unrelated files that do not match any exported receipt's default name,
   **When** the export runs, **Then** those unrelated files are left untouched.

---

### Edge Cases

- What happens when the user selects a destination folder without write permission? The export fails and the user is
  shown a clear error message; no partial files are left in an inconsistent state for that receipt.
- What happens when a receipt included in the export is deleted by another process between selecting the month and
  confirming the export? That receipt is skipped and the remaining receipts are still exported.
- What happens when generating the PDF for a specific receipt fails for a reason other than deletion (e.g., malformed
  or missing data required to render it)? That receipt is skipped, the remaining receipts are still exported, and the
  receipt is included in a list of failures reported to the user alongside the success count.
- What happens when two receipts in the same month resolve to the same default file name (e.g., same tenant, same
  date)? The later one processed overwrites the earlier one within the same export run, consistent with the
  overwrite behavior for pre-existing files.
- What happens if the user cancels the folder selection or the dialog before confirming? No files are generated and
  no changes are made.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The receipts page MUST provide a button that opens the monthly PDF export dialog.
- **FR-002**: The export dialog MUST let the user select exactly one month from a list of selectable months.
- **FR-003**: The list of selectable months MUST only include months for which at least one receipt exists, determined
  by each receipt's receipt date; months with zero receipts MUST NOT be selectable.
- **FR-004**: The export dialog MUST let the user choose a destination folder on the local file system.
- **FR-005**: Upon confirmation, the system MUST generate a PDF for every receipt whose receipt date falls within the
  selected month, regardless of which contract the receipt belongs to.
- **FR-006**: Each generated PDF MUST be saved into the chosen destination folder using the same default file name it
  would use when downloaded individually from the receipts list.
- **FR-007**: If a file with the same name already exists in the destination folder, the system MUST overwrite it
  without prompting the user for confirmation.
- **FR-008**: The system MUST leave any file in the destination folder that does not conflict with an exported
  receipt's file name unmodified.
- **FR-009**: The system MUST inform the user when the export completes, including how many receipts were exported
  and, if any receipts were skipped due to a generation failure, which ones and why.
- **FR-010**: The system MUST show a clear error message if the export cannot complete (e.g., destination folder is
  not writable) without leaving the application in a broken state.
- **FR-011**: Selecting a month or destination folder MUST NOT generate or save any files until the user explicitly
  confirms the export.
- **FR-012**: If generating the PDF for a specific receipt fails for a reason other than the destination folder being
  unwritable, the system MUST skip that receipt, continue exporting the remaining receipts, and include the skipped
  receipt in the completion report.

### Key Entities

- **Receipt**: Existing domain entity being exported; its receipt date determines which month it belongs to for the
  purposes of this feature.
- **Export Month Selection**: A transient choice of calendar month (year + month), scoped to the current export
  action only — not persisted.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user can export all receipts for a month to a folder in three actions or fewer (open dialog, pick
  month and folder, confirm).
- **SC-002**: 100% of receipts whose receipt date falls within the selected month are present as PDF files in the
  destination folder after export.
- **SC-003**: 0% of receipts whose receipt date falls outside the selected month appear in the export output.
- **SC-004**: Re-running an export for the same month into the same folder always results in the folder containing
  exactly the current set of PDFs for that month, with no duplicate or stale files left behind.
- **SC-005**: Months with no receipts are never selectable, verified across 100% of available months in the selector.

## Assumptions

- "Default file name" means the same file-naming convention already used elsewhere in the app when a single receipt
  is generated as a PDF (e.g., derived from tenant and receipt date), so exported files remain identifiable and
  consistent with single-receipt output.
- The export is not scoped by any contract filter currently active on the receipts page — selecting a month exports
  every receipt across all contracts whose receipt date falls in that month, since the feature request describes
  exporting "all receipts of a given month" without mentioning a contract-level restriction.
- The month selector offers a reasonable, bounded set of months (those with at least one receipt); no explicit limit
  on how far back the list can go beyond that.
- The destination folder is chosen via the operating system's native folder picker, consistent with other file
  operations in the app (e.g., backup and restore).
- No confirmation prompt is required before overwriting conflicting files, since the user has explicitly requested
  overwrite behavior as the default.
- This feature does not alter or remove any existing single-receipt PDF generation capability; it adds a new,
  separate bulk export action.
