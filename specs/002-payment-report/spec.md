# Feature Specification: Payment Report

**Feature Branch**: `002-payment-report`  
**Created**: 2026-05-12  
**Status**: Draft  
**Input**: User description: "I want to add a new type of report to the application: Payment Report. Before generating
it, the user should be able to select a month (the options should go back from the month of the start of the first
contract, the options should be shown in descending order, most recent first). The generated report should contain a
table with one property per line, with a column for the property name, another for the primary tenant, another for the
primary tenant CPF/CNPJ, another for the date of the payment (should be empty if still unpaid), another for the rent
value, another for the period (receipt period, should be empty if still unpaid). Lines of unpaid rents should have red
text. Lines of vacant properties should be light gray, with primary tenant set to 'Vacant property'"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Generate Payment Report for a Month (Priority: P1)

The property manager wants to quickly see the payment status of all properties for a specific month. They open the
Payment Report screen, select a month from a dropdown, and the application displays a table showing every property, its
current tenant (or "Vacant property"), the tenant's identification number, the payment date and period (if paid), and
the rent value. Unpaid rents appear in red text and vacant properties in light gray.

**Why this priority**: This is the entire feature. Viewing payment status across all properties for a chosen month is
the core deliverable.

**Independent Test**: Navigate to the Payment Report section, select a month, and verify that the table appears with one
row per property showing correct columns, colors, and data.

**Acceptance Scenarios**:

1. **Given** the user is on the Payment Report screen, **When** they select a month from the dropdown, **Then** a table
   is displayed with one row per property, showing: Property Name, Primary Tenant, Primary Tenant CPF/CNPJ, Payment
   Date, Rent Value, and Period.
2. **Given** a property has an active contract during the selected month and a recorded payment covering that month, *
   *When** the report is displayed, **Then** the row shows the payment date, the covered period, and the rent value in
   the default text color.
3. **Given** a property has an active contract during the selected month but no recorded payment covering that month, *
   *When** the report is displayed, **Then** the Payment Date and Period columns are empty, the Rent Value is shown, and
   the entire row is displayed in red text.
4. **Given** a property had no active contract during the selected month, **When** the report is displayed, **Then** the
   Primary Tenant column shows "Vacant property", the CPF/CNPJ, Payment Date, Rent Value, and Period columns are all
   empty, and the row is displayed in light gray text.

---

### User Story 2 - Browse Historical Months (Priority: P2)

The property manager wants to review payment status for past months going back to when the earliest lease began. They
open the Payment Report screen and find a month selector pre-populated with all months from the oldest contract's start
month through the current month, listed from most recent to oldest.

**Why this priority**: Historical review is essential for follow-up on outstanding payments and auditing.

**Independent Test**: Open the Payment Report section and inspect the month dropdown: it must include all months from
the oldest contract's start month through the current month, with the most recent month listed first.

**Acceptance Scenarios**:

1. **Given** contracts exist in the system, **When** the user opens the month dropdown, **Then** the options span from
   the current month back to the month of the oldest contract's start date, in descending order with no gaps.
2. **Given** no contracts exist in the system, **When** the user opens the Payment Report screen, **Then** the month
   selector shows only the current month and the table shows all properties as vacant.

---

### Edge Cases

- What happens when a property has no history of any contract (never been leased)?
- What happens when all properties are vacant for the selected month?
- What happens when a property transitions between contracts during the selected month — which contract's data is shown?
- What happens when a contract has multiple tenants — which is the "primary" tenant displayed?

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The system MUST provide a Payment Report section accessible from the main application navigation.
- **FR-002**: The system MUST present a month selector populated with all months from the earliest contract start month
  through the current month, displayed in descending order (most recent first).
- **FR-003**: Upon selecting a month, the system MUST display a table with exactly one row per property registered in
  the system.
- **FR-004**: Each table row MUST contain the following columns: Property Name, Primary Tenant, Primary Tenant CPF/CNPJ,
  Payment Date, Rent Value, and Period.
- **FR-005**: For properties with an active contract during the selected month, the Primary Tenant column MUST show the
  name of the first listed tenant on the contract.
- **FR-006**: For properties with an active contract during the selected month, the CPF/CNPJ column MUST show the
  identification number (CPF for physical persons, CNPJ for juridical persons) of the first listed tenant.
- **FR-007**: For properties with an active contract during the selected month and a recorded payment covering that
  month, the Payment Date column MUST show the date the payment was recorded, and the Period column MUST show the
  receipt's covered period (start date to end date).
- **FR-008**: For properties with an active contract during the selected month and no recorded payment for that month,
  the Payment Date and Period columns MUST be empty and the entire table row MUST be displayed in red text.
- **FR-009**: For properties with no active contract during the selected month, the Primary Tenant column MUST show "
  Vacant property", all other tenant-related and payment columns MUST be empty, and the row MUST be displayed in light
  gray text.
- **FR-010**: The Rent Value column MUST show the monthly rent amount from the active contract; for vacant properties
  this column MUST be empty.
- **FR-011**: The system MUST generate and export the Payment Report as a PDF document, consistent with the existing
  contract and receipt PDF generation flow.

### Key Entities

- **Property**: A real estate unit registered in the system. Appears in the report for every selected month regardless
  of tenancy status.
- **Contract**: A lease agreement linking one or more tenants to a property for a defined period with a monthly rent
  value. Determines whether a property is occupied for a given month.
- **Receipt**: A recorded payment against a contract, covering a date range (Period) with a specific payment date.
- **Payment Report Row**: A computed combination of a property, its contract status, and its payment status for the
  selected month.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: The property manager can select a month and view the complete payment status table in under 3 seconds.
- **SC-002**: Every registered property appears in the report for any selected month, with no omissions or duplicates.
- **SC-003**: The month selector contains every calendar month from the oldest contract start month to the current month
  with no gaps.
- **SC-004**: Unpaid and vacant rows are visually distinguishable without reading cell content — users correctly
  identify the payment status of any row at a glance.

## Assumptions

- "Primary tenant" is the first tenant in the ordered list of tenants associated with the active contract for the
  selected month.
- A receipt is matched to a selected month if its covered period (intervalStart to intervalEnd) includes any day in that
  month.
- When a property transitions between contracts within a selected month, the contract that was active at the first day
  of that month is used; if no contract was active on the first day, the property is treated as vacant for the report.
- The report reflects data at the time the user generates it; no historical report snapshots are persisted.
- All properties registered in the system are always shown in the report, even if they have never had a contract.
- The month selector is derived dynamically from the earliest contract start date in the system at the time the report
  screen is opened.
