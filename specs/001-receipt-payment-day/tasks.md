# Tasks: Receipt Payment Day Selection

**Input**: Design documents from `/specs/001-receipt-payment-day/`
**Prerequisites**: plan.md ✓, spec.md ✓, research.md ✓, data-model.md ✓, contracts/use-case-contracts.md ✓

**Tests**: Included — data-model.md and plan.md explicitly require test coverage for `PaymentDueDateService` and the
interactors.

**Organization**: Tasks grouped by user story to enable independent implementation and testing of each story. US4 (
Reports) generates no tasks — see note after Phase 4.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel with other tasks in this phase (different files, no unresolved dependencies)
- **[Story]**: User story this task belongs to (US1, US2, US3)
- Exact file paths are included in every task description

---

## Phase 1: Foundational (Blocking Prerequisites)

**Purpose**: DB schema, core domain service, repository contracts, and infrastructure that ALL user stories depend on.

**⚠️ CRITICAL**: No user story work can begin until this phase is complete.

- [X] T001 [P] Create V8 Flyway migration in `src/main/resources/db/migration/V8__add_receipt_payment_due_date.sql`: (1)
  `ALTER TABLE receipts ADD COLUMN payment_due_date DATE;` (2) `UPDATE receipts SET payment_due_date = ...` using the
  H2-compatible DATEADD backfill expression from data-model.md that clamps `contract.payment_day` to the month of
  `interval_start`; (3) `ALTER TABLE receipts ALTER COLUMN payment_due_date DATE NOT NULL;` (4)
  `ALTER TABLE receipts ADD CONSTRAINT uq_receipt_contract_payment_due_date UNIQUE (contract_id, payment_due_date);` —
  use the exact SQL from data-model.md verbatim
- [X] T002 [P] Implement `PaymentDueDateService` as a pure concrete class (no interface, no framework imports) in
  `src/main/java/com/guilherme/emobiliaria/contract/domain/service/PaymentDueDateService.java` — single public method
  `public List<LocalDate> computeDueDates(LocalDate startDate, int paymentDay, LocalDate today)`: (1) compute
  `firstDueDate` by clamping `paymentDay` to `startDate.lengthOfMonth()` and taking the same-month candidate; if the
  candidate falls before `startDate`, advance one month and re-clamp; (2) iterate monthly with
  `Math.min(paymentDay, yearMonth.lengthOfMonth())` clamping, collecting dates `<= today`; (3) return list (possibly
  empty)
- [X] T003 [P] Add `paymentDueDate: LocalDate` field to `Receipt` entity in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/entity/Receipt.java`: insert `paymentDueDate` parameter after
  `date` in both `create()` and `restore()` factory methods; add null-check validation (throw `IllegalArgumentException`
  or similar if null); ensure `paymentDueDate` is stored and exposed via getter
- [X] T004 Add two new methods to `ReceiptRepository` interface in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/repository/ReceiptRepository.java`:
  `boolean existsByContractAndPaymentDueDate(Long contractId, LocalDate paymentDueDate, Long excludeReceiptId);` and
  `List<LocalDate> findAllPaymentDueDatesByContractId(Long contractId);` — `excludeReceiptId` is nullable (null means do
  not exclude any receipt)
- [X] T005 [P] Implement the two new `ReceiptRepository` methods in `FakeReceiptRepository` at
  `src/test/java/com/guilherme/emobiliaria/receipt/domain/repository/FakeReceiptRepository.java`:
  `existsByContractAndPaymentDueDate` scans the in-memory list for matching `contractId` and `paymentDueDate`, skipping
  the entry whose id equals `excludeReceiptId` (when not null); `findAllPaymentDueDatesByContractId` returns a list of
  all non-null `paymentDueDate` values for receipts with the matching `contractId`
- [X] T006 [P] Modify `JdbcReceiptRepository` in
  `src/main/java/com/guilherme/emobiliaria/receipt/infrastructure/repository/JdbcReceiptRepository.java`: (a) include
  `payment_due_date` in all INSERT and UPDATE SQL statements and bind the `LocalDate` value; (b) map `payment_due_date`
  from `ResultSet` in the row-mapping method, passing it to `Receipt.restore()`; (c) implement
  `existsByContractAndPaymentDueDate` with
  `SELECT COUNT(*) FROM receipts WHERE contract_id = ? AND payment_due_date = ? AND (? IS NULL OR id != ?)` — returns
  `count > 0`; (d) implement `findAllPaymentDueDatesByContractId` with
  `SELECT payment_due_date FROM receipts WHERE contract_id = ?` mapped to `List<LocalDate>`
