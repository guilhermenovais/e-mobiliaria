---
description: "Task list for Payment Report feature implementation"
---

# Tasks: Payment Report

**Input**: Design documents from `/specs/002-payment-report/`
**Prerequisites**: plan.md ✅ spec.md ✅ research.md ✅ data-model.md ✅ quickstart.md ✅

**Organization**: Tasks are grouped by user story. The foundational phase covers all pure-Java domain and application
layer
pieces that both user stories share. No setup phase is needed — this feature extends the existing `reports` module with
no new top-level packages, no DI module changes, and no schema migrations.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies on incomplete tasks in the same phase)
- **[Story]**: Which user story this task belongs to (US1, US2)

---

## Phase 1: Foundational (Domain + Application Layer)

**Purpose**: Pure-Java building blocks shared by both user stories. No UI, no JDBC. Must be complete before any
infrastructure or UI work begins.

**⚠️ CRITICAL**: US1 and US2 implementation cannot begin until this phase is complete.

- [X] T001 Create `PaymentReportRowStatus.java` enum (`PAID`, `UNPAID`, `VACANT`) in
  `src/main/java/com/guilherme/emobiliaria/reports/domain/entity/PaymentReportRowStatus.java`
- [X] T002 Create `PaymentReportRow.java` record (fields: propertyName, primaryTenantName, primaryTenantTaxId,
  paymentDate, rent, periodStart, periodEnd, status) in
  `src/main/java/com/guilherme/emobiliaria/reports/domain/entity/PaymentReportRow.java`
- [X] T003 Extend `ReportRepository.java` interface with two new method signatures:
  `List<YearMonth> loadPaymentReportMonths()` and `List<PaymentReportRow> loadPaymentReportData(YearMonth month)` in
  `src/main/java/com/guilherme/emobiliaria/reports/domain/repository/ReportRepository.java`
- [X] T004 Extend `ReportFileService.java` interface with one new method signature:
  `byte[] generatePaymentReportPdf(List<PaymentReportRow> rows, YearMonth month)` in
  `src/main/java/com/guilherme/emobiliaria/reports/domain/service/ReportFileService.java`
- [X] T005 [P] Create `GetPaymentReportMonthsInput.java` empty record in
  `src/main/java/com/guilherme/emobiliaria/reports/application/input/GetPaymentReportMonthsInput.java`
- [X] T006 [P] Create `GetPaymentReportMonthsOutput.java` record (field: `List<YearMonth> months`) in
  `src/main/java/com/guilherme/emobiliaria/reports/application/output/GetPaymentReportMonthsOutput.java`
- [X] T007 [P] Create `LoadPaymentReportInput.java` record (field: `YearMonth month`) in
  `src/main/java/com/guilherme/emobiliaria/reports/application/input/LoadPaymentReportInput.java`
- [X] T008 [P] Create `LoadPaymentReportOutput.java` record (field: `List<PaymentReportRow> rows`) in
  `src/main/java/com/guilherme/emobiliaria/reports/application/output/LoadPaymentReportOutput.java`
- [X] T009 [P] Create `GeneratePaymentReportPdfInput.java` record (field: `YearMonth month`) in
  `src/main/java/com/guilherme/emobiliaria/reports/application/input/GeneratePaymentReportPdfInput.java`
- [X] T010 [P] Create `GeneratePaymentReportPdfOutput.java` record (field: `byte[] pdfBytes`) in
  `src/main/java/com/guilherme/emobiliaria/reports/application/output/GeneratePaymentReportPdfOutput.java`
- [X] T011 [P] Create `GetPaymentReportMonthsInteractor.java` with `@Inject` constructor taking `ReportRepository`;
  `execute()` delegates to `reportRepository.loadPaymentReportMonths()` in
  `src/main/java/com/guilherme/emobiliaria/reports/application/usecase/GetPaymentReportMonthsInteractor.java`
- [X] T012 [P] Create `LoadPaymentReportInteractor.java` with `@Inject` constructor taking `ReportRepository`;
  `execute()` delegates to `reportRepository.loadPaymentReportData(input.month())` in
  `src/main/java/com/guilherme/emobiliaria/reports/application/usecase/LoadPaymentReportInteractor.java`
- [X] T013 [P] Create `GeneratePaymentReportPdfInteractor.java` with `@Inject` constructor taking `ReportRepository` and
  `ReportFileService`; `execute()` loads rows then calls `reportFileService.generatePaymentReportPdf(rows, month)` in
  `src/main/java/com/guilherme/emobiliaria/reports/application/usecase/GeneratePaymentReportPdfInteractor.java`
