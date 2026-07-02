# Tasks: Monthly Receipts PDF Export

**Input**: Design documents from `/specs/007-monthly-receipts-pdf-export/`
**Prerequisites**: plan.md ✅, spec.md ✅, data-model.md ✅, research.md ✅, contracts/receipt-export.md ✅, quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no blocking dependencies)
- **[Story]**: Which user story this task belongs to (US1 = Export All Receipts for a Month, US2 = Overwrite
  Conflicting Files)
- Exact file paths are included in every task description

## Path Conventions

Java source root: `src/main/java/com/guilherme/emobiliaria/`
Test root: `src/test/java/com/guilherme/emobiliaria/`
Resources root: `src/main/resources/`

---

## Phase 1: Setup (Domain Layer)

**Purpose**: Create all pure-Java domain-layer artifacts this feature needs — the new error message, exception,
value object, and the two interface extensions. No framework, persistence, or UI dependencies.

- [X] T001 [P] Add `EXPORT_FOLDER_NOT_WRITABLE("receipt.export.folder_not_writable", "Destination folder is not
  writable")` to the `Receipt` enum in `ErrorMessage.java`, and create `ExportFolderNotWritableException extends
  BusinessException` (constructor `ExportFolderNotWritableException()` calling
  `super(ErrorMessage.Receipt.EXPORT_FOLDER_NOT_WRITABLE)`, following the `NoDrivesFoundException` pattern) in
  `src/main/java/com/guilherme/emobiliaria/shared/exception/ErrorMessage.java` and
  `src/main/java/com/guilherme/emobiliaria/shared/exception/ExportFolderNotWritableException.java`
- [X] T002 [P] Add `receipt.export.folder_not_writable=Destination folder is not writable` to
  `src/main/resources/messages.properties` and `receipt.export.folder_not_writable=Pasta de destino não permite
  escrita` to `src/main/resources/messages_pt_BR.properties` (actual UTF-8 characters, no unicode escapes)
- [X] T003 [P] Create `ReceiptExportResult` record (`int exportedCount`, `List<FailedExport> failures`, with nested
  `record FailedExport(Long receiptId, String reason)`) per data-model.md in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/entity/ReceiptExportResult.java`
- [X] T004 [P] Add `List<YearMonth> findAllReceiptMonths()` and `List<Receipt> findAllByMonth(YearMonth month)` method
  signatures to the `ReceiptRepository` interface in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/repository/ReceiptRepository.java`
- [X] T005 [P] Add `String defaultFileName(Receipt receipt)` method signature to the `ReceiptFileService` interface in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/service/ReceiptFileService.java`
- [X] T006 [P] Create the `ReceiptExportService` domain interface with
  `void writePdf(Path folder, String fileName, byte[] pdfBytes)` (throws `ExportFolderNotWritableException`) in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/service/ReceiptExportService.java`

**Checkpoint**: Pure domain layer compiles — no infrastructure, application, or UI code depends on these yet

---

## Phase 2: Foundational (Infrastructure, DI, and Test Fakes)

**Purpose**: Implement the real and fake sides of every interface added in Phase 1, and wire the new real
implementation into Guice. This is shared by both user stories and must be complete before either is built.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T007 [P] Implement `findAllReceiptMonths()` (`SELECT DISTINCT FORMATDATETIME(date, 'yyyy-MM') AS ym FROM
  receipts ORDER BY ym DESC`, parsed into `YearMonth`) and `findAllByMonth(YearMonth month)` (`date BETWEEN ? AND ?`
  using `month.atDay(1)` / `month.atEndOfMonth()`, reusing the existing `map(rs, conn)` mapper) in
  `JdbcReceiptRepository`, per research.md §3, in
  `src/main/java/com/guilherme/emobiliaria/receipt/infrastructure/repository/JdbcReceiptRepository.java`
