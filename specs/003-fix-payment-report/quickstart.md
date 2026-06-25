# Quickstart: Fix Payment Report

## What This Feature Changes

This feature fixes the payment report in four areas:

1. Payments grouped by receipt date (when the receipt was recorded) instead of payment due date
2. The Rent Value column shows the adjusted amount (rent - discount + fine) for paid rows
3. PDF rows are ordered by receipt date ascending (paid first, then unpaid/vacant)
4. Contracts starting mid-month now appear in the current month's report

## Key Files to Modify

### Repository Layer (query logic)

- `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepository.java`
    - Method: `loadPaymentReportData(YearMonth month)` — rewrite the SQL query

### Service Layer (PDF generation)

- `src/main/java/com/guilherme/emobiliaria/reports/infrastructure/service/ReportFileServiceImpl.java`
    - Method: `generatePaymentReportPdf()` — change row sorting logic

### UI Layer (on-screen total)

- `src/main/java/com/guilherme/emobiliaria/reports/ui/controller/PaymentReportController.java`
    - Method: `loadTableData()` — the paid total already sums `rent()` values, which will now contain adjusted values.
      No change needed if the repository returns the correct adjusted rent.

### Tests

- `src/test/java/com/guilherme/emobiliaria/reports/infrastructure/repository/JdbcReportRepositoryTest.java`
    - Add/update integration tests for new query behavior
- `src/test/java/com/guilherme/emobiliaria/reports/infrastructure/service/ReportFileServiceImplTest.java`
    - Update sorting tests

## Files That Should NOT Change

- `PaymentReportRow.java` — the record stays the same, `rent` now carries adjusted value
- `PaymentReportRowStatus.java` — no new statuses
- `ReportRepository.java` — interface signature unchanged
- `LoadPaymentReportInteractor.java` — pass-through, no logic changes
- `payment_report.jrxml` — JasperReports template unchanged, data already formatted by `PaymentReportRowBean`
- `PaymentReportRowBean.java` — no changes needed, receives already-adjusted data
- `PaymentReportTemplate.java` — no changes needed

## Build & Test

```bash
mvn test -pl . -Dtest="JdbcReportRepositoryTest"
mvn test -pl . -Dtest="ReportFileServiceImplTest"
mvn test  # full suite
```
