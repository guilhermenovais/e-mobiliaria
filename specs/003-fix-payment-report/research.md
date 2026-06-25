# Research: Fix Payment Report

## R1: How to filter receipts by receipt date instead of payment due date

**Decision**: Change the SQL JOIN condition in `JdbcReportRepository.loadPaymentReportData()` from filtering on
`r.payment_due_date` to filtering on `r.date` (the receipt date field).

**Rationale**: The `Receipt` entity has a `date` field (receipt date — when the payment was recorded) and a
`paymentDueDate` field (when the payment was due). The current SQL filters receipts by `payment_due_date` within the
month range. Per FR-001, payments should appear in the report based on `date`, not `payment_due_date`. The `receipts`
table column `date` maps to `Receipt.date`.

**Alternatives considered**:

- Filtering in Java after fetching all receipts: rejected — would load unnecessary data and complicate the query
  structure.

## R2: How to support multiple receipts per contract in the same month

**Decision**: Remove the `MAX(r2.id)` subquery that limits receipts to one per contract per month. Instead, use a LEFT
JOIN on receipts with the date range condition, producing one row per receipt. Contracts with no matching receipt still
produce a single UNPAID or VACANT row.

**Rationale**: The current query structure ensures exactly one row per property by using a correlated subquery (
`SELECT MAX(r2.id) ...`). The spec (FR-002) requires multiple rows for the same contract when multiple receipts have
receipt dates within the selected month. Removing this restriction and restructuring the query to produce one row per
receipt naturally satisfies this.

**Alternatives considered**:

- Post-processing: load single rows then expand via Java — rejected, adds complexity and makes the SQL/Java contract
  fragile.
- Window functions: could work but H2 compatibility and query clarity favored a simpler JOIN approach.

## R3: How to compute the adjusted rent value (rent - discount + fine)

**Decision**: Compute the adjusted rent in the SQL query: `c.rent - r.discount + r.fine` for paid rows. For
unpaid/vacant rows, continue using `c.rent` (base rent).

**Rationale**: Per FR-003, the Rent Value column for paid rows must show `rent - discount + fine`. The `receipts` table
already has `discount` and `fine` columns (integers, same unit as rent — cents). Computing in SQL keeps the
`PaymentReportRow` record unchanged (it already has `Integer rent` which can hold the adjusted value). The total at the
bottom (both on-screen and PDF) should sum these adjusted values.

**Alternatives considered**:

- Adding `discount` and `fine` fields to `PaymentReportRow`: would require changes to the UI controller, PDF bean, and
  JasperReports template. Not needed since only the adjusted value is displayed.
- Computing in Java: rejected — the repository already assembles the row, computing in SQL is simpler.

## R4: How to fix contracts starting mid-month not appearing in the report

**Decision**: Change the contract start date filter from `c.start_date <= firstDay` to `c.start_date <= lastDay` (for
past months) or `c.start_date <= CURRENT_DATE` (for the current month). The simplest correct approach: use
`c.start_date <= ?` where `?` is `lastDay` of the selected month. This correctly includes contracts starting on any day
of the selected month.

**Rationale**: The current condition `c.start_date <= ?` (bound to `firstDay`) excludes contracts starting after the
1st. A contract starting June 15 fails `June 15 <= June 1`. Using `lastDay` includes all contracts that started on or
before the last day of the month. For the current month, `lastDay` may be in the future, which is fine — the end date
condition already ensures only currently-active contracts are matched.

**Alternatives considered**:

- Using `CURRENT_DATE` for current month, `lastDay` for past months: adds branching complexity. Since `lastDay` works
  correctly for both cases (future days just mean "include everything up to end of month"), a single parameter value is
  simpler.

## R5: How to order PDF rows by receipt date

**Decision**: Change the sorting in `ReportFileServiceImpl.generatePaymentReportPdf()` to sort by `pageGroup` first (
paid before unpaid/vacant), then by receipt date ascending within the paid group. The `PaymentReportRowBean` needs to
store the raw `LocalDate` receipt date for sorting, or the sort can be applied before converting to beans. The
`PaymentReportRow` already carries `paymentDate`.

**Rationale**: FR-005 requires rows ordered by receipt date ascending, with unpaid/vacant rows after paid rows. The
current sort by `pageGroup` then `propertyName` doesn't match. Sorting the `PaymentReportRow` list before bean
conversion is simpler than adding sortable fields to the bean.

**Alternatives considered**:

- Adding a sortable date field to `PaymentReportRowBean`: would work but adds unnecessary state when we can sort before
  conversion.
- Sorting in JasperReports: possible via sortField but less controllable than Java-side sorting.

## R6: Query restructuring approach

**Decision**: Rewrite `loadPaymentReportData()` as a two-step approach:

1. First, query all properties and their most recent active contract for the month (as before, but with the fixed start
   date condition).
2. For each property with an active contract, query receipts with `date` (receipt date) in the selected month range.
3. Assemble rows: one PAID row per matching receipt, one UNPAID row if no receipts matched, one VACANT row if no active
   contract.

This can be done in a single SQL query by using a LEFT JOIN that no longer restricts to a single receipt, then grouping
the results in Java (since a property with multiple receipts produces multiple result rows, but a property with no
receipts produces one NULL-receipt row).

**Rationale**: The current single-query approach with subqueries for single-receipt restriction is tightly coupled.
Removing the restriction while keeping the LEFT JOIN naturally produces the right structure. The Java code needs to
handle: if receipt columns are NULL and contract exists → UNPAID; if receipt columns are NULL and no contract → VACANT;
otherwise → PAID.

**Alternatives considered**:

- Multiple separate queries: more readable but slower due to multiple round-trips.
- The existing single-query with subquery removal: simplest change, just remove the `MAX(r2.id)` subquery restriction.
