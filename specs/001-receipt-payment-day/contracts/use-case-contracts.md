# UI Contracts: Receipt Payment Day Selection

These contracts describe the data exchanged between the UI layer and the application layer for all
changed or new use cases in this feature.

---

## GetUnreceiptedDueDates (NEW)

**Called by**: `ReceiptFormController` on contract selection (create mode) or form load (edit mode)

**Input**:

```
contractId      Long       required — which contract to compute due dates for
today           LocalDate  required — boundary date (due dates after today excluded)
excludeReceiptId Long      nullable — receipt whose due date should be re-included (edit mode)
```

**Output**:

```
dueDates  List<LocalDate>  sorted ascending; empty if contract is inactive or all dates receipted
```

**Error cases**:

- Contract not found → `BusinessException(Contract.NOT_FOUND)` — shown as user-facing error
- No due dates available → empty list (UI shows "no outstanding payment days" message)

---

## CreateReceipt (MODIFIED)

Added field: `paymentDueDate: LocalDate`

**Input**:

```
date            LocalDate  required — date payment was received
paymentDueDate  LocalDate  required — NEW: the due date this receipt covers
intervalStart   LocalDate  required — billing interval start (auto-filled, user may override)
intervalEnd     LocalDate  required — billing interval end (auto-filled, user may override)
discount        int        cents (≥ 0)
fine            int        cents (≥ 0)
observation     String     nullable
contractId      Long       required
```

**Output**:

```
receipt  Receipt  the created receipt (with generated ID)
```

**New error cases**:

- `paymentDueDate` already has a receipt for this contract → `BusinessException(Receipt.DUPLICATE_PAYMENT_DUE_DATE)`

---

## EditReceipt (MODIFIED)

Added field: `paymentDueDate: LocalDate`

**Input**:

```
id              Long       required — receipt to update
date            LocalDate  required
paymentDueDate  LocalDate  required — NEW
intervalStart   LocalDate  required
intervalEnd     LocalDate  required
discount        int
fine            int
observation     String     nullable
contractId      Long       required
```

**Output**:

```
receipt  Receipt  the updated receipt
```

**New error cases**:

- `paymentDueDate` already has a different receipt for this contract →
  `BusinessException(Receipt.DUPLICATE_PAYMENT_DUE_DATE)`

---

## UI State Machine: Receipt Form

```
[Load Form]
    │
    ├─[Create Mode]
    │    │
    │    ▼
    │  Load contracts list (FindAllContracts)
    │    │
    │    ▼
    │  [User selects contract]
    │    │
    │    ▼
    │  GetUnreceiptedDueDates(contractId, today, null)
    │    │
    │    ├─ empty list → show "no outstanding payment days" info, disable submit
    │    │
    │    └─ dates list → populate payment day ComboBox
    │         │
    │         ▼
    │       [User selects payment due date D]
    │         │
    │         ▼
    │       Auto-fill: intervalStart = D, intervalEnd = D + 1 month − 1 day
    │         (user may override interval fields)
    │
    └─[Edit Mode]
         │
         ▼
       Load receipt (FindReceiptById) + contracts list
         │
         ▼
       GetUnreceiptedDueDates(contractId, today, currentReceiptId)
         │
         ▼
       Pre-select current receipt's paymentDueDate in ComboBox
         │
         ▼
       Pre-fill date, interval fields, discount, fine, observation
         │
         ▼
       [User changes payment due date D]  ← triggers interval auto-fill (overwrites)
```
