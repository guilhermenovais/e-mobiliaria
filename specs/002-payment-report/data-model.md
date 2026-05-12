# Data Model: Payment Report

## New Domain Entities

### `PaymentReportRowStatus` (enum)

```java
package com.guilherme.emobiliaria.reports.domain.entity;

public enum PaymentReportRowStatus {
  PAID,    // contract active, receipt found covering the selected month
  UNPAID,  // contract active, no receipt covering the selected month
  VACANT   // no contract active on the first day of the selected month
}
```

---

### `PaymentReportRow` (record)

Represents one row in the payment report table — one per registered property.

```java
package com.guilherme.emobiliaria.reports.domain.entity;

import java.time.LocalDate;

public record PaymentReportRow(
    String propertyName,          // always present
    String primaryTenantName,     // null if VACANT
    String primaryTenantTaxId,    // CPF (11 digits) or CNPJ (14 digits), null if VACANT
    LocalDate paymentDate,         // receipt.date, null if UNPAID or VACANT
    Integer rent,                  // contract.rent, null if VACANT
    LocalDate periodStart,         // receipt.intervalStart, null if UNPAID or VACANT
    LocalDate periodEnd,           // receipt.intervalEnd, null if UNPAID or VACANT
    PaymentReportRowStatus status  // PAID | UNPAID | VACANT
) {}
```

**Validation rules**: None — this is a read-only computed projection from DB data. The domain record is constructed only
by the infrastructure layer.

**State transitions**: N/A — immutable record, no lifecycle.

---

## New Infrastructure DTOs

### `PaymentReportRowBean` (JasperReports DTO)

Lives in `reports/infrastructure/service/`. Mirrors `PaymentReportRow` with `String` fields for JasperReports
compatibility (JasperReports requires JavaBean-style getters, which Java records provide). Dates are pre-formatted as
`"dd/MM/yyyy"` strings.

```java
package com.guilherme.emobiliaria.reports.infrastructure.service;

public class PaymentReportRowBean {
  private final String propertyName;
  private final String primaryTenantName;   // empty string if null
  private final String primaryTenantTaxId;  // formatted CPF/CNPJ, empty string if null
  private final String paymentDate;          // "dd/MM/yyyy" or empty string
  private final String rent;                 // "R$ X.XXX,XX" or empty string
  private final String period;               // "dd/MM/yyyy – dd/MM/yyyy" or empty string
  private final String status;               // "PAGO", "EM ABERTO", "IMÓVEL VAGO"

  // constructor + getters
}
```

---

## Existing Entity Relationships (read-only context)

```
properties (1)
  └── contract_tenants → contracts (*, active on 1st of month)
        └── physical_persons / juridical_persons (primary tenant = MIN id)
        └── receipts (covering any day of selected month)
```

The SQL query for `loadPaymentReportData(YearMonth month)` performs:

1. `FROM properties p` — all properties (LEFT JOINs to handle vacant)
2. `LEFT JOIN contracts c` — contract active on `first_day_of_month`:
    - `c.start_date <= :firstDay`
    - `effective_end_date > :firstDay`
    - Most recent by `start_date` DESC when multiple match (correlated subquery)
3. `LEFT JOIN contract_tenants ct` — primary tenant (lowest `ct.id` for the contract)
4. `LEFT JOIN physical_persons pp ON pp.id = ct.tenant_id AND ct.tenant_type = 'PHYSICAL'`
5. `LEFT JOIN juridical_persons jp ON jp.id = ct.tenant_id AND ct.tenant_type = 'JURIDICAL'`
6. `LEFT JOIN receipts r` — receipt covering the month:
    - `r.contract_id = c.id`
    - `r.interval_start <= :lastDay`
    - `r.interval_end >= :firstDay`
    - Most recent if multiple receipts match (take MAX `r.id`)

**Month selector query** (`loadPaymentReportMonths()`):

```sql
SELECT MIN(start_date) AS earliest FROM contracts
```

Returns months from `YearMonth.from(earliest)` down to `YearMonth.now()` in descending order. If `earliest` is NULL,
returns `List.of(YearMonth.now())`.

---

## Extended Interfaces

### `ReportRepository` (extended)

```java
List<YearMonth> loadPaymentReportMonths();
List<PaymentReportRow> loadPaymentReportData(YearMonth month);
```

### `ReportFileService` (extended)

```java
byte[] generatePaymentReportPdf(List<PaymentReportRow> rows, YearMonth month);
```
