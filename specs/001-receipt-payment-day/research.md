# Research: Receipt Payment Day Selection

## Decision 1: Payment Due Date Computation Algorithm

**Decision**: Implement as a pure concrete class `PaymentDueDateService` in
`contract/domain/service/`. No interface needed — the service is fast, in-memory, and has no
external dependencies.

**Algorithm**:

1. Inputs: `startDate` (contract start), `paymentDay` (1–31), `today`
2. First due date: first occurrence of `paymentDay` (clamped to month end) on or after `startDate`
    - If `paymentDay >= startDate.dayOfMonth`: clamp `paymentDay` to `startDate`'s month length, use
      same month
    - Else: advance one month, clamp `paymentDay` to that month's length
3. Subsequent due dates: advance one month at a time, clamp `paymentDay` to each month's length
4. Stop when the computed date is strictly after `today`
5. Returns a `List<LocalDate>`, possibly empty

**Month-end clamping**: `Math.min(paymentDay, YearMonth.of(year, month).lengthOfMonth())` — same
pattern already used in `JdbcDashboardRepository.computeDeadline()`.

**Rationale**: Purely about contract obligations (startDate, paymentDay → due dates). Following
`domain-services.md`: "if the implementation runs in memory and is fast, only the real
implementation should be implemented." Placed in `contract/domain/service/` because the computation
is a property of the contract, not of receipts.

**Alternatives considered**:

- `receipt/domain/service/` — rejected; computation depends only on contract attributes
- Inline SQL computation — rejected per FR-010 (must be centralized and reused across surfaces)

---

## Decision 2: Legacy Receipt Migration Algorithm

**Decision**: For each existing receipt, derive `payment_due_date` as:

> occurrence of `contract.payment_day` in the same month as `interval_start`, clamped to that
> month's last day

```sql
-- H2-compatible: TRUNC(date, 'MM') is not available; use DATEADD + EXTRACT
-- clamp(paymentDay, monthOf(interval_start))
UPDATE receipts r
SET payment_due_date = CASE
    WHEN c.payment_day <= DAY(DATEADD('DAY', -1, DATEADD('MONTH', 1,
                               DATEADD('DAY', -(DAY(r.interval_start)-1), r.interval_start))))
    THEN DATEADD('DAY', c.payment_day - 1,
                 DATEADD('DAY', -(DAY(r.interval_start)-1), r.interval_start))
    ELSE DATEADD('DAY', -1, DATEADD('MONTH', 1,
                  DATEADD('DAY', -(DAY(r.interval_start)-1), r.interval_start)))
END
FROM contracts c WHERE r.contract_id = c.id;
```

The old receipt form pre-filled `intervalStart` as the next occurrence of
`contractStartDate.getDayOfMonth()` — close to, but not identical to, `paymentDay`. Deriving from
`paymentDay` clamped to the `interval_start` month gives the most accurate retroactive mapping.

**Duplicate handling**: The UNIQUE constraint is added after the backfill. If the backfill produces
duplicate `(contract_id, payment_due_date)` pairs for existing data, the migration will fail loudly,
which is acceptable — it signals a data quality issue that must be resolved before deployment. In
practice, duplicates are unlikely because the old form prevented overlapping intervals.

**Alternatives considered**:

- Using `interval_start` directly — rejected; it conflates billing period start with payment
  obligation date
- Using the `date` (payment-received date) — rejected; that records when payment happened, not when
  it was owed

---

## Decision 3: Uniqueness Enforcement Strategy

**Decision**: Two-layer enforcement:

1. **DB level**: `UNIQUE (contract_id, payment_due_date)` constraint added in V8 migration
2. **Application level**: `ReceiptRepository.existsByContractAndPaymentDueDate()` checked in
   `CreateReceiptInteractor` and `EditReceiptInteractor`, throwing `BusinessException` with a
   user-friendly message

**Rationale**: DB constraint prevents race conditions; application check provides a meaningful error
before the DB constraint violation is reached.

---

## Decision 4: Active Contract Check Responsibility

**Decision**: `PaymentDueDateService.computeDueDates()` is a pure function that does not check
contract status. Callers are responsible for only passing active contracts.

`GetUnreceiptedDueDatesInteractor` checks status (ACTIVE/EXPIRING) before calling the service. The
dashboard's SQL CTE already filters active contracts before the Java-level computation.

**Rationale**: Keeps the service a simple, easily testable pure function.

---

## Decision 5: New Use Case for Receipt Form

**Decision**: Add `GetUnreceiptedDueDatesInteractor` that accepts `(contractId, today,
excludeReceiptId?)` and returns unreceipted payment due dates.

- **Create mode**: `excludeReceiptId = null` — all receipted dates excluded
- **Edit mode**: `excludeReceiptId = currentReceiptId` — excludes that receipt's due date from
  the "already receipted" set, making it selectable again

**Logic**:

1. Load contract; return empty list if not found or not active
2. `PaymentDueDateService.computeDueDates(contract.startDate, contract.paymentDay, today)`
3. Load receipted dates: `ReceiptRepository.findAllPaymentDueDatesByContractId(contractId)`
4. If `excludeReceiptId != null`: load that receipt, remove its due date from the receipted set
5. Return computed dates minus receipted set

---

## Decision 6: Interval Pre-fill Rule

**Decision**: When the user selects payment due date `D`:

- `intervalStart = D`
- `intervalEnd = D.plusMonths(1).minusDays(1)`

Since `D` is itself the clamped occurrence of `paymentDay`, it satisfies FR-013 ("interval start =
nearest occurrence of payment-day-of-month on or before D"). No additional clamping needed.

---

## Decision 7: Dashboard Migration Approach

**Decision**: Replace the current interval-overlap check in `JdbcDashboardRepository.loadUnpaidRents()`
with a query that:

1. Fetches active contracts (existing CTE unchanged)
2. For each active contract, computes payment due dates in Java via `PaymentDueDateService`
3. Batch-loads all receipted `payment_due_date` values for those contracts in **one SQL query**:
   `SELECT contract_id, payment_due_date FROM receipts WHERE contract_id IN (?,...)`
4. Filters in Java: due dates with no matching receipt entry = unreceipted

This eliminates the N+1 query pattern of the current implementation and uses the new column.

**The old `computePeriodStart` / `computeDeadline` static methods in `JdbcDashboardRepository`** are
removed — they embodied the old algorithm that the new feature replaces.

---

## Decision 8: Reports Scope

**Decision**: The `reports` module currently contains only rent evolution and occupation rate
reports — neither surfaces outstanding/unpaid rents. FR-011 is satisfied by design: the reports draw
on `receipts.payment_due_date` whenever they read receipt data, which is now accurate. No
report-specific code changes are required for this feature.

If a future report explicitly surfaces pending obligations, it will naturally use
`PaymentDueDateService` at that time.
