# Feature Specification: Receipt Payment Proofs

**Feature Branch**: `004-receipt-payment-proofs`  
**Created**: 2026-06-28  
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Attach Proof of Payment to a Receipt (Priority: P1)

A user creates or edits a receipt and wants to attach one or more files as evidence that the payment was received. The
receipt form includes a dedicated area where the user can drop files, click to browse, or paste an image from the
clipboard. Multiple proofs can be attached to the same receipt.

**Why this priority**: This is the core capability of the feature. Without it, no proof can be stored or viewed. All
other stories depend on this one.

**Independent Test**: Can be fully tested by creating a new receipt, attaching a PDF and an image via each supported
method (drop, click, paste), saving, and confirming that the files are associated with the receipt.

**Acceptance Scenarios**:

1. **Given** a receipt is being created or edited, **When** the user drags and drops a PDF file onto the proof of
   payment area, **Then** the file is attached and displayed as a proof of payment for that receipt.
2. **Given** a receipt is being created or edited, **When** the user clicks the proof of payment area and selects an
   image file via the file picker, **Then** the image is attached and listed as a proof of payment.
3. **Given** a receipt is being created or edited and the user has an image on the clipboard, **When** the user presses
   CTRL+V while the form is active, **Then** the clipboard image is attached as a proof of payment.
4. **Given** a receipt already has one proof attached, **When** the user attaches an additional file, **Then** both
   proofs are associated with the receipt.
5. **Given** a user drops a file of an unsupported type (e.g., `.docx`, `.txt`), **When** the drop completes, **Then**
   the file is rejected and a clear inline error message is shown within the drop zone indicating only PDFs and images
   are accepted.
6. **Given** the user presses CTRL+V when there is no image on the clipboard, **When** the paste action occurs, **Then**
   nothing happens (no error, no attachment).

---

### User Story 2 - View a Proof of Payment from the Receipts List (Priority: P2)

A user browsing the receipts list wants to quickly access a proof of payment without opening the receipt for editing.
Each receipt row has a button for proofs. The button is disabled when no proof exists and enabled when at least one
proof is attached. Clicking an enabled button opens the proof using the appropriate application on the user's computer.

**Why this priority**: This is the consumption side of the feature — viewing proofs is the primary reason for attaching
them. The list screen is the main working surface for users.

**Independent Test**: Can be fully tested by viewing the receipts list, observing that receipts with proofs have an
enabled button and those without have a disabled button, clicking an enabled button, selecting a proof when multiple
exist, and confirming it opens with the correct application.

**Acceptance Scenarios**:

1. **Given** the receipts list is displayed, **When** a receipt has no proofs of payment, **Then** the proof button for
   that receipt is visually disabled and cannot be clicked.
2. **Given** the receipts list is displayed, **When** a receipt has at least one proof of payment, **Then** the proof
   button for that receipt is visually enabled and clickable.
3. **Given** a receipt has exactly one proof of payment, **When** the user clicks the proof button, **Then** the file
   opens immediately using the operating system's default application for that file type.
4. **Given** a receipt has multiple proofs of payment, **When** the user clicks the proof button, **Then** a selection
   dialog appears listing all attached proofs — each entry showing a file-type icon (PDF or image) alongside the
   original filename — and the user can choose which one to open.
5. **Given** the user selects a proof from the selection dialog, **When** the selection is confirmed, **Then** the
   chosen file opens using the operating system's default application for that file type.

---

### User Story 3 - Remove a Proof of Payment (Priority: P3)

A user who attached the wrong file wants to remove a proof of payment from a receipt while in the creation or edit
screen.

**Why this priority**: Mistakes happen during data entry. Without removal, users are stuck with incorrect proofs
attached. Lower priority than viewing since proofs can still be used even if incorrect ones cannot be removed.

**Independent Test**: Can be fully tested by attaching a file to a receipt, removing it from the proof list in the form,
saving, and confirming it no longer appears in the list or is accessible via the proof button.

**Acceptance Scenarios**:

1. **Given** a receipt form has one or more proofs attached, **When** the user removes a proof from the list in the
   form, **Then** that proof is no longer shown in the attachment area.
2. **Given** the user removes all proofs from a receipt and saves, **When** viewing the receipts list, **Then** the
   proof button for that receipt is disabled.

---

### Edge Cases

- What happens when a user drops a folder instead of a file? The drop is ignored and no error is shown (folders are not
  supported).
