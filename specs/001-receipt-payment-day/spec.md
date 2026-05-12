# Feature Specification: Receipt Payment Day Selection

**Feature Branch**: `001-receipt-payment-day`  
**Created**: 2026-05-12  
**Status**: Draft

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Select Payment Day When Creating a Receipt (Priority: P1)

When a property manager creates a receipt for a contract, they select from a list of outstanding payment due dates for
that contract. The system determines which payment dates have not yet been receipted and presents only those as options.
Selecting a payment due date auto-populates the interval fields (start and end date), which are used for PDF generation.
The user may not need to adjust the interval manually in the common case.

**Why this priority**: This is the core of the feature — it changes how receipts are linked to specific rent
obligations, replacing free-form interval entry with precise payment day tracking.

**Independent Test**: Select a contract with no receipts, verify the dropdown shows exactly the expected list of due
dates from contract start up to today.

**Acceptance Scenarios**:

1. **Given** a contract that started on 10/01/2026 with payment day 15 and no existing receipts, **When** a user opens
   the receipt creation form today (04/05/2026), **Then** the payment day selector shows exactly: 15/01/2026,
   15/02/2026, 15/03/2026, 15/04/2026, 15/05/2026 — and no other options.

2. **Given** a contract that started on 26/03/2026 with payment day 5 and no existing receipts, **When** a user opens
   the receipt creation form today (04/05/2026), **Then** the payment day selector shows exactly: 05/04/2026,
   05/05/2026 — and no other options.

3. **Given** a contract with some payment days already receipted, **When** a user opens the receipt creation form, *
   *Then** only the unreceipted payment days are shown in the selector.

4. **Given** all payment days for a contract have receipts, **When** a user attempts to create a receipt for that
   contract, **Then** the system informs them that there are no outstanding payment days.

---

### User Story 2 - Select Payment Day When Editing a Receipt (Priority: P2)

When a property manager edits an existing receipt, the payment day selector is pre-filled with the current receipt's
payment day and the options include the current day plus any other unreceipted days (so the user can reassign the
receipt to a different due date if needed).

**Why this priority**: Edit support is necessary for correctness, but create is more commonly used and delivers the
primary value.

**Independent Test**: Edit an existing receipt and verify the selector shows the receipt's current payment day
pre-selected, with other unreceipted days also available.

**Acceptance Scenarios**:

1. **Given** an existing receipt linked to payment day 15/01/2026, **When** the user opens the edit form, **Then**
   15/01/2026 is pre-selected in the payment day selector.

2. **Given** an existing receipt linked to 15/01/2026 and other unreceipted days 15/02/2026 and 15/03/2026, **When** the
   user opens the edit form, **Then** the selector shows 15/01/2026, 15/02/2026, and 15/03/2026 as options.

3. **Given** a user changes the payment day on an existing receipt and saves, **When** returning to the receipt list, *
   *Then** the receipt is now linked to the newly selected payment day.

---

### User Story 3 - Dashboard Shows Accurate Pending Rents (Priority: P3)

The dashboard's pending rents section lists each unreceipted payment due date across all active contracts, using the new
payment day calculation rather than the previous period-based approach.

**Why this priority**: Visibility into outstanding rents is secondary to correct data capture, but is still a key
user-facing surface.

**Independent Test**: Create contracts with known payment days and receipts, verify the dashboard pending list matches
expected unpaid entries exactly.

**Acceptance Scenarios**:

1. **Given** an active contract with three payment days due and only one receipted, **When** the user views the
   dashboard, **Then** the pending rents section shows two entries for that contract — one per unreceipted due date.

2. **Given** all payment days for all active contracts are receipted, **When** the user views the dashboard, **Then**
   the pending rents section shows no entries.

3. **Given** a contract whose first payment day has not yet arrived (e.g., contract started yesterday, next due date is
   in the future), **When** the user views the dashboard, **Then** no pending entry appears for that contract.

---

### User Story 4 - Reports Reflect Payment Day Accuracy (Priority: P4)

Any report that surfaces pending or unpaid rents (such as rent evolution or financial summaries) uses the payment day
model to determine which obligations are outstanding.

**Why this priority**: Reports are downstream consumers of the data model; fixing the underlying model automatically
improves report accuracy.

**Independent Test**: Generate a report for a contract with known payment day history and verify the reported
outstanding amounts match expected values.

**Acceptance Scenarios**:

1. **Given** a contract with 3 due payment days and 1 receipt, **When** a report is generated, **Then** the report lists
   2 outstanding payment obligations for that contract.

---

### Edge Cases

- What happens when the contract's payment day is 31 and a month has only 28, 29, or 30 days? The system must clamp to
  the last valid day of that month (e.g., 28/02 instead of 31/02).
- What happens when a contract starts on the same day as its payment day (e.g., starts 15/01/2026, payment day 15)? The
  first due date is 15/01/2026 (same day counts).
- What happens when a contract starts after the payment day in the same month (e.g., starts 26/03/2026, payment day 5)?
  The first due date falls in the next month (05/04/2026).
- What if the contract is marked as rescinded or inactive? Only active contracts generate payment day obligations.
- What if a user tries to create a receipt with a payment day that already has a receipt? The system must prevent
  duplicate receipts for the same payment day on the same contract.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: Each receipt MUST be linked to exactly one payment due date (a specific calendar date representing when
  the rent was owed).
