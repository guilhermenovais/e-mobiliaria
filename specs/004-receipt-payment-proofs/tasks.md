---
description: "Task list for Receipt Payment Proofs feature implementation"
---

# Tasks: Receipt Payment Proofs

**Input**: Design documents from `/specs/004-receipt-payment-proofs/`  
**Prerequisites**: plan.md ✅, spec.md ✅, research.md ✅, data-model.md ✅, quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story?] Description`

- **[P]**: Can run in parallel (different files, no unmet dependencies)
- **[Story]**: User story this task belongs to (US1, US2, US3)
- Exact file paths are included in every description

## Path Conventions

- Main source: `src/main/java/com/guilherme/emobiliaria/`
- Test source: `src/test/java/com/guilherme/emobiliaria/`
- Resources: `src/main/resources/`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: DB schema change that must exist before any application code can compile or run.

- [X] T001 Create Flyway migration `src/main/resources/db/migration/V10__add_payment_proofs.sql` with `payment_proofs`
  table (columns: id, receipt_id FK, original_filename, stored_filename, file_type, attached_at) and index on receipt_id

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Domain entities, interfaces, fakes, infrastructure, DI, and module wiring that ALL user stories depend on.
No user story can begin until this phase is complete.

**⚠️ CRITICAL**: Complete this phase before starting any user story work.

### Domain Layer

- [X] T002 [P] Create `ProofFileType` enum in `receipt/domain/entity/ProofFileType.java` with values `PDF` and `IMAGE`,
  accepted extensions per value, and static factory `fromExtension(String filename) → Optional<ProofFileType>`
- [X] T003 Create `PaymentProof` entity in `receipt/domain/entity/PaymentProof.java` with fields (id, originalFileName,
  storedFileName, fileType, attachedAt, receiptId), factory methods `create(...)` and `restore(...)`, and
  `BusinessException` validation for null/blank fields (depends on T002)
- [X] T004 [P] Create `PaymentProofRepository` interface in `receipt/domain/repository/PaymentProofRepository.java` with
  methods: `create`, `delete`, `findAllByReceiptId`, `deleteAllByReceiptId`, `countByReceiptIds` (depends on T003)
- [X] T005 [P] Create `PaymentProofStorageService` interface in `receipt/domain/service/PaymentProofStorageService.java`
  with methods: `copyToStorage(Path, String)`, `copyBytesToStorage(byte[], String)`, `delete(String)`, `resolve(String)`
- [X] T006 Update `receipt/domain/entity/Receipt.java` — add `proofs` field (`List<PaymentProof>` defaulting to empty
  list), `getProofs()` (unmodifiable view), `hasProofs()`, and `restoreWithProofs(...)` overload (existing `restore()`
  unchanged) (depends on T003)

### Shared Infrastructure

- [X] T007 [P] Add `proofStorageDirectory()` static method to `shared/persistence/AppDataPaths.java` returning
  `ensureDirectory(resolveAppDataDir().resolve("proofs"))`

### Test Fakes (needed before use-case tests)

- [X] T008 [P] Create `PaymentProofTest` in `receipt/domain/entity/PaymentProofTest.java` — unit tests for `create`/
  `restore` factory methods and validation rules (depends on T003)
- [X] T009 [P] Create `FakePaymentProofRepository` in
  `test/.../receipt/domain/repository/FakePaymentProofRepository.java` implementing `PaymentProofRepository` with
  in-memory storage (depends on T004)
- [X] T010 [P] Create `FakePaymentProofStorageService` in
  `test/.../receipt/domain/service/FakePaymentProofStorageService.java` implementing `PaymentProofStorageService` using
  temp files or no-op (depends on T005)

### Infrastructure Implementations

- [X] T011 Create `PaymentProofStorageServiceImpl` in
  `receipt/infrastructure/service/PaymentProofStorageServiceImpl.java` — `copyToStorage` uses `UUID + ext` naming and
  `Files.copy`; `copyBytesToStorage` writes bytes; `delete` calls `Files.deleteIfExists`; `resolve` returns full path (
  depends on T005, T007)
- [X] T012 Create `JdbcPaymentProofRepository` in `receipt/infrastructure/repository/JdbcPaymentProofRepository.java` —
  standard JDBC pattern matching existing repositories; implements all five interface methods including IN-clause batch
  for `countByReceiptIds` (depends on T004)
- [X] T013 Create `JdbcPaymentProofRepositoryTest` in
  `receipt/infrastructure/repository/JdbcPaymentProofRepositoryTest.java` — H2 in-memory + Flyway integration tests
  covering all repository methods (depends on T012)

### DI and Module Wiring

- [X] T014 Update `receipt/di/ReceiptModule.java` — add
  `bind(PaymentProofRepository.class).to(JdbcPaymentProofRepository.class)` and
  `bind(PaymentProofStorageService.class).to(PaymentProofStorageServiceImpl.class)` (depends on T011, T012)
- [X] T015 Update `src/main/java/module-info.java` — open `receipt.domain.repository`, `receipt.domain.service`,
  `receipt.infrastructure.service` to `com.google.guice`; open `receipt.ui.component` to `javafx.fxml` and
  `com.google.guice` (depends on T011, T012)

**Checkpoint**: Foundation complete — domain types compile, DB table exists, fakes are ready, DI is wired.

---

## Phase 3: User Story 1 — Attach Proof of Payment to a Receipt (Priority: P1) 🎯 MVP

**Goal**: A user can attach one or more PDF or image files to a receipt via drag-drop, file picker, or clipboard paste.
Files are copied to app storage and associated with the receipt on save.

**Independent Test**: Create a new receipt, attach a PDF and an image using each method (drag-drop, click-to-browse,
CTRL+V paste), save, and confirm the files exist in the app `proofs/` directory and are associated with the receipt in
the DB.

### Application Layer

- [X] T016 [P] [US1] Create `AttachPaymentProofFromFileInput` record in
  `receipt/application/input/AttachPaymentProofFromFileInput.java` with fields
  `(Long receiptId, Path sourceFile, String originalFileName)`
- [X] T017 [P] [US1] Create `AttachPaymentProofFromBytesInput` record in
  `receipt/application/input/AttachPaymentProofFromBytesInput.java` with fields
  `(Long receiptId, byte[] imageBytes, String originalFileName)`
- [X] T018 [P] [US1] Create `AttachPaymentProofOutput` record in
  `receipt/application/output/AttachPaymentProofOutput.java` wrapping the created `PaymentProof`
- [X] T019 [US1] Create `AttachPaymentProofInteractor` in
  `receipt/application/usecase/AttachPaymentProofInteractor.java` — verify receipt exists, validate extension via
  `ProofFileType.fromExtension`, call storage service, call `PaymentProof.create`, persist via repository, return
  output (depends on T016, T017, T018)
- [X] T020 [P] [US1] Create `AttachPaymentProofInteractorTest` in
  `receipt/application/usecase/AttachPaymentProofInteractorTest.java` — covers file attach, bytes attach, unsupported
  extension rejection, and receipt-not-found case (depends on T019, T009, T010)
- [X] T021 [P] [US1] Create `FindPaymentProofsByReceiptIdInput` record in
  `receipt/application/input/FindPaymentProofsByReceiptIdInput.java` and `FindPaymentProofsByReceiptIdOutput` record in
  `receipt/application/output/FindPaymentProofsByReceiptIdOutput.java`
- [X] T022 [US1] Create `FindPaymentProofsByReceiptIdInteractor` in
  `receipt/application/usecase/FindPaymentProofsByReceiptIdInteractor.java` — verify receipt exists, return
  `proofRepository.findAllByReceiptId(receiptId)` (depends on T021)
- [X] T023 [P] [US1] Create `FindPaymentProofsByReceiptIdInteractorTest` in
  `receipt/application/usecase/FindPaymentProofsByReceiptIdInteractorTest.java` (depends on T022, T009)

### UI Layer

- [X] T024 [US1] Create `ProofDropZonePane` in `receipt/ui/component/ProofDropZonePane.java` — `VBox`-based component
  with: styled drop zone label, `DRAG_OVER`/`DRAG_DROPPED` handlers for file drops, `MOUSE_CLICKED` handler opening
  `FileChooser` (PDF + image filter), `ListView` of staged/existing proofs (icon + filename + remove button), inline
  error label for unsupported types, inline warning for duplicates, `getPendingFilesToAttach()` returning
  `List<PendingProof>`, `getProofsToRemove()` returning `List<Long>`, `loadExistingProofs(List<PaymentProof>)` method,
  and `handleClipboardPaste()` method (depends on T006)
- [X] T025 [US1] Update `receipt/ui/controller/ReceiptFormController.java` — inject `AttachPaymentProofInteractor`,
  `FindPaymentProofsByReceiptIdInteractor`, and `Provider<ProofDropZonePane>`; add drop zone pane to form layout; in
  edit mode load existing proofs on background thread and populate drop zone; add scene-level CTRL+V handler delegating
  to `dropZonePane.handleClipboardPaste()`; in save flow call `attachProof.execute()` for each pending file after
  receipt is saved (depends on T019, T022, T024)
- [X] T026 [P] [US1] Add i18n keys to `src/main/resources/messages.properties`: `receipt.form.proof.section`,
  `receipt.form.proof.drop_zone`, `receipt.form.proof.error.unsupported_type`, `receipt.form.proof.error.duplicate`,
  `receipt.form.proof.dialog.title`

**Checkpoint**: US1 is fully functional. Users can attach proofs to a receipt. The proof button in the list remains
disabled for all receipts (US2 not yet implemented).

---

## Phase 4: User Story 2 — View a Proof of Payment from the Receipts List (Priority: P2)

**Goal**: Each row in the receipts list has a "Comprovantes" button. The button is disabled when no proof exists and
enabled when at least one proof is attached. Clicking it opens the file using the OS default application; if multiple
proofs exist, a selection dialog is shown first.

**Independent Test**: View the receipts list; confirm that receipts with proofs show an enabled button and those without
show a disabled button. Click an enabled single-proof button — the file opens. Click an enabled multi-proof button — the
selection dialog appears, selecting a proof opens it. Confirm a missing-file error is shown when the stored file is not
found on disk.

### Application Layer

- [X] T027 [P] [US2] Create `GetReceiptProofCountsInput` record in
  `receipt/application/input/GetReceiptProofCountsInput.java` with field `List<Long> receiptIds` and
  `GetReceiptProofCountsOutput` record in `receipt/application/output/GetReceiptProofCountsOutput.java` wrapping
  `Map<Long, Integer>`
- [X] T028 [US2] Create `GetReceiptProofCountsInteractor` in
  `receipt/application/usecase/GetReceiptProofCountsInteractor.java` — delegates to
  `proofRepository.countByReceiptIds(receiptIds)` and returns output (depends on T027)

### UI Layer

- [X] T029 [US2] Create `ProofSelectionDialog` in `receipt/ui/controller/ProofSelectionDialog.java` —
  `Dialog<PaymentProof>` or `ChoiceDialog<PaymentProof>` subclass with custom cell factory showing file-type icon and
  original filename for each proof entry
- [X] T030 [US2] Update `receipt/ui/controller/ReceiptListController.java` — inject `GetReceiptProofCountsInteractor`
  and `FindPaymentProofsByReceiptIdInteractor`; after each page load dispatch async `Task` calling
  `countInteractor.execute(pageReceiptIds)` to populate `Set<Long> receiptsWithProofs`; add `hasProofs(Long receiptId)`
  helper; in `ActionsCell.updateItem()` add `proofsBtn` (label from `receipt.list.button.proofs`) bound to
  `hasProofs(receipt.getId())`; on button click: if single proof call `handleOpenProof(proof)` directly, if multiple
  show `ProofSelectionDialog` then `handleOpenProof(selected)`; `handleOpenProof` resolves the file path via storage
  service and calls `Desktop.getDesktop().open(file)` wrapped in a `Task`; show inline error alert for missing files (
  depends on T028, T029, T022)
- [X] T031 [P] [US2] Add i18n keys to `src/main/resources/messages.properties`: `receipt.list.button.proofs`,
  `receipt.list.proof.error.file_not_found`

**Checkpoint**: US1 + US2 are fully functional. Users can attach and view proofs. Removal of wrong files is not yet
supported.

---

## Phase 5: User Story 3 — Remove a Proof of Payment (Priority: P3)

**Goal**: A user can remove individual proofs while editing a receipt. The file is deleted from app storage and the DB
record is removed on save. After saving with all proofs removed, the list button for that receipt becomes disabled.

**Independent Test**: Open a receipt with an attached proof, click the remove button for that proof in the drop zone,
save, and confirm the list proof button is now disabled and the file is no longer in the `proofs/` directory.

### Application Layer

- [X] T032 [P] [US3] Create `RemovePaymentProofInput` record in `receipt/application/input/RemovePaymentProofInput.java`
  with fields `(Long receiptId, Long proofId)`
- [X] T033 [US3] Create `RemovePaymentProofInteractor` in
  `receipt/application/usecase/RemovePaymentProofInteractor.java` — load proofs for receipt, find proof by id (throw
  NOT_FOUND if missing), call `storageService.delete(proof.getStoredFileName())`, call
  `proofRepository.delete(proofId)` (depends on T032)
- [X] T034 [P] [US3] Create `RemovePaymentProofInteractorTest` in
  `receipt/application/usecase/RemovePaymentProofInteractorTest.java` — covers successful removal, not-found case, and
  file-deletion call (depends on T033, T009, T010)
- [X] T035 [US3] Update `receipt/application/usecase/DeleteReceiptInteractor.java` — before deleting the receipt: load
  proofs via `proofRepository.findAllByReceiptId(receiptId)`, call `storageService.delete(proof.getStoredFileName())`
  for each, then call `proofRepository.deleteAllByReceiptId(receiptId)`, then delete receipt as before (depends on T033)

### UI Layer

- [X] T036 [US3] Update `receipt/ui/controller/ReceiptFormController.java` save flow — after saving the receipt, iterate
  `dropZonePane.getProofsToRemove()` and call `removeProof.execute(new RemovePaymentProofInput(receiptId, proofId))` for
  each ID before calling attach interactors (depends on T033, T025)

**Checkpoint**: All three user stories are fully functional. The feature is complete.

---

## Phase 6: Polish & Cross-Cutting Concerns

- [X] T037 [P] Review `src/main/resources/messages_pt_BR.properties` (if present) and add matching PT-BR translations
  for all new i18n keys added in T026 and T031
- [ ] T038 Run manual validation scenarios from `specs/004-receipt-payment-proofs/quickstart.md` — attach PDF via drop,
  attach image via file picker, attach image via CTRL+V paste, open from list (single and multiple proofs), remove proof
  and verify button disables

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Setup)**: No dependencies — start immediately
- **Phase 2 (Foundational)**: Depends on T001 — BLOCKS all user stories
- **Phase 3 (US1)**: Depends on all of Phase 2
- **Phase 4 (US2)**: Depends on all of Phase 2; integrates with US1 interactors (T022)
- **Phase 5 (US3)**: Depends on all of Phase 2; integrates with US1 UI (T025)
- **Phase 6 (Polish)**: Depends on all user story phases complete

### User Story Dependencies

- **US1 (P1)**: Starts after Phase 2 — no dependency on US2 or US3
- **US2 (P2)**: Starts after Phase 2 — reuses `FindPaymentProofsByReceiptIdInteractor` from US1 (T022); US1 must be done
  first
- **US3 (P3)**: Starts after Phase 2 — adds removal wiring to `ReceiptFormController` built in US1 (T025)

### Within Phase 2 (Foundational)

Sequential constraints:

- T002 → T003 → T004, T006
- T005 (independent)
- T007 (independent)
- T003 → T008, T009
- T005 → T010
- T005 + T007 → T011
- T004 → T012 → T013
- T011 + T012 → T014 → T015

### Parallel Opportunities

**Within Phase 2**:

```
T002  →  T003  →  T004 [P]   →  T012 → T013
                  T006 [P]
