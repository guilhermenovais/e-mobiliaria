# Quickstart: Payment Report Implementation

## Where to Start

All code goes inside the existing `reports` module. No new top-level packages, no new DI modules, no schema migrations.

```
src/main/java/com/guilherme/emobiliaria/reports/
src/main/resources/com/guilherme/emobiliaria/reports/ui/view/
src/main/resources/reports/
```

---

## Implementation Order

### 1. Domain layer (no dependencies, start here)

Create in `reports/domain/entity/`:

- `PaymentReportRowStatus.java` — enum `{ PAID, UNPAID, VACANT }`
- `PaymentReportRow.java` — Java record (see `data-model.md`)

Extend interfaces:

- `ReportRepository.java` — add `loadPaymentReportMonths()` and `loadPaymentReportData(YearMonth)`
- `ReportFileService.java` — add `generatePaymentReportPdf(List<PaymentReportRow>, YearMonth)`

### 2. Application layer (depends only on domain)

Create in `reports/application/input/`:

- `GetPaymentReportMonthsInput.java` — `public record GetPaymentReportMonthsInput() {}`
- `LoadPaymentReportInput.java` — `public record LoadPaymentReportInput(YearMonth month) {}`
- `GeneratePaymentReportPdfInput.java` — `public record GeneratePaymentReportPdfInput(YearMonth month) {}`

Create in `reports/application/output/`:

- `GetPaymentReportMonthsOutput.java` — `public record GetPaymentReportMonthsOutput(List<YearMonth> months) {}`
- `LoadPaymentReportOutput.java` — `public record LoadPaymentReportOutput(List<PaymentReportRow> rows) {}`
- `GeneratePaymentReportPdfOutput.java` — `public record GeneratePaymentReportPdfOutput(byte[] pdfBytes) {}`

Create in `reports/application/usecase/`:

```java
// GetPaymentReportMonthsInteractor
@Inject
public GetPaymentReportMonthsInteractor(ReportRepository reportRepository) { ... }

public GetPaymentReportMonthsOutput execute(GetPaymentReportMonthsInput input) {
    return new GetPaymentReportMonthsOutput(reportRepository.loadPaymentReportMonths());
}
```

```java
// LoadPaymentReportInteractor
@Inject
public LoadPaymentReportInteractor(ReportRepository reportRepository) { ... }

public LoadPaymentReportOutput execute(LoadPaymentReportInput input) {
    return new LoadPaymentReportOutput(reportRepository.loadPaymentReportData(input.month()));
}
```

```java
// GeneratePaymentReportPdfInteractor
@Inject
public GeneratePaymentReportPdfInteractor(ReportRepository reportRepository,
                                           ReportFileService reportFileService) { ... }

public GeneratePaymentReportPdfOutput execute(GeneratePaymentReportPdfInput input) {
    List<PaymentReportRow> rows = reportRepository.loadPaymentReportData(input.month());
    byte[] pdf = reportFileService.generatePaymentReportPdf(rows, input.month());
    return new GeneratePaymentReportPdfOutput(pdf);
}
```

### 3. Infrastructure — Repository (`JdbcReportRepository`)

**`loadPaymentReportMonths()`**:

```sql
SELECT MIN(start_date) AS earliest FROM contracts
```

Generate months from `YearMonth.from(earliest)` to `YearMonth.now()`. Collect into a list, then reverse for descending
order. If `earliest` is null, return `List.of(YearMonth.now())`.

**`loadPaymentReportData(YearMonth month)`**:

Core SQL (H2 dialect):

```sql
SELECT
    p.name                                     AS property_name,
    COALESCE(pp.name, jp.corporate_name)       AS tenant_name,
    COALESCE(pp.cpf, jp.cnpj)                  AS tenant_tax_id,
    r.date                                     AS payment_date,
    c.rent                                     AS rent,
    r.interval_start                           AS period_start,
    r.interval_end                             AS period_end
FROM properties p

-- Active contract on first day of month (most recent if overlap)
LEFT JOIN contracts c ON c.property_id = p.id
    AND c.start_date <= :firstDay
    AND CASE WHEN c.rescinded_at IS NOT NULL
             THEN c.rescinded_at
             ELSE DATEADD('MONTH',
                    CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                    c.start_date)
        END > :firstDay
    AND c.id = (
        SELECT c2.id FROM contracts c2
        WHERE c2.property_id = p.id
          AND c2.start_date <= :firstDay
          AND CASE WHEN c2.rescinded_at IS NOT NULL
                   THEN c2.rescinded_at
                   ELSE DATEADD('MONTH',
                          CAST(SUBSTRING(c2.duration, 2, LENGTH(c2.duration) - 2) AS INT),
                          c2.start_date)
              END > :firstDay
        ORDER BY c2.start_date DESC, c2.id DESC
        LIMIT 1
    )

-- Primary tenant (first by insertion order)
LEFT JOIN contract_tenants ct ON ct.contract_id = c.id
    AND ct.id = (SELECT MIN(id) FROM contract_tenants WHERE contract_id = c.id)
LEFT JOIN physical_persons pp  ON pp.id = ct.tenant_id AND ct.tenant_type = 'PHYSICAL'
LEFT JOIN juridical_persons jp ON jp.id = ct.tenant_id AND ct.tenant_type = 'JURIDICAL'

-- Most recent receipt covering the selected month
LEFT JOIN receipts r ON r.contract_id = c.id
    AND r.interval_start <= :lastDay
    AND r.interval_end   >= :firstDay
    AND r.id = (
        SELECT MAX(r2.id) FROM receipts r2
        WHERE r2.contract_id = c.id
          AND r2.interval_start <= :lastDay
          AND r2.interval_end   >= :firstDay
    )

ORDER BY p.name
```