- [X] T007 Write `PaymentDueDateServiceTest` in
  `src/test/java/com/guilherme/emobiliaria/contract/domain/service/PaymentDueDateServiceTest.java` using JUnit 5,
  covering: (1) `paymentDay=15, startDate=2026-01-10, today=2026-05-04` →
  `[2026-01-15, 2026-02-15, 2026-03-15, 2026-04-15, 2026-05-15]`; (2)
  `paymentDay=5, startDate=2026-03-26, today=2026-05-04` → `[2026-04-05, 2026-05-05]`; (3) `paymentDay=31` in February →
  clamped to `2026-02-28` (non-leap); (4) `startDate` equals the payment day date → first due date is that same day; (5)
  `today` before `firstDueDate` → empty list

**Checkpoint**: Schema migrated, `PaymentDueDateService` testable, `Receipt` entity updated, repository contract and
infrastructure complete — user story phases can now proceed.

---

## Phase 2: User Story 1 — Select Payment Day When Creating a Receipt (Priority: P1) 🎯 MVP

**Goal**: Property manager opens the receipt creation form, selects a contract, sees a payment day `ComboBox` populated
with unreceipted due dates, picks one, and the interval fields auto-fill. Duplicate due dates are rejected.

**Independent Test**: Select a contract with no existing receipts; verify the `ComboBox` shows exactly the expected due
dates from contract start up to today. Create a receipt; verify it is saved with the correct `paymentDueDate` and
interval values.

- [X] T008 [P] [US1] Add `paymentDueDate: LocalDate` field to `CreateReceiptInput` record in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/input/CreateReceiptInput.java` — insert after `date`,
  before `intervalStart`; all existing fields remain in their original order
- [X] T009 [P] [US1] Create `GetUnreceiptedDueDatesInput` record
  `(Long contractId, LocalDate today, Long excludeReceiptId)` in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/input/GetUnreceiptedDueDatesInput.java` and
  `GetUnreceiptedDueDatesOutput` record `(List<LocalDate> dueDates)` in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/output/GetUnreceiptedDueDatesOutput.java`
- [X] T010 [US1] Implement `GetUnreceiptedDueDatesInteractor` in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/usecase/GetUnreceiptedDueDatesInteractor.java` — annotate
  constructor `@Inject`; accept `ContractRepository`, `ReceiptRepository`, and `PaymentDueDateService`; execute: (1)
  load contract via `ContractRepository.findById(input.contractId())`, throw `BusinessException(Contract.NOT_FOUND)` if
  absent; (2) return empty `GetUnreceiptedDueDatesOutput` if contract status is not ACTIVE or EXPIRING; (3) call
  `paymentDueDateService.computeDueDates(contract.startDate, contract.paymentDay, input.today())`; (4) call
  `receiptRepository.findAllPaymentDueDatesByContractId(input.contractId())` to get receipted set; (5) if
  `input.excludeReceiptId()` is not null, load that receipt and remove its `paymentDueDate` from the receipted set; (6)
  return computed dates minus receipted set, sorted ascending