- [X] T008 [P] Implement `defaultFileName(Receipt receipt)` in `ReceiptFileServiceImpl` returning
  `"Recibo_" + date-as-ddMMyyyy + "_" + sanitized-tenant-name + ".pdf"`, where the tenant name comes from
  `TemplateFormatter.personName(receipt.getContract().getTenants().getFirst())` with whitespace replaced by `_` and
  `\/:*?"<>|` characters stripped, per research.md §1, in
  `src/main/java/com/guilherme/emobiliaria/receipt/infrastructure/service/ReceiptFileServiceImpl.java`
- [X] T009 [P] Create `FileSystemReceiptExportService implements ReceiptExportService`: pre-flight check
  `Files.isWritable(folder)` and throw `ExportFolderNotWritableException` if false; otherwise
  `Files.write(folder.resolve(fileName), pdfBytes, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)`,
  catching `IOException`/`AccessDeniedException` and rethrowing as `ExportFolderNotWritableException`, per
  research.md §4 and contracts/receipt-export.md, in
  `src/main/java/com/guilherme/emobiliaria/receipt/infrastructure/service/FileSystemReceiptExportService.java`
- [X] T010 [P] Implement `findAllReceiptMonths()` (distinct `YearMonth.from(r.getDate())` values from the in-memory
  store, sorted descending) and `findAllByMonth(YearMonth month)` (filter store by
  `YearMonth.from(r.getDate()).equals(month)`) in `FakeReceiptRepository`, calling `maybeFail()` at the start of
  each, in
  `src/test/java/com/guilherme/emobiliaria/receipt/domain/repository/FakeReceiptRepository.java`
- [X] T011 [P] Create `FakeReceiptExportService implements ReceiptExportService` extending `FakeImplementation`: keep
  an in-memory `Map<Path, byte[]>` keyed by `folder.resolve(fileName)`, `writePdf` calls `maybeFail()` then stores the
  bytes (overwriting any existing entry for that path), plus a package-visible accessor (e.g.
  `Map<Path, byte[]> writtenFiles()`) tests can use to assert what was written, in
  `src/test/java/com/guilherme/emobiliaria/receipt/domain/service/FakeReceiptExportService.java`
- [X] T012 Bind `ReceiptExportService` to `FileSystemReceiptExportService` in
  `src/main/java/com/guilherme/emobiliaria/receipt/di/ReceiptModule.java` (depends on T009)
- [X] T013 [P] Create `JdbcReceiptRepositoryTest` (H2 in-memory + Flyway migration, following the
  `JdbcPaymentProofRepositoryTest` setup pattern) covering: `findAllReceiptMonths()` returns distinct months ordered
  most-recent-first; `findAllByMonth(YearMonth)` returns exactly the receipts whose `date` falls within the month,
  including receipts on the first and last day of the month (Acceptance Scenario 5) and excluding receipts in
  adjacent months, in
  `src/test/java/com/guilherme/emobiliaria/receipt/infrastructure/repository/JdbcReceiptRepositoryTest.java`
  (depends on T007)
- [X] T014 [P] Create `ReceiptFileServiceImplTest` covering `defaultFileName(Receipt)` for a physical-person tenant,
  a juridical-person tenant, and a tenant name containing spaces/accented characters, asserting the sanitized,
  `.pdf`-suffixed result, in
  `src/test/java/com/guilherme/emobiliaria/receipt/infrastructure/service/ReceiptFileServiceImplTest.java`
  (depends on T008)

**Checkpoint**: All infrastructure, DI wiring, and test fakes are in place — application-layer and UI work can begin

---

## Phase 3: User Story 1 - Export All Receipts for a Month (Priority: P1) 🎯 MVP

**Goal**: A user clicks "Export Month" on the receipts page, picks a month (from months that actually have
receipts) and a destination folder, confirms, and gets one PDF per matching receipt saved into that folder, with a
completion summary reporting how many were exported and which (if any) failed.

**Independent Test**: Create several receipts with receipt dates in the same month (and at least one in a
different month), trigger the export for that month, and confirm only the matching receipts' PDFs appear in the
chosen folder with a correct completion count.

### Implementation for User Story 1