- [X] T014 Extend `FakeReportRepository.java` with stub implementations for `loadPaymentReportMonths()` (returns
  `List.of(YearMonth.now())`) and `loadPaymentReportData(YearMonth)` (returns `List.of()`) in
  `src/test/java/com/guilherme/emobiliaria/reports/domain/repository/FakeReportRepository.java`; also extend
  `FakeReportFileService.java` with stub for `generatePaymentReportPdf()` in
  `src/test/java/com/guilherme/emobiliaria/reports/domain/service/FakeReportFileService.java`

**Checkpoint**: All pure-Java domain and application layer pieces compile. `FakeReportRepository` and
`FakeReportFileService` are ready for unit tests. Infrastructure and UI work can now begin in parallel.

---

## Phase 2: User Story 1 — Generate Payment Report for a Month (Priority: P1) 🎯 MVP

**Goal**: Property manager selects a month and sees the full payment status table with correct columns, row colors
(red for UNPAID, light gray for VACANT), and can export a PDF.

**Independent Test**: Navigate to the Payment Report section; select a month from the dropdown (initially showing only
the current month); verify the table renders with one row per property showing all six columns with correct data and
visual styling; click Export PDF and verify a PDF opens.

### Tests for User Story 1

- [X] T015 [P] [US1] Write unit tests for `LoadPaymentReportInteractor` and `GeneratePaymentReportPdfInteractor` using
  `FakeReportRepository` and `FakeReportFileService` in
  `src/test/java/com/guilherme/emobiliaria/reports/application/usecase/LoadPaymentReportInteractorTest.java` and
  `GeneratePaymentReportPdfInteractorTest.java`
- [X] T016 [P] [US1] Write JDBC integration test for `loadPaymentReportData(YearMonth)` — seed properties, contracts,
  receipts in H2; assert correct PAID/UNPAID/VACANT rows, primary tenant resolution, date fields, ordering by property
  name — in `src/test/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepositoryTest.java` (
  add a new `@Nested` class `PaymentReportDataTests`)

### Implementation for User Story 1

- [X] T017 [US1] Implement `loadPaymentReportData(YearMonth month)` in `JdbcReportRepository.java` using the SQL query
  from `quickstart.md` (LEFT JOIN contracts active on first day, primary tenant via MIN ct.id, most recent receipt via
  MAX r.id, map rows to `PaymentReportRow` with correct PAID/UNPAID/VACANT status) in
  `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepository.java`
- [X] T018 [P] [US1] Create `PaymentReportRowBean.java` DTO with private final String fields (propertyName,
  primaryTenantName, primaryTenantTaxId, paymentDate, rent, period, statusLabel), all-arg constructor, and getters —
  null-safe, dates formatted as `"dd/MM/yyyy"`, money as `"R$ X.XXX,XX"`, empty string when null — in
  `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/service/PaymentReportRowBean.java`
- [X] T019 [US1] Implement `generatePaymentReportPdf(List<PaymentReportRow> rows, YearMonth month)` in
  `ReportFileServiceImpl.java`: map rows to `PaymentReportRowBean` list, format month as PT-BR label (e.g.
  `"maio/2026"`), pass `JRBeanCollectionDataSource` to `PdfGenerationService` loading `payment_report.jrxml` — in
  `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/service/ReportFileServiceImpl.java`
- [X] T020 [P] [US1] Create `payment_report.jrxml` JasperReports template with title
  `"Relatório de Pagamentos — $P{MONTH_LABEL}"`, a `MONTH_LABEL` string parameter, and a single detail band with six
  columns (Imóvel, Locatário, CPF/CNPJ, Data de Pagamento, Valor do Aluguel, Período) mapped to `PaymentReportRowBean`
  getters — in `src/main/resources/reports/payment_report.jrxml`
- [X] T021 [P] [US1] Create `payment-report-view.fxml` with a `VBox` root containing: a `ComboBox` for month selection,
  a `TableView` with six `TableColumn` items, an Export PDF `Button`, and a `ProgressIndicator` — in
  `src/main/resources/com/guilherme/emobiliaria/reports/ui/view/payment-report-view.fxml`
- [X] T022 [P] [US1] Create `payment-report-view.css` with styles for UNPAID rows (red text, `-fx-text-fill: #d32f2f`)
  and VACANT rows (light gray text, `-fx-text-fill: #9e9e9e`) applied via row factory CSS pseudo-classes or style
  classes — in `src/main/resources/com/guilherme/emobiliaria/reports/ui/view/payment-report-view.css`
