# Tasks: Fix Payment Report

**Input**: Design documents from `/specs/003-fix-payment-report/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, quickstart.md

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (e.g., US1, US2, US3, US4)
- Include exact file paths in descriptions

## Phase 1: Setup

**Purpose**: No setup required. This is a bug fix / behavior change on an existing project with no new packages,
classes, or infrastructure.

---

## Phase 2: Foundational

**Purpose**: No foundational work required. All changes modify existing methods in existing files.

**Checkpoint**: Ready to proceed directly to user story implementation.

---

## Phase 3: User Story 4 - Contracts Starting Mid-Month Are Shown (Priority: P1) :dart: MVP

**Goal**: Contracts that start partway through the selected month appear in the report instead of being excluded.

**Independent Test**: Create a contract starting on the 15th of a month. Generate that month's report and verify the
contract's property appears with tenant info, not as "Vacant property".

### Implementation for User Story 4

- [X] T001 [US4] Fix the contract start date filter in `loadPaymentReportData()` — change the parameter bound to
  `c.start_date <= ?` from `firstDay` to `lastDay` (affects parameters 1 and 3 in the PreparedStatement) in
  `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepository.java`
- [X] T002 [US4] Add integration tests: (1) contract starting mid-month appears as UNPAID with tenant info, (2) contract
  starting mid-month with a receipt appears as PAID, in
  `src/test/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepositoryTest.java`

**Checkpoint**: Contracts starting on any day of the selected month now appear in the report. Existing tests still pass.

---

## Phase 4: User Story 1 - Payments Grouped by Receipt Date (Priority: P1)

**Goal**: Payments appear in the report based on the receipt date (`r.date`) instead of the payment due date (
`r.payment_due_date`). Multiple receipts for the same contract in the same month each produce their own row.

**Independent Test**: Create receipts for the same contract with receipt dates in the same month but different payment
due dates. Generate the report and verify both receipts appear.

### Implementation for User Story 1

- [X] T003 [US1] Rewrite the SQL query in `loadPaymentReportData()` — change the receipt LEFT JOIN to filter by
  `r.date >= ? AND r.date <= ?` instead of `r.payment_due_date >= ? AND r.payment_due_date <= ?`, and remove the
  `MAX(r2.id)` correlated subquery that restricts to one receipt per contract, in
  `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepository.java`
- [X] T004 [US1] Update the existing `shouldPlaceReceiptInPaymentDueDateMonth` test to verify receipt-date-based
  grouping instead, and add new tests: (1) two receipts with receipt dates in the same month for one contract produce
  two PAID rows, (2) a receipt with receipt date in month X but payment due date in month Y appears in month X's report,
  in `src/test/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepositoryTest.java`

**Checkpoint**: Report groups payments by receipt date. Multiple receipts per contract per month each produce their own
row. Existing tests updated to match.

---

## Phase 5: User Story 2 - Rent Value Reflects Discount and Fine (Priority: P1)

**Goal**: The Rent Value column for paid rows shows `contract.rent - receipt.discount + receipt.fine` instead of just
`contract.rent`. Unpaid rows continue to show the base contract rent.

**Independent Test**: Create a receipt with a discount of 50 and a fine of 20 on a contract with rent 1000. Verify the
Rent Value column shows 970.

### Implementation for User Story 2

- [X] T005 [US2] Add `r.discount` and `r.fine` to the SQL SELECT clause, and update the Java ResultSet processing in
  `loadPaymentReportData()` to compute `rent - discount + fine` for PAID rows in
  `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepository.java`
- [X] T006 [US2] Add an overloaded `insertReceipt` helper method that accepts discount and fine parameters, and add
  tests: (1) paid row with discount=50 and fine=20 shows rent=970 for base rent 1000, (2) paid row with zero discount
  and fine shows base rent, (3) unpaid row shows base contract rent unmodified, in
  `src/test/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepositoryTest.java`

**Checkpoint**: Paid rows show the adjusted rent value. Unpaid rows show base rent. The on-screen total and PDF total
automatically reflect adjusted values since they sum `rent()`.

---

## Phase 6: User Story 3 - PDF Rows Ordered by Receipt Date (Priority: P2)

**Goal**: PDF rows are sorted by receipt date ascending within the paid group. Unpaid and vacant rows appear after all
paid rows.

**Independent Test**: Generate a PDF with multiple paid rows having different receipt dates and some unpaid rows. Verify
paid rows are sorted by receipt date ascending, with unpaid/vacant rows at the end.

### Implementation for User Story 3

- [X] T007 [P] [US3] Change the sorting in `generatePaymentReportPdf()` from
  `thenComparing(PaymentReportRowBean::getPropertyName)` to sorting by receipt date ascending within the paid group (
  sort `PaymentReportRow` list before bean conversion, or add receipt-date-based sort to bean comparator), in
  `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/service/ReportFileServiceImpl.java`
- [X] T008 [US3] Add a test for `generatePaymentReportPdf()` that verifies paid rows are sorted by receipt date
  ascending and unpaid/vacant rows appear after paid rows, in
  `src/test/java/com/guilherme/emobiliaria/reports/infrastructure/service/ReportFileServiceImplTest.java`

**Checkpoint**: PDF output has paid rows sorted by receipt date, with unpaid/vacant rows at the end.

---

## Phase 7: Polish & Cross-Cutting Concerns

**Purpose**: Final validation across all stories

- [X] T009 Run full test suite (`mvn test`) and verify all acceptance scenarios from spec.md pass
- [X] T010 Run quickstart.md validation — verify build commands succeed and no regressions in other report features (
  rent evolution, occupation rate)

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Skipped — no new infrastructure
- **Foundational (Phase 2)**: Skipped — no blocking prerequisites
- **US4 (Phase 3)**: Can start immediately — simplest fix, touches only parameter bindings
- **US1 (Phase 4)**: Depends on US4 completion (both modify the same SQL query)
- **US2 (Phase 5)**: Depends on US1 completion (layers rent computation on the restructured query)
- **US3 (Phase 6)**: **Independent of US1/US2/US4** — modifies a different file (`ReportFileServiceImpl.java`)
- **Polish (Phase 7)**: Depends on all story phases being complete

### User Story Dependencies

- **US4 (P1)**: No dependencies — start here (MVP, simplest fix)
- **US1 (P1)**: Depends on US4 (same SQL query, US1 restructures it further)
- **US2 (P1)**: Depends on US1 (adds computation to the restructured query)
- **US3 (P2)**: Can start after Phase 2 — completely independent file

### Parallel Opportunities

- **T007** [US3] can run in parallel with **T001–T006** (US4, US1, US2) since it modifies `ReportFileServiceImpl.java`
  while the others modify `JdbcReportRepository.java`
- Within each story, implementation tasks must precede their corresponding test tasks

---

## Parallel Example

```text
# These can run in parallel (different files):
Stream A: T001 → T002 → T003 → T004 → T005 → T006  (JdbcReportRepository + tests)
Stream B: T007 → T008                                  (ReportFileServiceImpl + tests)

# Then converge:
T009, T010 (Polish — run after both streams complete)
```

---

## Implementation Strategy

### MVP First (User Story 4 Only)

1. Complete T001–T002: Fix mid-month contract bug
2. **STOP and VALIDATE**: Run `mvn test -Dtest="JdbcReportRepositoryTest"` — verify the fix works
3. This alone fixes a user-visible bug

### Incremental Delivery

1. US4 (T001–T002) → Bug fix validated
2. US1 (T003–T004) → Receipt date grouping validated
3. US2 (T005–T006) → Adjusted rent values validated
4. US3 (T007–T008) → PDF sorting validated (can run in parallel with steps 1–3)
5. Polish (T009–T010) → Full regression check

---

## Notes

- All four stories modify only infrastructure-layer code — no domain, application, or UI changes required
- `PaymentReportRow` record is unchanged; the `rent` field carries adjusted values for PAID rows
- The on-screen total in `PaymentReportController` already sums `rent()`, so it automatically reflects adjusted values
- US1 and US2 are both P1 but US2 depends on US1's query restructuring being in place
- US3 is P2 but can be implemented in parallel since it touches a different file
