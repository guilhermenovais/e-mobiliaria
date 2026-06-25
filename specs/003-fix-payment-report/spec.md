# Feature Specification: Fix Payment Report

**Feature Branch**: `003-fix-payment-report`  
**Created**: 2026-06-25  
**Status**: Draft  
**Input**: User description: "I need to adjust some things related to the payment report. First, payments should be
included in a month's report based on the receipt date, not the payment due date. This means that multiple payments for
the same contract may be present in the same month report. Second, the discount and fee should be considered on the rent
value column. Third, the payments on the generated pdf should be ordered by the receipt date. Fourth, there is a bug
that causes payments of contracts that started in the current month to not get shown on the payment report."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Payments Grouped by Receipt Date (Priority: P1)

The property manager generates a payment report for a given month. Instead of seeing payments matched by their due date,
payments appear based on the date the receipt was actually recorded. If a tenant paid two months' rent on the same day (
e.g., catching up on arrears), both payments appear in the report for the month of that receipt date.

**Why this priority**: This changes the fundamental grouping logic of the report. All other adjustments depend on this
behavior being correct first.

**Independent Test**: Create receipts for the same contract with receipt dates falling in the same month but different
payment due dates. Generate the report for that month and verify both receipts appear.

**Acceptance Scenarios**:

1. **Given** a contract has a receipt with a receipt date in June and a payment due date in May, **When** the user
   generates the June report, **Then** the payment row appears in the June report.
2. **Given** a contract has a receipt with a receipt date in June and a payment due date in May, **When** the user
   generates the May report, **Then** no paid row appears for that contract in the May report (it may appear as unpaid
   if no other receipt covers May).
3. **Given** a contract has two receipts both with receipt dates in June (e.g., one for May's due date and one for June'
   s due date), **When** the user generates the June report, **Then** two rows appear for the same contract, one for
   each receipt.
4. **Given** a contract has no receipts with receipt dates in the selected month, **When** the user generates the
   report, **Then** the contract appears as a single unpaid row.

---

### User Story 2 - Rent Value Reflects Discount and Fee (Priority: P1)

The property manager views the payment report and the Rent Value column shows the effective amount considering discounts
and fines applied to the receipt, not just the base contract rent.

**Why this priority**: The rent value column currently misrepresents the actual amount, making the report inaccurate for
financial reconciliation.

**Independent Test**: Create a receipt with a discount and a fine applied. Generate the report and verify the Rent Value
column shows the contract rent adjusted by the discount and fine.

**Acceptance Scenarios**:

1. **Given** a contract with rent of 1000, a receipt with a discount of 50 and a fine of 20, **When** the report is
   generated, **Then** the Rent Value column for the paid row shows 970 (1000 - 50 + 20).
2. **Given** a contract with rent of 1000, a receipt with no discount and no fine, **When** the report is generated, *
   *Then** the Rent Value column for the paid row shows 1000.
3. **Given** a contract with rent of 1000 and no receipt for the selected month, **When** the report is generated, *
   *Then** the Rent Value column for the unpaid row shows the base contract rent (1000).

---

### User Story 3 - PDF Rows Ordered by Receipt Date (Priority: P2)

When the property manager exports the payment report to PDF, the rows are sorted by receipt date in ascending order.
Unpaid and vacant rows (which have no receipt date) appear after all paid rows.

**Why this priority**: Ordering by receipt date makes the PDF easier to cross-reference with bank statements and
bookkeeping.

**Independent Test**: Generate a PDF with multiple paid and unpaid rows. Verify that paid rows appear first, sorted by
receipt date ascending, followed by unpaid and vacant rows.

**Acceptance Scenarios**:

1. **Given** multiple paid rows with different receipt dates, **When** the PDF is generated, **Then** paid rows appear
   sorted by receipt date in ascending order.
2. **Given** a mix of paid, unpaid, and vacant rows, **When** the PDF is generated, **Then** unpaid and vacant rows
   appear after all paid rows.

