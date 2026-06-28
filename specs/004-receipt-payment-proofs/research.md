# Research: Receipt Payment Proofs

## File Storage Strategy

**Decision**: App-managed directory under `AppDataPaths.resolveAppDataDir().resolve("proofs")` — a new
`proofStorageDirectory()` method added to `AppDataPaths`.

**Rationale**: The existing `AppDataPaths` already manages `database/` and `logs/` subdirectories under
`%LOCALAPPDATA%\e-mobiliaria\` (Windows) or `~/.e-mobiliaria/` (other OS). Adding `proofs/` follows the same pattern
with zero new dependencies. Files are copied on attach (source file is never modified), deleted on removal, and persist
across sessions.

**Alternatives considered**: Storing binary blobs in H2 — rejected because it bloats the database file and makes file
access more complex; referencing original paths — rejected per spec (files are copied, not referenced).

---

## File Type Validation

**Decision**: Extension-based validation only (per spec clarification). Accepted extensions: `.pdf`, `.jpg`, `.jpeg`,
`.png`, `.gif`, `.bmp`, `.webp`, `.tiff`, `.tif`.

**Rationale**: The spec explicitly chose "file extension only" over magic-byte validation. This keeps the implementation
simple and consistent with the clarified requirement.

**Alternatives considered**: MIME-type sniffing via Java's `Files.probeContentType()` or Apache Tika — rejected per spec
decision.

---

## Stored File Naming

**Decision**: `UUID.randomUUID().toString() + ".<extension>"` as the stored filename. The original filename is preserved
separately in the `original_filename` column.

**Rationale**: Avoids filename collisions when the same filename is attached to different receipts. Preserves the
user-visible original name for display. The UUID approach is used in many existing patterns and requires no additional
libraries.

---

## Duplicate Detection

**Decision**: Compare `originalFileName` within the current receipt's in-progress proofs list on the UI side. If a file
with the same name is already staged or persisted, show an inline warning.

**Rationale**: The spec says "show a brief inline warning and discard the duplicate". Comparing by original filename is
sufficient since the spec chose extension-only validation and doesn't mandate content hash comparison.

---

## Proof Count in Receipt List

**Decision**: After loading a page of receipts via `findAllByContractId` / `search`, the JDBC repository executes one
additional `SELECT receipt_id, COUNT(*) FROM payment_proofs WHERE receipt_id IN (...)` batch query. Results are applied
to each `Receipt` in the page to set the `proofs` list (empty = no proofs, or a lightweight count representation).
`Receipt.hasProofs()` returns `!proofs.isEmpty()`.

**Rationale**: A single IN-batch query avoids N+1 problems for 20-item pages. The `Receipt` entity already holds its
full `Contract` (eagerly loaded), so adding a proof list is consistent. The list view only needs `hasProofs()` — the
full proof objects are loaded when the user opens the form or clicks the proofs button.

**Alternatives considered**: Adding a `proofCount` int field to `Receipt` — avoids loading `PaymentProof` objects but
adds a field that's semantically redundant with `proofs.size()`. Storing a denormalized count in the `receipts` table —
rejects because it adds write-time complexity for what is a read-time performance concern solved by the batch query.

---

## Opening Files with the OS

**Decision**: Use `java.awt.Desktop.getDesktop().open(file)`, same pattern already used by
`ReceiptListController.handleGeneratePdf()` for opening PDF receipts. Run on a JavaFX background thread (wrapping in
`Task`) to avoid blocking the UI thread.

**Rationale**: `Desktop.open()` honors the OS-registered default application for any file type. Already imported (
`requires java.desktop` in `module-info.java`) and in use.

---

## Clipboard Paste (CTRL+V) Support

**Decision**: Add a `KeyEvent` handler on the `ReceiptFormController`'s scene (or the drop zone component) that checks
`Clipboard.getSystemClipboard().hasImage()` when CTRL+V is pressed. If an image is present, write it to a temp file and
pass it through the same attachment flow. If no image, do nothing.

**Rationale**: JavaFX's `Clipboard` API supports `hasImage()` / `getImage()` natively. The spec says paste should only
trigger when the receipt form (or drop zone) has focus. Attaching to the scene root covers this.

---

## Architecture: Proof Persistence relative to Receipt Save

**Decision**: Proof attachment and removal are performed as separate use cases (`AttachPaymentProofInteractor`,
`RemovePaymentProofInteractor`), called by the UI controller after the receipt is saved. The form controller stages "to
attach" and "to remove" lists; on save: (1) create/update receipt, (2) remove marked proofs, (3) attach new proofs.

**Rationale**: Keeps `CreateReceiptInteractor` and `EditReceiptInteractor` clean — they don't need to know about file
I/O. Each use case has a single responsibility. If step 1 fails, no proof file is created. Partial proof attachment
failure is acceptable for a desktop app (no distributed transactions needed).

**Alternatives considered**: Passing proof paths through `CreateReceiptInput` — rejected because it couples file
management to the receipt save use case and complicates interactor testing. A single transaction wrapping receipt +
proofs — not achievable without framework support, and over-engineering for a local desktop app.

---

## When a Receipt Is Deleted

**Decision**: `DeleteReceiptInteractor` calls `RemovePaymentProofInteractor` for each proof associated with the receipt
before deleting the receipt record. The `payment_proofs` table uses `ON DELETE CASCADE` as a safety net, but the
physical files are deleted by the interactor.

**Rationale**: The DB cascade handles the DB rows; the interactor must handle the file system. Running file deletion
before the DB delete means a DB failure leaves orphaned files (acceptable) rather than DB rows pointing to missing
files.