- **FR-002**: The system MUST compute the list of payment due dates for a contract starting from the contract's start
  date up to and including today, based on the contract's payment day of month.
- **FR-003**: The first payment due date for a contract MUST be the first occurrence of the contract's payment day that
  falls on or after the contract's start date. If the payment day occurs before the start date within the same month,
  the first due date is in the following month.
- **FR-004**: Subsequent payment due dates MUST occur monthly thereafter.
- **FR-005**: When the contract's payment day does not exist in a given month (e.g., day 31 in February), the system
  MUST use the last valid day of that month.
- **FR-006**: The receipt creation form MUST present only unreceipted payment due dates for the selected contract as
  selectable options.
- **FR-007**: The receipt editing form MUST pre-select the receipt's current payment due date and also present all other
  unreceipted due dates as options.
- **FR-008**: The system MUST prevent creation of two receipts for the same contract and the same payment due date.
- **FR-009**: The dashboard's pending rents section MUST list each unreceipted payment due date for each active contract
  where the due date is on or before today.
- **FR-010**: The payment due date computation logic MUST be centralized and reused by the receipt form, the dashboard,
  and the reports — not duplicated across the application.
- **FR-011**: Reports that include rent payment status MUST use the payment due date model to determine which
  obligations are outstanding.
- **FR-012**: Inactive, expired, or rescinded contracts MUST NOT contribute payment due dates to any pending rent list.
- **FR-013**: Whenever the user selects or changes the payment due date on the receipt form (both create and edit),
  the interval fields MUST be re-pre-filled with a suggested value, overwriting any previous interval. The user can
  override the pre-filled interval before saving. The pre-fill rule is:
    - **Interval start**: the nearest occurrence of the contract's payment-day-of-month that falls on or before the
      selected payment due date (handles month-end clamping, e.g., payment day 31 clamped to 28/02 → start = 28/02).
    - **Interval end**: interval start + 1 month − 1 day (e.g., start 15/01/2026 → end 14/02/2026).
- **FR-014**: The billing interval (start and end date) on a receipt is used exclusively for PDF generation and does
  NOT affect payment due date tracking or pending rent calculations.
- **FR-015**: At deployment, existing receipts that have no payment due date MUST be automatically migrated by deriving
  a payment due date from their interval data. After migration, all receipts participate in paid/unpaid calculations.

### Key Entities

- **Receipt**: A record of a rent payment linked to a specific payment due date AND retaining a billing interval (start
  date, end date) used for PDF generation. The interval is auto-populated when a payment due date is selected, though
  it remains editable. Key attributes: date paid, payment due date, interval start, interval end, discount, fine,
  observation, associated contract.
- **Payment Due Date**: A computed value representing a specific date on which rent was owed under a contract. Not
  stored independently — derived from contract start date, payment day, and duration up to today.
- **Contract**: Retains its existing attributes (start date, payment day, duration, rent amount) — no structural
  changes, as these already carry all the information needed to compute due dates.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A property manager can select a payment due date and create a receipt in under 60 seconds from opening the
  receipt form.
- **SC-002**: The dashboard pending rents list is accurate — zero false positives (paid days shown as pending) and zero
  false negatives (unpaid days not shown) across all active contracts.
- **SC-003**: The payment due date logic produces consistent results across all surfaces (receipt form, dashboard,
  reports) — the same set of pending days appears regardless of where the user checks.
- **SC-004**: All existing receipt-related workflows (view list, generate PDF, edit) continue to function without
  regressions after the change.

## Assumptions

- The existing contract fields (`startDate`, `paymentDay`, `duration`) are sufficient to compute all payment due dates —
  no new contract attributes are needed.
- "Today" is the boundary: payment due dates falling strictly after today are not yet considered pending and are not
  shown.
- A contract that starts today with a payment day also today generates one immediate pending due date.
- Receipts created before this feature was deployed (which have interval data but no payment due date) will be
  automatically migrated: the system will derive and assign a payment due date for each legacy receipt based on its
  interval data (best-effort). The exact derivation algorithm (e.g., using interval start as the payment due date) is
  determined during implementation planning.
- The scope of "reports" affected is limited to reports that surface pending or outstanding rent information. PDF
  generation for individual receipts is unaffected in format.
- Only one receipt per contract per payment due date is allowed — partial or split payments against a single due date
  are out of scope.

## Clarifications

### Session 2026-05-12

- Q: On the receipt form, how do the interval fields and the payment day selector relate to each other? → A: Selecting a
  payment day pre-fills/suggests the interval fields, but the user can override them
- Q: When a payment due date is selected, what interval gets pre-filled? → A: interval start = nearest occurrence of
  contract's payment-day-of-month on or before the selected due date; interval end = interval start + 1 month − 1 day (
  e.g., selecting 15/02/2026 pre-fills 15/02/2026 → 14/03/2026)
- Q: How should legacy receipts (no payment due date) be handled in paid/unpaid calculations? → A: Best-effort automatic
  migration — derive and assign a payment due date from interval data at deployment
- Q: On the edit form, if the user changed the interval manually and then changes the payment due date, should the
  interval be re-pre-filled? → A: Yes — always re-pre-fill when the payment due date changes, overwriting any manual
  edits