T005 [P]         T009 [P]
T007 [P]         T010 [P]
                 T008 [P]
                 T011
```

After T003: T004, T006, T008, T009 can all run in parallel.  
After T004: T009, T012 can run in parallel.

**Within Phase 3 (US1)**:

```
T016 [P]  \
T017 [P]   → T019 → T020 [P]
T018 [P]  /
T021 [P]    → T022 → T023 [P]
T024        → T025 → T026 [P]
```

T016, T017, T018, T021, T024 can all start in parallel.

**Within Phase 4 (US2)**:

```
T027 [P] → T028
T029 [P]           → T030
T031 [P]
```

T027, T029, T031 can all start in parallel.

**Within Phase 5 (US3)**:

```
T032 [P] → T033 → T034 [P]
                   T035
                   T036
```

---

## Implementation Strategy

### MVP Scope (User Story 1 Only)

1. Complete Phase 1 (T001)
2. Complete Phase 2 (T002–T015) — CRITICAL
3. Complete Phase 3 (T016–T026) — US1 fully functional
4. **STOP and VALIDATE**: Attach proofs via all three input methods; confirm files persist

### Incremental Delivery

1. Phase 1 + Phase 2 → Foundation ready
2. Phase 3 → Attach proofs works (MVP)
3. Phase 4 → View proofs from list works
4. Phase 5 → Remove proofs works (full feature)
5. Phase 6 → Polish and i18n complete

---

## Notes

- [P] tasks operate on different files with no pending dependencies — safe to run concurrently
- [USn] label maps each task to its user story for traceability and independent delivery
- Each user story phase produces a working, independently testable increment
- No test TDD gate is enforced — tests are included but may be written after or alongside implementation
- The `ProofDropZonePane` remove buttons (T024) stage removals locally; actual deletion only executes on form save (
  T036)
- `Receipt.restore()` is never modified — only a new `restoreWithProofs()` overload is added (T006)