- [X] T023 [US1] Create `PaymentReportController.java` with `@Inject` constructor taking
  `GetPaymentReportMonthsInteractor`, `LoadPaymentReportInteractor`, `GeneratePaymentReportPdfInteractor`, and
  `GuiceFxmlLoader`; `initialize()` populates ComboBox with `List.of(YearMonth.now())` as a placeholder (US2 will
  replace this with the real interactor call), adds a change listener to load table data, wires Export PDF button with
  async `Task<Void>` following the `ReportsController` pattern, applies `TableRow` cell factory for PAID/UNPAID/VACANT
  coloring — in `src/main/java/com/guilherme/emobiliaria/reports/ui/controller/PaymentReportController.java`
- [X] T024 [US1] Extend `ReportsController.java` to add a Payment Report card: inject `PaymentReportController`, add
  FXML labels/buttons for the payment report entry, and implement navigation that loads `payment-report-view.fxml` via
  `GuiceFxmlLoader` — in `src/main/java/com/guilherme/emobiliaria/reports/ui/controller/ReportsController.java` and
  `src/main/resources/com/guilherme/emobiliaria/reports/ui/view/reports-view.fxml`
- [X] T025 [P] [US1] Add payment report i18n keys to both `src/main/resources/messages.properties` and
  `src/main/resources/messages_pt_BR.properties` (keys: `reports.payment_report.name`,
  `reports.payment_report.description`, `reports.payment_report.month_selector.label`,
  `reports.payment_report.column.property`, `reports.payment_report.column.tenant`,
  `reports.payment_report.column.tax_id`, `reports.payment_report.column.payment_date`,
  `reports.payment_report.column.rent`, `reports.payment_report.column.period`, `reports.payment_report.vacant_label`,
  `reports.payment_report.button.generate_pdf`)

**Checkpoint**: At this point, User Story 1 is fully functional and testable. The payment report screen opens, displays
a table for the current month, colors rows correctly, and generates a PDF. Month selector shows only current month
(US2 will complete the historical range).

---

## Phase 3: User Story 2 — Browse Historical Months (Priority: P2)

**Goal**: The month selector is pre-populated with all months from the oldest contract's start month through the current
month, in descending order with no gaps. No-contracts edge case returns only the current month.

**Independent Test**: Open the Payment Report section; inspect the ComboBox contents and verify months span from current
month back to the earliest contract start date with no gaps, most recent first; verify the no-contracts edge case shows
only the current month.

### Tests for User Story 2

- [X] T026 [P] [US2] Write unit tests for `GetPaymentReportMonthsInteractor`: assert it delegates to
  `FakeReportRepository.loadPaymentReportMonths()` and wraps results in `GetPaymentReportMonthsOutput` — in
  `src/test/java/com/guilherme/emobiliaria/reports/application/usecase/GetPaymentReportMonthsInteractorTest.java`
- [X] T027 [P] [US2] Write JDBC integration test for `loadPaymentReportMonths()`: seed contracts with known start dates,
  assert returned list is descending, spans from oldest start month to current month with no gaps; assert empty database
  returns `List.of(YearMonth.now())` — add `@Nested` class `PaymentReportMonthsTests` in
  `src/test/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepositoryTest.java`

### Implementation for User Story 2

- [X] T028 [US2] Implement `loadPaymentReportMonths()` in `JdbcReportRepository.java`: run
  `SELECT MIN(start_date) AS earliest FROM contracts`; if null return `List.of(YearMonth.now())`; otherwise generate
  months from `YearMonth.from(earliest)` up to `YearMonth.now()` then reverse for descending order — in
  `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepository.java`
- [X] T029 [US2] Update `PaymentReportController.initialize()` to replace the hardcoded `List.of(YearMonth.now())`
  placeholder with a call to `GetPaymentReportMonthsInteractor.execute(new GetPaymentReportMonthsInput())`; add a
  `StringConverter<YearMonth>` on the ComboBox formatting months as PT-BR short form (e.g. `"mai/2026"`); set initial
  selection to first item (most recent month) — in
  `src/main/java/com/guilherme/emobiliaria/reports/ui/controller/PaymentReportController.java`

**Checkpoint**: Both user stories are independently functional. The month selector displays the full historical range
and the table loads correct data for any selected month.

---

## Phase 4: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and any remaining cross-cutting issues.

