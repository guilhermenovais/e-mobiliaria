# Data Model: Receipt Payment Day Selection

## Entity Changes

### Receipt — `receipt/domain/entity/Receipt.java` (MODIFIED)

**New field**: `paymentDueDate: LocalDate`

The calendar date on which rent was owed under the contract. This is the primary link between a
receipt and a specific payment obligation.

**Validation rule**: Must not be null.

**Uniqueness invariant**: A `(contract, paymentDueDate)` pair must be unique system-wide. Enforced
by DB constraint and application-level check.

**Updated factory signatures**:

```java
// New: paymentDueDate parameter inserted after date
public static Receipt create(
    LocalDate date,
    LocalDate paymentDueDate,    // NEW
    LocalDate intervalStart,
    LocalDate intervalEnd,
    int discount, int fine, String observation,
    Contract contract)

public static Receipt restore(
    Long id,
    LocalDate date,
    LocalDate paymentDueDate,    // NEW
    LocalDate intervalStart,
    LocalDate intervalEnd,
    int discount, int fine, String observation,
    Contract contract)
```

**No new entity**: "Payment Due Date" is a computed value, not a stored entity. It is derived from
`contract.startDate` and `contract.paymentDay` on demand.

---

## New Domain Service

### PaymentDueDateService — `contract/domain/service/PaymentDueDateService.java` (NEW)

Concrete class (no interface) — fast, in-memory computation.

```java
public class PaymentDueDateService {
    /** Returns all payment due dates from startDate through today (inclusive), ascending. */
    public List<LocalDate> computeDueDates(LocalDate startDate, int paymentDay, LocalDate today)
}
```

**Algorithm detail**:

1. Compute `firstDueDate`:
    - `int clampedDay = Math.min(paymentDay, startDate.lengthOfMonth())`
    - Candidate in same month: `startDate.withDayOfMonth(clampedDay)`
    - If candidate is before `startDate`, advance one month:
      `candidate = candidate.plusMonths(1).withDayOfMonth(Math.min(paymentDay, nextMonth.lengthOfMonth()))`
2. Iterate: while `current <= today`, add to list; advance `current` by one month with clamping
3. Return list (may be empty if `today < firstDueDate`)

**Test coverage required** (see spec acceptance scenarios):

- Contract started 10/01/2026, paymentDay=15, today=04/05/2026 → [15/01, 15/02, 15/03, 15/04, 15/05]
- Contract started 26/03/2026, paymentDay=5, today=04/05/2026 → [05/04, 05/05]
- paymentDay=31 in February → clamped to 28/02 (or 29/02 in leap year)
- Contract started same day as payment day → first due date is that day (start counts)

---

## New Use Case

### GetUnreceiptedDueDatesInteractor — `receipt/application/usecase/` (NEW)

```java
// Input record
public record GetUnreceiptedDueDatesInput(
    Long contractId,
    LocalDate today,
    Long excludeReceiptId   // nullable; pass current receipt ID in edit mode
) {}

// Output record
public record GetUnreceiptedDueDatesOutput(
    List<LocalDate> dueDates   // sorted ascending, empty if none
) {}
```

**Logic**:

1. Load contract via `ContractRepository.findById(contractId)` — throw if not found
2. If contract has no active status (RESCINDED, INACTIVE, EXPIRED): return empty list
3. Compute all due dates: `paymentDueDateService.computeDueDates(contract.startDate, contract.paymentDay, today)`
4. Load already-receipted dates: `receiptRepository.findAllPaymentDueDatesByContractId(contractId)`
5. If `excludeReceiptId != null`: load that receipt, remove its `paymentDueDate` from the receipted set
6. Return `computedDates - receiptedSet` (sorted)

---

## ReceiptRepository Interface — `receipt/domain/repository/ReceiptRepository.java` (MODIFIED)

New methods:

```java
/** True if any receipt (other than excludeId) already covers this due date for this contract. */
boolean existsByContractAndPaymentDueDate(Long contractId, LocalDate paymentDueDate, Long excludeReceiptId);

/** All payment due dates with an existing receipt for this contract. */
List<LocalDate> findAllPaymentDueDatesByContractId(Long contractId);
```

**Note**: `existsByContractAndPaymentDueDate` always takes `excludeReceiptId` (pass null for create
mode). This avoids two separate method signatures.

---

## Application Input/Output Changes

### CreateReceiptInput (MODIFIED)

```java
public record CreateReceiptInput(
    LocalDate date,
    LocalDate paymentDueDate,   // NEW
    LocalDate intervalStart,
    LocalDate intervalEnd,
    int discount, int fine, String observation,
    Long contractId
) {}
```

### EditReceiptInput (MODIFIED)

```java
public record EditReceiptInput(
    Long id,
    LocalDate date,
    LocalDate paymentDueDate,   // NEW
    LocalDate intervalStart,
    LocalDate intervalEnd,
    int discount, int fine, String observation,
    Long contractId
) {}
```

---

## Database Schema

### Migration V8 — `src/main/resources/db/migration/V8__add_receipt_payment_due_date.sql` (NEW)

```sql
-- 1. Add nullable column
ALTER TABLE receipts ADD COLUMN payment_due_date DATE;

-- 2. Backfill: use paymentDay clamped to the month of interval_start
--    H2 expression for "first day of interval_start's month":
--    DATEADD('DAY', -(DAY(interval_start)-1), interval_start)
--    Last day of that month:
--    DATEADD('DAY', -1, DATEADD('MONTH', 1, first_of_month))
UPDATE receipts r
SET payment_due_date = (
    SELECT
        CASE WHEN c.payment_day <=
                  DAY(DATEADD('DAY', -1, DATEADD('MONTH', 1,
                      DATEADD('DAY', -(DAY(r.interval_start)-1), r.interval_start))))
             THEN DATEADD('DAY', c.payment_day - 1,
                      DATEADD('DAY', -(DAY(r.interval_start)-1), r.interval_start))
             ELSE DATEADD('DAY', -1, DATEADD('MONTH', 1,
                      DATEADD('DAY', -(DAY(r.interval_start)-1), r.interval_start)))
        END
    FROM contracts c
    WHERE c.id = r.contract_id
);

-- 3. Make non-nullable
ALTER TABLE receipts ALTER COLUMN payment_due_date DATE NOT NULL;

-- 4. Enforce uniqueness
ALTER TABLE receipts ADD CONSTRAINT uq_receipt_contract_payment_due_date
    UNIQUE (contract_id, payment_due_date);
```

**Note**: Step 4 will fail if any two legacy receipts derive the same `payment_due_date` for the
same contract. This is intentional — it surfaces a data quality issue before deployment.