- [X] T015 [P] [US1] Create `ExportReceiptsByMonthInput` record (`YearMonth month`, `Path destinationFolder`) in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/input/ExportReceiptsByMonthInput.java`
- [X] T016 [P] [US1] Create `ExportReceiptsByMonthOutput` record (`ReceiptExportResult result`) and
  `GetExportableReceiptMonthsOutput` record (`List<YearMonth> months`) in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/output/ExportReceiptsByMonthOutput.java` and
  `src/main/java/com/guilherme/emobiliaria/receipt/application/output/GetExportableReceiptMonthsOutput.java`
- [X] T017 [US1] Create `GetExportableReceiptMonthsInteractor` with a parameterless `execute()` returning
  `GetExportableReceiptMonthsOutput` from `receiptRepository.findAllReceiptMonths()` in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/usecase/GetExportableReceiptMonthsInteractor.java`
  (depends on T004, T016)
- [X] T018 [US1] Create `ExportReceiptsByMonthInteractor.execute(ExportReceiptsByMonthInput)`: load
  `receiptRepository.findAllByMonth(input.month())`; for each receipt, wrap
  `receiptFileService.generateReceiptPdf(receipt)` +
  `receiptExportService.writePdf(input.destinationFolder(), receiptFileService.defaultFileName(receipt), pdfBytes)`
  in a try/catch — on `ExportFolderNotWritableException` let it propagate immediately (whole operation aborts, per
  FR-010); on any other `RuntimeException`, record a `ReceiptExportResult.FailedExport(receipt.getId(),
  e.getMessage())` and continue to the next receipt; return `ExportReceiptsByMonthOutput` wrapping a
  `ReceiptExportResult` with the success count and failure list, per research.md §4 and
  contracts/receipt-export.md, in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/usecase/ExportReceiptsByMonthInteractor.java`
  (depends on T004, T005, T006, T015, T016)
- [X] T019 [P] [US1] Create `GetExportableReceiptMonthsInteractorTest` using `FakeReceiptRepository`, covering: months
  with at least one receipt are returned; a month with zero receipts is not present; empty repository yields an
  empty list (not an error), in
  `src/test/java/com/guilherme/emobiliaria/receipt/application/usecase/GetExportableReceiptMonthsInteractorTest.java`
  (depends on T010, T017)
- [X] T020 [P] [US1] Create `ExportReceiptsByMonthInteractorTest` using `FakeReceiptRepository`,
  `FakeReceiptFileService`, and `FakeReceiptExportService`, covering: happy path (N receipts in the month all
  exported, `exportedCount` matches, `FakeReceiptExportService.writtenFiles()` has one entry per receipt); a
  `FakeReceiptFileService.failNext(...)` on one receipt is skipped and reported in `failures` while the rest still
  export; a `FakeReceiptExportService.failNext(() -> new ExportFolderNotWritableException())` aborts the whole
  export with no partial `ReceiptExportResult` returned (exception propagates); receipts outside the selected month
  are never touched, in
  `src/test/java/com/guilherme/emobiliaria/receipt/application/usecase/ExportReceiptsByMonthInteractorTest.java`
  (depends on T010, T011, T018)
- [X] T021 [US1] Add an `fx:id="exportMonthButton" styleClass="btn-secondary"` `Button` next to `newButton` inside
  the `HBox styleClass="list-view-header-row"` in
  `src/main/resources/com/guilherme/emobiliaria/receipt/ui/view/receipt-list-view.fxml`
- [X] T022 [US1] Add i18n keys `receipt.list.button.export_month`, `receipt.export.dialog.title`,
  `receipt.export.dialog.month_label`, `receipt.export.dialog.folder_label`,
  `receipt.export.dialog.choose_folder_button`, `receipt.export.dialog.no_folder_selected`,
  `receipt.export.dialog.confirm_button`, `receipt.export.result.title`, `receipt.export.result.message`
  (formatted with exported count), `receipt.export.result.failures_suffix` (formatted with failure count/list) to
  `src/main/resources/messages.properties` and their translated counterparts to
  `src/main/resources/messages_pt_BR.properties`