- [X] T011 [US1] Modify `CreateReceiptInteractor` in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/usecase/CreateReceiptInteractor.java`: (a) read
  `paymentDueDate` from `CreateReceiptInput`; (b) call
  `receiptRepository.existsByContractAndPaymentDueDate(input.contractId(), input.paymentDueDate(), null)` before
  creating the entity; if true, throw `BusinessException(Receipt.DUPLICATE_PAYMENT_DUE_DATE)`; (c) pass
  `input.paymentDueDate()` to `Receipt.create()`
- [X] T012 [US1] Open `com.guilherme.emobiliaria.contract.domain.service` to `com.google.guice` in
  `src/main/java/module-info.java` (needed for Guice to instantiate `PaymentDueDateService`); also verify
  `com.guilherme.emobiliaria.receipt.application.usecase` is already opened to Guice — if not, add it; no explicit
  `bind()` is needed in `ReceiptModule` (`GetUnreceiptedDueDatesInteractor` resolves via JIT binding)
- [X] T013 [US1] Modify `ReceiptFormController` in
  `src/main/java/com/guilherme/emobiliaria/receipt/ui/controller/ReceiptFormController.java` and
  `src/main/resources/com/guilherme/emobiliaria/receipt/ui/view/receipt-form-view.fxml` for create mode: (a) inject
  `GetUnreceiptedDueDatesInteractor` via constructor; (b) add `ComboBox<LocalDate>` for payment day to the FXML and bind
  `@FXML` field in controller; (c) on contract selection, call `GetUnreceiptedDueDatesInteractor` with
  `(contractId, LocalDate.now(), null)`, populate `ComboBox` with results — if results are empty, show an informational
  message and disable the submit button; (d) on `ComboBox` selection change, set `intervalStart = selectedDate` and
  `intervalEnd = selectedDate.plusMonths(1).minusDays(1)` in the interval date pickers (overwriting any previous
  values); (e) include `paymentDueDate` from the selected `ComboBox` value in `CreateReceiptInput`
- [X] T014 [P] [US1] Write `GetUnreceiptedDueDatesInteractorTest` in
  `src/test/java/com/guilherme/emobiliaria/receipt/application/usecase/GetUnreceiptedDueDatesInteractorTest.java` using
  `FakeReceiptRepository` and `FakeContractRepository` and the real `PaymentDueDateService`: (1) contract with no
  receipts shows all due dates up to today (US1 scenario 1 values); (2) contract with some receipted dates shows only
  unreceipted dates (US1 scenario 3); (3) all dates receipted returns empty list (US1 scenario 4); (4)
  inactive/rescinded contract returns empty list; (5) `excludeReceiptId` not null re-includes that receipt's due date in
  the options (edit mode behaviour)
- [X] T015 [P] [US1] Update `CreateReceiptInteractorTest` in
  `src/test/java/com/guilherme/emobiliaria/receipt/application/usecase/CreateReceiptInteractorTest.java`: (a) add
  `paymentDueDate` to all existing `CreateReceiptInput` constructions; (b) add test: creating a receipt with a
  `paymentDueDate` already used by another receipt for the same contract throws `BusinessException` with code
  `Receipt.DUPLICATE_PAYMENT_DUE_DATE`; (c) add test: valid `paymentDueDate` is persisted correctly on the saved receipt

**Checkpoint**: User Story 1 fully functional — receipt creation uses payment day selector with interval auto-fill and
duplicate protection.

---

## Phase 3: User Story 2 — Select Payment Day When Editing a Receipt (Priority: P2)

**Goal**: Property manager opens an existing receipt for editing, sees the current payment day pre-selected in the
`ComboBox` alongside other unreceipted options, can reassign to a different due date, and the interval re-fills on
change.

**Independent Test**: Open an existing receipt in edit mode; verify the `ComboBox` pre-selects the receipt's current
`paymentDueDate` and also lists other unreceipted dates. Change the selection and save; verify the receipt now reflects
the newly selected payment day.

- [X] T016 [US2] Add `paymentDueDate: LocalDate` field to `EditReceiptInput` record in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/input/EditReceiptInput.java` — insert after `date`,
  before `intervalStart`
