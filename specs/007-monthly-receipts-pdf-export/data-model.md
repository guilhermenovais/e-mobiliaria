# Phase 1 Data Model: Monthly Receipts PDF Export

No new persisted tables or columns. This feature adds transient, in-memory value objects only.

## Receipt (existing entity — unmodified)

`com.guilherme.emobiliaria.receipt.domain.entity.Receipt` is unchanged. Its `date` field (receipt date, distinct
from `paymentDueDate`, `intervalStart`, `intervalEnd`) is the field this feature filters by, per FR-003/FR-005 and
the spec's explicit statement that inclusion is "based on their Receipt Date."

## Export Month Selection (transient, not persisted)

Represented directly as `java.time.YearMonth` — no new type needed. Scoped to the lifetime of a single export
dialog interaction; never written to the database or any settings file.

- **Fields**: year (int), month (1–12) — both carried by `YearMonth`.
- **Validation**: must be one of the months returned by `findAllReceiptMonths()`; the UI enforces this by only ever
  populating the selector `ComboBox<YearMonth>` from that query's result, so no separate validation layer is
  required in the use case beyond "month has zero matching receipts → nothing to export" (not an error case, see
  Edge Cases).

## ReceiptExportResult (new domain-layer record)

```java
package com.guilherme.emobiliaria.receipt.domain.entity;

public record ReceiptExportResult(int exportedCount, List<FailedExport> failures) {
  public record FailedExport(Long receiptId, String reason) {}
}
```

- **Fields**:
    - `exportedCount` — number of receipts whose PDF was successfully written to the destination folder.
    - `failures` — list of `(receiptId, reason)` pairs for receipts skipped due to a generation/write error other
      than the destination folder being unwritable. Empty list when everything succeeds.
- **Relationships**: `FailedExport.receiptId` references `Receipt.id`; no cascading or persistence implications
  since neither the result nor its failures are stored.
- **State transitions**: none — constructed once at the end of `ExportReceiptsByMonthInteractor.execute(...)` and
  returned to the UI layer for display in a completion summary (FR-009).
- **Validation rules**: `exportedCount >= 0`; `failures` never null (empty list, not null, when there are no
  failures) — enforced by construction in the interactor, no setters exist (record).

## Application layer input/output shapes

```java
// input
public record ExportReceiptsByMonthInput(YearMonth month, Path destinationFolder) {}

// outputs
public record ExportReceiptsByMonthOutput(ReceiptExportResult result) {}
public record GetExportableReceiptMonthsOutput(List<YearMonth> months) {}
```

`GetExportableReceiptMonthsInteractor.execute()` takes no input record — it queries the full set of receipt months
with no filter to express, so a parameterless method is simpler than introducing an empty input type.