- [X] T023 [US1] Create `ExportReceiptsDialog extends Dialog<ExportReceiptsDialog.Result>` (nested
  `record Result(YearMonth month, Path destinationFolder)`), constructed with `(List<YearMonth> months,
  ResourceBundle bundle)`: a `ComboBox<YearMonth>` populated from `months`, a "Choose folder" `Button` that opens a
  `javafx.stage.DirectoryChooser` (per research.md §2) and displays the chosen path in a `Label`, and OK/Cancel
  `ButtonType`s where OK is disabled until both a month and a folder are selected; `setResultConverter` returns a
  `Result` only when OK is clicked, in
  `src/main/java/com/guilherme/emobiliaria/receipt/ui/controller/ExportReceiptsDialog.java`
- [X] T024 [US1] In `ReceiptListController`: add `GetExportableReceiptMonthsInteractor` and
  `ExportReceiptsByMonthInteractor` as `@Inject` constructor parameters; add `@FXML private Button
  exportMonthButton` and set its text from `receipt.list.button.export_month` in `initialize()`; wire
  `exportMonthButton.setOnAction` to run `GetExportableReceiptMonthsInteractor` on a background `Task`, then on
  success show `ExportReceiptsDialog` and, if the user confirms, run `ExportReceiptsByMonthInteractor` on a
  background `Task` — on success show an `Alert` summarizing `exportedCount` and any `failures` (receipt IDs +
  reasons); on `ExportFolderNotWritableException` show an error `Alert` via `ErrorHandler.handle`, in
  `src/main/java/com/guilherme/emobiliaria/receipt/ui/controller/ReceiptListController.java` (depends on T017,
  T018, T021, T022, T023)

**Checkpoint**: User Story 1 is fully functional and independently testable — clicking "Export Month", picking a
month and folder, and confirming produces the correct PDFs with a completion summary

---

## Phase 4: User Story 2 - Overwrite Conflicting Files (Priority: P2)

**Goal**: Re-running an export into a folder that already contains files from a previous export (or unrelated
files) overwrites only the name-conflicting files, silently and without prompting, leaving everything else intact.

**Independent Test**: Export a month's receipts into a folder, drop an unrelated file into that folder, re-export
the same month into the same folder, and confirm the receipt PDFs are refreshed, no duplicate/`(1)`-suffixed files
appear, and the unrelated file is untouched.

### Implementation for User Story 2

- [X] T025 [US2] Create `FileSystemReceiptExportServiceTest` using a JUnit `@TempDir`, covering: writing a PDF to a
  folder that already contains a file with the same name overwrites it (content matches the new bytes, no second
  file is created); an unrelated file placed in the same folder before a `writePdf` call is untouched afterward;
  calling `writePdf` against a folder with no write permission throws `ExportFolderNotWritableException` (e.g. via
  `Files.setPosixFilePermissions` to strip write access, or `File.setWritable(false)` on platforms where that is
  supported by the test runner), in
  `src/test/java/com/guilherme/emobiliaria/receipt/infrastructure/service/FileSystemReceiptExportServiceTest.java`
  (depends on T009)

**Checkpoint**: Both user stories are independently functional — overwrite behavior is verified at the
infrastructure layer where the actual filesystem write happens

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: End-to-end validation of the whole feature as a running app.

- [ ] T026 Run quickstart.md manual verification end-to-end: User Story 1 steps (export a month, verify file names
  and PDF contents), User Story 2 steps (re-export, unrelated file untouched, edited receipt reflected), and Edge
  Cases (unwritable folder shows a clear error, mid-export deletion is skipped gracefully, cancel produces no
  files)
- [X] T027 Run `mvn test` to confirm the full suite (including all tests added in this feature) passes with no
  regressions

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — all six tasks (T001–T006) touch different files and can run in parallel
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS both user stories; T007–T011 depend on their
  respective Phase 1 interface (T007→T004, T008→T005, T009→T006/T001, T010→T004, T011→T006); T012 depends on T009;
  T013 depends on T007; T014 depends on T008