- [X] T017 [US2] Modify `EditReceiptInteractor` in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/usecase/EditReceiptInteractor.java`: (a) read
  `paymentDueDate` from `EditReceiptInput`; (b) call
  `receiptRepository.existsByContractAndPaymentDueDate(input.contractId(), input.paymentDueDate(), input.id())` — if
  true, throw `BusinessException(Receipt.DUPLICATE_PAYMENT_DUE_DATE)`; (c) pass `input.paymentDueDate()` to
  `Receipt.restore()` when rebuilding the entity for update
- [X] T018 [US2] Modify `ReceiptFormController` in
  `src/main/java/com/guilherme/emobiliaria/receipt/ui/controller/ReceiptFormController.java` for edit mode: (a) on form
  load, call `GetUnreceiptedDueDatesInteractor` with `(contractId, LocalDate.now(), currentReceiptId)`; (b) pre-select
  the receipt's current `paymentDueDate` in the `ComboBox`; (c) pre-fill all other fields (date, intervalStart,
  intervalEnd, discount, fine, observation) from the existing receipt data; (d) on `ComboBox` selection change (
  including programmatic pre-selection change by user), re-set `intervalStart = selectedDate` and
  `intervalEnd = selectedDate.plusMonths(1).minusDays(1)` — always overwrites; (e) include `paymentDueDate` from
  `ComboBox` in `EditReceiptInput`
- [X] T019 [US2] Update `EditReceiptInteractorTest` in
  `src/test/java/com/guilherme/emobiliaria/receipt/application/usecase/EditReceiptInteractorTest.java`: (a) add
  `paymentDueDate` to all existing `EditReceiptInput` constructions; (b) add test: changing `paymentDueDate` to a date
  already used by a different receipt on the same contract throws
  `BusinessException(Receipt.DUPLICATE_PAYMENT_DUE_DATE)`; (c) add test: editing a receipt to keep its own
  `paymentDueDate` does not throw (self-exclusion via `excludeReceiptId`); (d) add test: valid `paymentDueDate` change
  is persisted correctly

**Checkpoint**: User Stories 1 and 2 functional — both create and edit flows use the payment day selector.

---

## Phase 4: User Story 3 — Dashboard Shows Accurate Pending Rents (Priority: P3)

**Goal**: The dashboard's pending rents section lists each unreceipted `paymentDueDate` (on or before today) per active
contract, replacing the old interval-based calculation.

**Independent Test**: Seed contracts with known `startDate`, `paymentDay`, and some receipts; verify the dashboard
pending list shows exactly the expected unreceipted due dates — no false positives, no false negatives.

- [X] T020 [US3] Replace the unpaid rent algorithm in `JdbcDashboardRepository` at
  `src/main/java/com/guilherme/emobiliaria/dashboard/infrastructure/repository/JdbcDashboardRepository.java`: (a) inject
  `PaymentDueDateService` via constructor (`@Inject`); (b) keep the existing SQL CTE that loads active contracts; (c)
  remove the old `computePeriodStart` and `computeDeadline` static helper methods; (d) for each active contract returned
  by the SQL CTE, call `paymentDueDateService.computeDueDates(contract.startDate, contract.paymentDay, LocalDate.now())`
  in Java; (e) batch-load all receipted due dates for those contracts in one query:
  `SELECT contract_id, payment_due_date FROM receipts WHERE contract_id IN (...)` — build a `Map<Long, Set<LocalDate>>`
  from the results; (f) for each contract subtract its receipted set from its computed set; remaining dates are
  unreceipted pending entries — build and return the appropriate `UnpaidRentEntry` list (one entry per unreceipted date,
  using existing `UnpaidRentEntry` structure)
- [X] T021 [US3] Update `JdbcDashboardRepositoryTest` in
  `src/test/java/com/guilherme/emobiliaria/dashboard/infrastructure/repository/JdbcDashboardRepositoryTest.java`: update
  test fixtures to include `payment_due_date` in any inserted `receipts` rows; add or update tests to verify the new
  algorithm — active contract with 3 due dates and 1 receipt shows 2 pending; all receipted shows 0; future-only due
  dates show 0 pending

**Checkpoint**: Dashboard pending rents list now reflects the payment day model with accurate unpaid entries.

---

> **Note — User Story 4 (Reports)**: No tasks generated. Per research.md Decision 8, the `reports` module does not
> surface outstanding or pending rents. FR-011 is satisfied automatically — all receipt data now carries
`payment_due_date`. No report-specific code changes are required for this feature.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Entity test coverage and final regression validation.

- [X] T022 [P] Update `ReceiptTest` in
  `src/test/java/com/guilherme/emobiliaria/receipt/domain/entity/ReceiptTest.java`: (a) add `paymentDueDate` to all
  existing `Receipt.create()` and `Receipt.restore()` calls; (b) add test: `create()` with null `paymentDueDate`
  throws; (c) add test: `paymentDueDate` is correctly exposed by its getter
- [X] T023 Run the full JUnit 5 test suite (`./mvnw test` or project-equivalent command) and confirm: (a) all new tests
  pass; (b) no regressions in existing receipt workflows (list, PDF generation, delete); (c) existing
  `ReceiptFormControllerAmountParsingTest` and `ReceiptFormControllerDateCalculationTest` still pass; (d) dashboard and
  contract tests still pass — resolve any compilation errors caused by the new `paymentDueDate` parameter in
  `Receipt.create()` / `Receipt.restore()` across the codebase before marking this task done

**Checkpoint**: All phases complete. Receipt feature upgraded with payment day tracking end-to-end.

---

## Dependencies & Execution Order

### Phase Dependencies

- **Phase 1 (Foundational)**: Start immediately.
    - T001, T002, T003 parallel (no mutual dependencies)
    - T004 after T003 (interface references entity)
    - T005 and T006 parallel after T004 (T006 also after T001)
    - T007 after T002
- **Phase 2 (US1)**: Requires all Phase 1 tasks complete.
    - T008 and T009 parallel
    - T010 after T009
    - T011 after T008
    - T012 after T010
    - T013 after T010 and T011
    - T014 and T015 parallel (T014 after T010, T015 after T011)
- **Phase 3 (US2)**: Requires Phase 1 complete + T010 (GetUnreceiptedDueDatesInteractor).
    - T016 first; T017 after T016; T018 after T016 and T017; T019 after T017
- **Phase 4 (US3)**: Requires Phase 1 complete (especially T002 for PaymentDueDateService). Independent of Phase 2/3
  implementation tasks.
- **Phase 5 (Polish)**: After all implementation phases.

### User Story Dependencies

- **US1 (P1)**: Requires Phase 1. No dependency on US2 or US3. Start here for MVP.
- **US2 (P2)**: Requires Phase 1 + T010 (cannot build edit selector without GetUnreceiptedDueDatesInteractor). Cannot
  start before T010 is done.
- **US3 (P3)**: Requires Phase 1 (T002). Independent of US1/US2 implementation tasks — can be worked in parallel with
  Phase 2.
- **US4 (P4)**: No tasks.

### Parallel Opportunities

**Phase 1:**

```
Immediate (parallel):
  T001 - DB migration
  T002 - PaymentDueDateService
  T003 - Receipt entity