- What happens if the same file is attached twice? A brief inline warning is shown ("This file is already attached") and
  the duplicate is discarded.
- What happens when the associated file is no longer accessible (deleted or moved from disk)? The proof entry remains in
  the list but attempting to open it shows a clear inline error message indicating the file cannot be found.
- What happens when multiple files are selected at once in the file picker? All selected files that match the accepted
  types are attached; unsupported types among the selection are skipped with a warning.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Users MUST be able to attach one or more proof of payment files to a receipt during creation or editing.
- **FR-002**: The proof of payment area MUST accept PDF files and common image files (JPEG, PNG, GIF, BMP, WEBP, TIFF).
- **FR-003**: The receipt form MUST include a dedicated drop zone area for attaching proofs of payment.
- **FR-004**: Users MUST be able to attach a proof by dragging and dropping a file onto the drop zone.
- **FR-005**: Users MUST be able to attach a proof by clicking the drop zone to open a file picker dialog.
- **FR-006**: Users MUST be able to attach an image proof by pressing CTRL+V when an image is present on the clipboard.
- **FR-007**: The system MUST reject files of unsupported types — determined by file extension — and display a clear
  inline error message within the drop zone.
- **FR-008**: Users MUST be able to remove individual proofs of payment from a receipt within the creation or edit form.
- **FR-009**: The receipts list MUST display a proof of payment button on each receipt row.
- **FR-010**: The proof button MUST be visually disabled and non-interactive when the receipt has no proofs attached.
- **FR-011**: The proof button MUST be visually enabled and clickable when the receipt has at least one proof attached.
- **FR-012**: When clicked and the receipt has exactly one proof, the system MUST open that file immediately using the
  operating system's default application for the file's type.
- **FR-013**: When clicked and the receipt has multiple proofs, the system MUST present a selection dialog listing all
  available proofs, and open the chosen one using the operating system's default application.
- **FR-014**: Proofs of payment MUST be persisted and remain associated with their receipt across application sessions.

### Key Entities

- **Proof of Payment**: Evidence file associated with a receipt. Attributes: original file name, path within the
  app-managed storage directory, file type (MIME category: PDF or image), date attached. Belongs to exactly one receipt.
- **Receipt**: Existing domain entity that gains an ordered collection of zero or more Proof of Payment items.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can attach a proof of payment to a receipt in under 30 seconds using any of the three supported
  input methods (drop, click, paste).
- **SC-002**: 100% of proofs opened from the receipts list open with the correct application for their file type.
- **SC-003**: Users can access a proof of payment directly from the receipts list without navigating to the receipt edit
  screen, reducing navigation steps by at least 2.
- **SC-004**: Receipts with no proof attached always show a disabled button, and receipts with at least one proof always
  show an enabled button — 0% mismatch between proof presence and button state.
- **SC-005**: All supported file types (PDF and common image formats) are successfully accepted and stored without data
  loss.

## Assumptions

- Proof files are copied into an app-managed directory on the local file system when attached; the original source file
  is never modified or removed. No cloud upload or sync is involved.
- There is no enforced maximum file size — available disk space is the only constraint.
- The receipts list screen already exists; this feature adds a proof button to existing list items.
- CTRL+V paste for images is only active when the receipt form (or the drop zone within it) has keyboard focus.
- When exactly one proof is attached and the user clicks the proof button, the file opens directly without an
  intermediate selection dialog.
- Accepted image formats include at minimum: JPEG, PNG, GIF, BMP, WEBP, and TIFF.
- When a proof is removed from a receipt, its copy in the app-managed directory is deleted; the original source file is
  never affected.
- The application runs on a desktop operating system (Windows) with a default application registered for PDF and image
  MIME types.

## Clarifications

### Session 2026-06-28

- Q: Are proof files copied to an app-managed directory or referenced by their original path on disk? → A: Copied to an
  app-managed directory (Option A).
- Q: When the same file is attached twice, should the duplicate be silently ignored or trigger a warning? → A: Show a
  brief inline warning and discard the duplicate (Option B).
- Q: What does each entry in the multi-proof selection dialog display? → A: A file-type icon (PDF or image) alongside
  the original filename (Option C).
- Q: How should the system determine whether a file's type is supported — by file extension, magic bytes, or both? → A:
  File extension only (Option B).
- Q: How should error messages (unsupported file type, file not found) be presented in the UI? → A: Inline within the
  drop zone or proof area where the action was triggered (Option A).
