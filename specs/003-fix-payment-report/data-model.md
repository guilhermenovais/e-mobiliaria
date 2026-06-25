# Data Model: Fix Payment Report

## Existing Entities (no schema changes)

### Receipt (receipt feature)

| Field          | Type      | Description                              |
|----------------|-----------|------------------------------------------|
| id             | Long      | Primary key                              |
| date           | LocalDate | Receipt date (when payment was recorded) |
| paymentDueDate | LocalDate | When the payment was due                 |
| intervalStart  | LocalDate | Start of the period covered              |
| intervalEnd    | LocalDate | End of the period covered                |
| discount       | int       | Discount in cents                        |
| fine           | int       | Fine/surcharge in cents                  |
| observation    | String    | Free text observation                    |
| contract       | Contract  | Associated contract                      |

### Contract (contract feature)

| Field     | Type      | Description                               |
|-----------|-----------|-------------------------------------------|
| id        | Long      | Primary key                               |
| startDate | LocalDate | Contract start date                       |
| duration  | Period    | Contract duration                         |
| rent      | int       | Monthly rent in cents                     |
| ...       | ...       | Other fields not relevant to this feature |

### PaymentReportRow (reports feature — domain record)

| Field              | Type                   | Description                            |
|--------------------|------------------------|----------------------------------------|
| propertyName       | String                 | Name of the property                   |
| primaryTenantName  | String                 | Primary tenant's name (null if vacant) |
| primaryTenantTaxId | String                 | Tenant's CPF/CNPJ (null if vacant)     |
| paymentDate        | LocalDate              | Receipt date (null if unpaid/vacant)   |
| rent               | Integer                | Rent value in cents (null if vacant)   |
| periodStart        | LocalDate              | Period start (null if unpaid/vacant)   |
| periodEnd          | LocalDate              | Period end (null if unpaid/vacant)     |
| status             | PaymentReportRowStatus | PAID, UNPAID, or VACANT                |

**No schema changes to this record.** The `rent` field will now carry the adjusted value (
`contractRent - discount + fine`) for PAID rows, and the base `contractRent` for UNPAID rows.

## Behavioral Changes

### Grouping Logic

- **Before**: Receipts matched to months by `payment_due_date` (the due date of the payment).
- **After**: Receipts matched to months by `date` (the date the receipt was recorded).

### Multiplicity

- **Before**: At most one receipt per contract per month (enforced by `MAX(r2.id)` subquery).
- **After**: Multiple receipts per contract per month allowed. Each receipt with a `date` in the selected month produces
  one PAID row.

### Rent Value

- **Before**: `rent` always holds `contract.rent` (base rent).
- **After**: For PAID rows, `rent` holds `contract.rent - receipt.discount + receipt.fine`. For UNPAID rows, `rent`
  holds `contract.rent`.

### Contract Visibility

- **Before**: Contract must have `start_date <= firstDayOfMonth` to appear.
- **After**: Contract must have `start_date <= lastDayOfMonth` to appear.

### Row Ordering

- **Before**: PDF sorts by `pageGroup` (paid/unpaid) then `propertyName`.
- **After**: PDF sorts by `pageGroup` (paid/unpaid), then receipt date ascending within paid group. Unpaid/vacant rows
  sorted by property name.

## Database Tables (no DDL changes)

No database schema changes required. All changes are to query logic and Java application code.