Then sequential:
  T004 - ReceiptRepository interface  (after T003)

Then parallel after T004:
  T005 - FakeReceiptRepository
  T006 - JdbcReceiptRepository        (also needs T001)

After T002:
  T007 - PaymentDueDateServiceTest
```

**Phase 2 (US1):**

```
Start together:
  T008 - CreateReceiptInput
  T009 - GetUnreceiptedDueDates I/O records

After T009:
  T010 - GetUnreceiptedDueDatesInteractor

After T008:
  T011 - CreateReceiptInteractor

After T010:
  T012 - DI wiring + module-info.java

After T010 + T011:
  T013 - ReceiptFormController (create mode)

Parallel (T014 after T010, T015 after T011):
  T014 - GetUnreceiptedDueDatesInteractorTest
  T015 - CreateReceiptInteractorTest
```

**Phase 3 (US2) + Phase 4 (US3) can run in parallel** once Phase 1 is done and T010 is done (for US2).

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Foundational) — 7 tasks, T001–T003 parallel start
2. Complete Phase 2 (US1) — 8 tasks
3. **STOP and VALIDATE**: Launch app, open receipt creation form, select a contract, verify the payment day dropdown
   populates, pick a date, verify interval auto-fills, save the receipt, verify no regressions in list and PDF views
4. Deploy if ready

### Incremental Delivery

1. Phase 1 → Foundation ready
2. Phase 2 (US1) → Create receipt with payment day → Deploy/Demo (**MVP**)
3. Phase 3 (US2) → Edit receipt with payment day → Deploy/Demo
4. Phase 4 (US3) → Dashboard accuracy → Deploy/Demo
5. Phase 5 → Polish + full test suite → Final release

---

## Notes

- [P] = different files, no unresolved in-phase dependencies — safe to parallelize
- Tests are included because data-model.md explicitly states "Test coverage required" for `PaymentDueDateService` and
  interactor tests appear throughout plan.md's source structure
- `Receipt.create()` and `Receipt.restore()` signature changes will break compilation in many existing tests — T023
  surface these; fix them by adding the new `paymentDueDate` parameter to all callers
- The FXML for the receipt form is `src/main/resources/com/guilherme/emobiliaria/receipt/ui/view/receipt-form-view.fxml`
- `PaymentDueDateService` has no interface per research.md Decision 1 — Guice resolves it as a concrete class via JIT
  binding once its package is opened to Guice (T012)
- The `payment_due_date` UNIQUE constraint in the migration (T001) will intentionally fail if legacy data contains
  duplicates — this surfaces a data quality issue before deployment and is by design (research.md Decision 2)