Bind `:firstDay = month.atDay(1)` and `:lastDay = month.atEndOfMonth()`.

Map result to `PaymentReportRow`:

- `c.id IS NULL` → status = `VACANT`, tenant fields + payment fields null
- `c.id IS NOT NULL && r.id IS NULL` → status = `UNPAID`, payment fields null
- `c.id IS NOT NULL && r.id IS NOT NULL` → status = `PAID`

### 4. Infrastructure — PDF Service

**`PaymentReportRowBean.java`** in `reports/infrastructure/service/`:

- Plain Java class with private final fields + constructor + getters
- Fields: `propertyName`, `primaryTenantName`, `primaryTenantTaxId`, `paymentDate` (formatted string), `rent` (money
  string), `period` (formatted range string), `statusLabel`

**`ReportFileServiceImpl.generatePaymentReportPdf(...)`**:

- Format `YearMonth` as e.g. `"maio/2026"` (PT-BR) for the report title
- Map `List<PaymentReportRow>` → `List<PaymentReportRowBean>`
- Load `payment_report.jrxml` via `PdfGenerationService` (same pattern as `generateOccupationRatePdf`)
- Pass `JRBeanCollectionDataSource` of beans

**`payment_report.jrxml`**:

- Title: `"Relatório de Pagamentos — {MONTH_LABEL}"`
- Single detail band with 7 columns: Imóvel | Locatário | CPF/CNPJ | Data de Pagamento | Valor do Aluguel | Período
- No sub-reports needed (flat list)
- Use a `REPORT_PARAMETERS_MAP` parameter for the title month label

### 5. UI Layer

**`PaymentReportController.java`**:

```java
@Inject
public PaymentReportController(
    GetPaymentReportMonthsInteractor getMonths,
    LoadPaymentReportInteractor loadReport,
    GeneratePaymentReportPdfInteractor generatePdf,
    GuiceFxmlLoader fxmlLoader) { ... }
```

`initialize()`:

1. Load months via `getMonths.execute(...)` → populate `ComboBox<YearMonth>` (custom `StringConverter` to format as "
   Mês/YYYY" in PT-BR)
2. Set initial selection to first item (most recent month)
3. Add listener on combo box: on change, call `loadReport.execute(...)` → populate `TableView<PaymentReportRow>`
4. Wire "Generate PDF" button to `generatePdf.execute(...)` → write temp file → `Desktop.open`

Row coloring via `TableRow` cell factory: red for `UNPAID`, light gray for `VACANT`, default for `PAID`.

**`payment-report-view.fxml`**:

- `VBox` root with `ComboBox` (month selector) + `TableView` + `Button` (Generate PDF) + `ProgressIndicator`
- Six `TableColumn` items matching `PaymentReportRow` fields

**`ReportsController.java`** (extension):

- Add a new report card/button "Relatório de Pagamentos" in the existing reports list view
- On click: `navigationService.navigate(() -> paymentReportController.buildView(), "reports")`
  (or load a sub-view inline — follow the existing pattern for how Reports screen is organized)

**`module-info.java`**: No new packages to open — `reports.ui.controller` is already opened to
`javafx.fxml, com.google.guice`.

---

## Testing Strategy

### Unit tests (no DB, no UI)

| Test class                               | What it tests                                    |
|------------------------------------------|--------------------------------------------------|
| `GetPaymentReportMonthsInteractorTest`   | Month list generation, empty-DB edge case        |
| `LoadPaymentReportInteractorTest`        | Delegates to repository, returns output          |
| `GeneratePaymentReportPdfInteractorTest` | Delegates to repository + service, returns bytes |

Use fake repository (`FakeReportRepository` extending existing test infrastructure or a new simple implementation in
`test/`).

### Integration test

`JdbcReportRepositoryPaymentReportTest` (or extend existing `JdbcReportRepositoryTest` if one exists):

- Uses in-memory H2 with Flyway migrations applied
- Seeds properties, contracts, receipts
- Asserts: correct `PAID`/`UNPAID`/`VACANT` rows, correct primary tenant, correct date fields, correct ordering

---

## i18n Keys to Add (`messages.properties`)

```properties
reports.payment_report.name=Relatório de Pagamentos
reports.payment_report.description=Visualize o status de pagamento de todos os imóveis por mês.
reports.payment_report.month_selector.label=Mês:
reports.payment_report.column.property=Imóvel
reports.payment_report.column.tenant=Locatário
reports.payment_report.column.tax_id=CPF/CNPJ
reports.payment_report.column.payment_date=Data de Pagamento
reports.payment_report.column.rent=Valor do Aluguel
reports.payment_report.column.period=Período
reports.payment_report.vacant_label=Imóvel vago
reports.payment_report.button.generate_pdf=Exportar PDF
```