---

### User Story 4 - Contracts Starting in Current Month Are Shown (Priority: P1)

The property manager generates the payment report for the current month. Contracts that started partway through the
current month appear in the report, showing the tenant information and payment status correctly.

**Why this priority**: This is a bug fix. Currently, contracts starting after the first day of the current month are
excluded, causing tenants and their payments to be invisible in the report.

**Independent Test**: Create a contract starting on the 15th of the current month. Generate the current month's payment
report and verify the contract's property appears with tenant information (not as vacant).

**Acceptance Scenarios**:

1. **Given** a contract starts on June 15 of the current month, **When** the user generates the June report, **Then**
   the property row shows the contract's tenant and payment status, not "Vacant property".
2. **Given** a contract starts on June 15 and has a receipt dated June 20, **When** the user generates the June report,
   **Then** the row appears as paid with the correct tenant and receipt information.

---

### Edge Cases

- What happens when a contract has receipts with receipt dates in a month where the contract was not yet active? The
  receipt should still appear since grouping is by receipt date.
- What happens when a property has multiple contracts and receipts from different contracts fall in the same month? Each
  receipt appears as a separate row.
- What happens when a receipt has both a discount and fine of zero? The rent value should equal the base contract rent.

## Clarifications

### Session 2026-06-25

- Q: When a contract has receipts with receipt dates in the selected month (covering a different month's due date) but
  no receipt for the selected month's own due date, should an unpaid row also appear? → A: No. If any receipt with a
  receipt date in the selected month exists for a contract, no unpaid row appears.
- Q: The codebase uses "fine" for the surcharge field but the spec used "fee". Which is canonical? → A: "fine" — align
  the spec to the existing codebase term.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST include a payment in a month's report based on the receipt date (the date the receipt was
  recorded), not the payment due date.
- **FR-002**: The system MUST allow multiple rows for the same contract in a single month's report when multiple
  receipts have receipt dates within that month.
- **FR-003**: The Rent Value column for paid rows MUST show the contract rent adjusted by the receipt's discount (
  subtracted) and fine (added): `rent - discount + fine`.
- **FR-004**: The Rent Value column for unpaid rows MUST show the base contract rent without adjustments.
- **FR-005**: Rows in both the on-screen table and the PDF MUST be ordered by receipt date in ascending order, with
  unpaid and vacant rows appearing after all paid rows.
- **FR-006**: The system MUST include contracts that started at any point during the selected month, not only contracts
  active on the first day of the month.
- **FR-007**: For the current month, contracts starting after the first day but before or on the current date MUST
  appear in the report with their correct tenant information and payment status.

### Key Entities

- **Receipt**: A recorded payment with a receipt date, a payment due date, a discount, and a fine. The receipt date
  determines which month's report the payment appears in.
- **PaymentReportRow**: Updated to reflect the effective rent value (base rent adjusted by discount and fine) and to
  support multiple rows per contract per month.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Every receipt with a receipt date in the selected month appears in the report, regardless of the payment
  due date.
- **SC-002**: The Rent Value column for paid rows matches the formula `rent - discount + fine` for every row.
- **SC-003**: All contracts active during any part of the selected month appear in the report with correct tenant
  information.
- **SC-004**: Rows in the PDF are sorted by receipt date ascending, with non-paid rows at the end.

## Assumptions

- The receipt date field on the Receipt entity corresponds to the `date` column in the `receipts` table.
- Discount and fine are stored as integer values in the same unit as rent (cents).
- The existing on-screen table and the PDF report should both reflect the same data and ordering.
- Unpaid rows continue to be determined per contract: if a contract is active in the selected month and has no receipt
  with a receipt date in that month, one unpaid row is shown for that contract.
- Vacant property rows continue to appear for properties with no active contract in the selected month.
- The total field at the bottom of the report should reflect the sum of the adjusted rent values (rent - discount +
  fine) for all rows, not just base rents.