- **User Story 1 (Phase 3)**: Depends on Foundational completion; T015/T016 have no internal dependencies; T017
  depends on T004+T016; T018 depends on T004/T005/T006/T015/T016; T019 depends on T010+T017; T020 depends on
  T010/T011/T018; T021/T022 have no internal dependencies; T023 depends on T022; T024 depends on T017/T018/T021/
  T022/T023
- **User Story 2 (Phase 4)**: Depends on Foundational completion (specifically T009); independent of User Story 1's
  application/UI work — can be built in parallel with Phase 3 once Phase 2 is done
- **Polish (Phase 5)**: Depends on both user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational (Phase 2) — no dependency on US2
- **US2 (P2)**: Can start after Foundational (Phase 2) — its only implementation task (T025) exercises
  `FileSystemReceiptExportService` directly and does not depend on the US1 application/UI layer, though in practice
  the overwrite behavior it verifies is what US1's `ExportReceiptsByMonthInteractor` relies on at runtime

### Parallel Opportunities

- T001–T006 (Phase 1 — different files, no internal deps)
- T007, T008, T009, T010, T011 (Phase 2 — different files, no internal deps beyond their Phase 1 interface)
- T013, T014 (Phase 2 — test files, independent of each other)
- T015, T016 (Phase 3 — different files, no internal deps)
- T019, T020 (Phase 3 — test files, independent of each other)
- T025 (Phase 4) can run concurrently with any Phase 3 task once Phase 2 is complete

---

## Parallel Example: Phase 2 (Foundational)

```bash
# These five tasks touch different files and have no dependencies on each other:
Task T007: "Implement findAllReceiptMonths()/findAllByMonth() in JdbcReceiptRepository"
Task T008: "Implement defaultFileName(Receipt) in ReceiptFileServiceImpl"
Task T009: "Create FileSystemReceiptExportService"
Task T010: "Implement findAllReceiptMonths()/findAllByMonth() in FakeReceiptRepository"
Task T011: "Create FakeReceiptExportService"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (domain layer)
2. Complete Phase 2: Foundational (infrastructure, DI, fakes) — CRITICAL, blocks all user stories
3. Complete Phase 3: User Story 1 (application layer + dialog + controller wiring)
4. **STOP and VALIDATE**: Export a month's receipts through the running app and confirm the PDFs and completion
   summary are correct
5. Deliver bulk export capability

### Incremental Delivery

1. Complete Setup + Foundational → domain/infra/DI compiles, no user-visible change yet
2. Add User Story 1 → export works end-to-end → validate independently → demo (MVP!)
3. Add User Story 2 → overwrite behavior verified at the infrastructure layer → validate independently → demo
4. Polish → quickstart.md walkthrough + full test suite

---

## Notes

- [P] tasks operate on different files with no blocking dependencies and can be worked concurrently
- [US1]/[US2] labels map each task to its user story for traceability
- Test tasks follow this project's existing conventions (`docs/test.md`, `docs/application-layer.md`): JUnit 5,
  `@Nested`/`@DisplayName`, Fake implementations over mocks, one test class per method group
- `ExportReceiptsDialog` (T023) is built programmatically like the existing `ProofSelectionDialog` — no new FXML
  file, and it receives already-fetched data (`List<YearMonth>`) rather than an injected interactor, consistent
  with how `ProofSelectionDialog` receives a `List<PaymentProof>`
- No file is written until the user confirms the dialog (FR-011) — `ExportReceiptsByMonthInteractor` (T018) is only
  invoked from `ReceiptListController` (T024) after `ExportReceiptsDialog.showAndWait()` returns a present `Result`
- The overwrite behavior required by User Story 2 has no separate production code path — it is the
  `StandardOpenOption.TRUNCATE_EXISTING` write mode already implemented in T009 as part of Foundational; Phase 4's
  task exists to independently verify that behavior at the filesystem level, per the spec's independent-test
  requirement for US2