- [ ] T030 Run all quickstart.md validation scenarios manually: verify PAID/UNPAID/VACANT row coloring, verify all six
  columns have correct data, verify month selector ordering, verify no-contracts edge case, verify PDF exports correctly
  with PT-BR month label

---

## Dependencies & Execution Order

### Phase Dependencies

- **Foundational (Phase 1)**: No dependencies — can start immediately
- **US1 (Phase 2)**: Depends entirely on Phase 1 completion — **BLOCKS US1 infrastructure and UI**
- **US2 (Phase 3)**: Depends on Phase 1 completion; T029 also depends on T023 (PaymentReportController) from Phase 2
- **Polish (Phase 4)**: Depends on all story phases complete

### User Story Dependencies

- **US1**: Can start after Foundational (Phase 1). Independent of US2.
- **US2**: Can start after Foundational (Phase 1). T029 depends on T023 (controller exists), so US2 implementation
  is best done after US1 is complete.

### Within Each User Story

- Tests (T015, T016 for US1; T026, T027 for US2) should be written and verified to fail before their corresponding
  JDBC implementations
- T018 (PaymentReportRowBean) before T019 (PDF service implementation)
- T019 (PDF service) and T020 (JRXML template) before T023 (controller PDF wiring)
- T021 (FXML) and T022 (CSS) before T023 (PaymentReportController)
- T023 (controller) before T024 (ReportsController navigation)

### Parallel Opportunities

Within Foundational Phase:

- T001–T004 must be sequential (T002→T001, T003→T002, T004→T002)
- T005–T013 can all run in parallel once T003 and T004 are done
- T014 can run in parallel with T005–T013

Within US1 Phase:

- T015, T016 can run in parallel (different test files)
- T018, T021, T022, T025 can all run in parallel (independent files)
- T017, T019 are sequential within their own chain (T019→T018)
- T020 (JRXML) can run in parallel with everything except T019

Within US2 Phase:

- T026, T027 can run in parallel (different test files)
- T028 can run in parallel with T026, T027

---

## Parallel Example: Foundational Phase (after T001–T004 complete)

```
Task T005: GetPaymentReportMonthsInput record
Task T006: GetPaymentReportMonthsOutput record
Task T007: LoadPaymentReportInput record
Task T008: LoadPaymentReportOutput record
Task T009: GeneratePaymentReportPdfInput record
Task T010: GeneratePaymentReportPdfOutput record
Task T011: GetPaymentReportMonthsInteractor
Task T012: LoadPaymentReportInteractor
Task T013: GeneratePaymentReportPdfInteractor
Task T014: Extend FakeReportRepository + FakeReportFileService
```

## Parallel Example: User Story 1 (infrastructure + UI)

```
# After T003, T004 complete (interfaces extended):
Task T015: Unit tests for LoadPaymentReport + GeneratePdf interactors
Task T016: JDBC integration test for loadPaymentReportData
Task T017: Implement loadPaymentReportData() in JdbcReportRepository
Task T018: PaymentReportRowBean DTO
Task T021: payment-report-view.fxml
Task T022: payment-report-view.css
Task T025: i18n keys in messages.properties + messages_pt_BR.properties
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Foundational
2. Complete Phase 2: User Story 1
3. **STOP and VALIDATE**: Test US1 independently (table renders, PDF exports, row colors correct)
4. Deploy/demo if ready

### Incremental Delivery

1. Foundational → compile-time foundation ready
2. User Story 1 → payment table + PDF export working with current-month selector → **Demo-able MVP**
3. User Story 2 → add historical month selector → complete feature
4. Polish → validation pass

---

## Notes

- [P] tasks = different files, no dependencies — safe for parallel execution
- [Story] label maps each task to its user story for traceability
- T023 (PaymentReportController) initially uses a hardcoded `YearMonth.now()` placeholder for the month selector — this
  is intentional and will be replaced by T029 in US2
- No `module-info.java` changes needed — `reports.ui.controller` is already opened to `javafx.fxml` and
  `com.google.guice`
- No `ReportsModule.java` changes needed — all three new interactors use Guice JIT binding (concrete classes with
  `@Inject`)
- No schema migrations — all queries use existing tables
- Follow the `ReportsController.generatePdf()` async `Task<Void>` pattern for the PDF export button in
  `PaymentReportController`
- Follow `JdbcReportRepository` SUBSTRING trick for contract end-date calculation:
  `DATEADD('MONTH', CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT), c.start_date)` — see `research.md` Q2
