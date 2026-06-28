# Implementation Plan: Receipt Payment Proofs

**Branch**: `004-receipt-payment-proofs` | **Date**: 2026-06-28 | **Spec**: `specs/004-receipt-payment-proofs/spec.md`
**Input**: Feature specification from `/specs/004-receipt-payment-proofs/spec.md`

## Summary

Allow users to attach one or more proof-of-payment files (PDF or image) to a receipt via drag-drop, file picker, or
clipboard paste; view proofs from the receipts list via a toggled button; and remove proofs while editing. Files are
copied into an app-managed `proofs/` directory; metadata is stored in a new `payment_proofs` DB table. The feature adds
a `PaymentProof` entity and two service/repository interfaces to the `receipt` module, extends the receipts list
`ActionsCell` with a proof button, and adds a drop-zone component and selection dialog to the receipt form.

## Technical Context

**Language/Version**: Java 24  
**Primary Dependencies**: JavaFX (FXML + controls), Google Guice 7, H2 (SQL via JDBC), Flyway (migrations), JUnit
5.12.1  
**Storage**: H2 file-based DB (existing) + local filesystem `proofs/` directory under `AppDataPaths`  
**Testing**: JUnit 5, in-memory Fake repositories/services (domain/application), H2 in-memory + Flyway (infrastructure
JDBC tests)  
**Target Platform**: Windows desktop (JavaFX, `java.desktop` already in module-info)  
**Project Type**: Desktop app (JavaFX)  
**Performance Goals**: N/A — proof attach/open is single-file, infrequent  
**Constraints**: Files must be copied to app-managed storage; extension-only file type validation; no cloud sync  
**Scale/Scope**: Single-user desktop; typical receipt lists are 20 items/page; proofs per receipt are typically 1–5
files

## Constitution Check

Constitution file is a blank template with no filled-in principles. No gate violations to check. Architecture gates from
`general-instructions.md`:

| Gate                                           | Status                                                                                |
|------------------------------------------------|---------------------------------------------------------------------------------------|
| Domain layer has no UI/framework deps          | PASS — all new domain classes use only `java.time`, `java.util`, `java.nio.file.Path` |
| Application layer has no business rules        | PASS — interactors orchestrate; rules enforced in `PaymentProof` entity               |
| Infrastructure/UI don't enforce business rules | PASS — repository and service are pure I/O                                            |
| Constructor injection throughout               | PASS — all new classes use `@Inject` constructor                                      |
| Feature module registered in AppModule         | PASS — bindings added to existing `ReceiptModule`                                     |

## Project Structure

### Documentation (this feature)

```text
specs/004-receipt-payment-proofs/
├── plan.md              # This file
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks)
```

### Source Code — new files

```text
src/main/java/com/guilherme/emobiliaria/receipt/
├── domain/
│   ├── entity/
│   │   ├── PaymentProof.java             [NEW]
│   │   └── ProofFileType.java            [NEW]
│   ├── repository/
│   │   └── PaymentProofRepository.java   [NEW]
│   └── service/
│       └── PaymentProofStorageService.java [NEW]
│
├── application/
│   ├── input/
│   │   ├── AttachPaymentProofInput.java  [NEW]
│   │   └── RemovePaymentProofInput.java  [NEW]
│   ├── output/
│   │   ├── AttachPaymentProofOutput.java [NEW]
│   │   └── FindPaymentProofsByReceiptIdOutput.java [NEW]
│   └── usecase/
│       ├── AttachPaymentProofInteractor.java  [NEW]
│       ├── RemovePaymentProofInteractor.java  [NEW]
│       └── FindPaymentProofsByReceiptIdInteractor.java [NEW]
│
├── infrastructure/
│   ├── repository/
│   │   └── JdbcPaymentProofRepository.java [NEW]
│   └── service/
│       └── PaymentProofStorageServiceImpl.java [NEW]
│
└── ui/
    ├── component/
    │   └── ProofDropZonePane.java        [NEW]
    └── controller/
        └── ProofSelectionDialog.java     [NEW]

src/main/resources/db/migration/
└── V10__add_payment_proofs.sql           [NEW]

src/test/java/com/guilherme/emobiliaria/receipt/
├── domain/
│   ├── entity/
│   │   └── PaymentProofTest.java         [NEW]
│   ├── repository/
│   │   └── FakePaymentProofRepository.java [NEW]
│   └── service/
│       └── FakePaymentProofStorageService.java [NEW]
├── application/usecase/
│   ├── AttachPaymentProofInteractorTest.java  [NEW]
│   ├── RemovePaymentProofInteractorTest.java  [NEW]
│   └── FindPaymentProofsByReceiptIdInteractorTest.java [NEW]
└── infrastructure/repository/
    └── JdbcPaymentProofRepositoryTest.java [NEW]
```

### Source Code — modified files

```text
src/main/java/com/guilherme/emobiliaria/receipt/
├── domain/entity/Receipt.java                     [MODIFIED — add proofs field]
├── application/usecase/DeleteReceiptInteractor.java [MODIFIED — delete proofs first]
├── di/ReceiptModule.java                           [MODIFIED — new bindings]
└── ui/controller/
    ├── ReceiptFormController.java                  [MODIFIED — integrate drop zone]
    └── ReceiptListController.java                  [MODIFIED — proof button in ActionsCell]

src/main/java/com/guilherme/emobiliaria/shared/persistence/AppDataPaths.java [MODIFIED — proofStorageDirectory()]
src/main/java/module-info.java                     [MODIFIED — opens for new packages]
src/main/resources/messages.properties             [MODIFIED — new i18n keys]
```

## Complexity Tracking

No constitution violations.

---

## Implementation Details

### 1. DB Migration — `V10__add_payment_proofs.sql`

```sql
CREATE TABLE payment_proofs
(
    id                BIGINT GENERATED BY DEFAULT AS IDENTITY PRIMARY KEY,
    receipt_id        BIGINT       NOT NULL REFERENCES receipts (id) ON DELETE CASCADE,
    original_filename VARCHAR(255) NOT NULL,
    stored_filename   VARCHAR(255) NOT NULL UNIQUE,
    file_type         VARCHAR(10)  NOT NULL,
    attached_at       DATE         NOT NULL
);

CREATE INDEX idx_payment_proofs_receipt_id ON payment_proofs (receipt_id);
```

### 2. Domain: `ProofFileType` enum

```java
package com.guilherme.emobiliaria.receipt.domain.entity;

import java.util.Optional;

public enum ProofFileType {
  PDF, IMAGE;

  private static final Set<String> PDF_EXTENSIONS = Set.of(".pdf");
  private static final Set<String> IMAGE_EXTENSIONS =
      Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".tif");

  public static Optional<ProofFileType> fromExtension(String filename) {
    if (filename == null)
      return Optional.empty();
    String lower = filename.toLowerCase(Locale.ROOT);
    if (PDF_EXTENSIONS.stream().anyMatch(lower::endsWith))
      return Optional.of(PDF);
    if (IMAGE_EXTENSIONS.stream().anyMatch(lower::endsWith))
      return Optional.of(IMAGE);
    return Optional.empty();
  }
}
```

### 3. Domain: `PaymentProof` entity

Factory pattern matching existing entities (`Receipt`, `PaymentAccount`, etc.):

- `PaymentProof.create(originalFileName, storedFileName, fileType, attachedAt, receiptId)` — validates, returns new
  instance
- `PaymentProof.restore(id, ...)` — reconstitutes from DB

Validation: throw `BusinessException` with a new `ErrorMessage.PaymentProof.*` constant for null/blank fields.

### 4. Domain: `PaymentProofRepository` interface

```java
public interface PaymentProofRepository {
  PaymentProof create(PaymentProof proof);

  void delete(Long id);

  List<PaymentProof> findAllByReceiptId(Long receiptId);

  void deleteAllByReceiptId(Long receiptId);

  Map<Long, Integer> countByReceiptIds(List<Long> receiptIds);
}
```

### 5. Domain: `PaymentProofStorageService` interface

```java
public interface PaymentProofStorageService {
  String copyToStorage(Path sourceFile, String originalFileName);

  String copyBytesToStorage(byte[] imageBytes, String originalFileName);

  void delete(String storedFileName);

  Path resolve(String storedFileName);
}
```

### 6. `Receipt` entity changes

```java
// New field (default empty)
private List<PaymentProof> proofs = new ArrayList<>();

// New restore overload (existing restore() unchanged)
public static Receipt restoreWithProofs(Long id, ...,List<PaymentProof> proofs)

// New accessors
public List<PaymentProof> getProofs() {
  return Collections.unmodifiableList(proofs);
}

public void setProofs(List<PaymentProof> proofs) {
  this.proofs = new ArrayList<>(proofs);
}

public boolean hasProofs() {
  return !proofs.isEmpty();
}
```

### 7. `AppDataPaths` change

```java
public static Path proofStorageDirectory() {
  return ensureDirectory(resolveAppDataDir().resolve("proofs"));
}
```

### 8. Infrastructure: `PaymentProofStorageServiceImpl`

- `copyToStorage(path, name)`: `UUID.randomUUID() + ext`, `Files.copy()` to `proofStorageDirectory()`
- `copyBytesToStorage(bytes, name)`: write bytes, same naming
- `delete(storedFileName)`: `Files.deleteIfExists()`
- `resolve(storedFileName)`: `proofStorageDirectory().resolve(storedFileName)`

### 9. Infrastructure: `JdbcPaymentProofRepository`

Standard JDBC pattern matching `JdbcPaymentAccountRepository`:

- INSERT / DELETE / SELECT by receipt_id / SELECT count by receipt_ids (IN clause)

### 10. Application Use Cases

#### `AttachPaymentProofInput`

```java
record AttachPaymentProofInput(Long receiptId, Path sourceFile, String originalFileName) {
}


// OR for clipboard:
record AttachPaymentProofInput(Long receiptId, byte[] imageBytes, String originalFileName) {
}
```

Use a sealed interface or two separate input types. Simplest: two separate inputs and two overloaded `execute()`
methods, or a single input with nullable fields. Use two separate classes to keep it clean:

- `AttachPaymentProofFromFileInput(Long receiptId, Path sourceFile, String originalFileName)`
- `AttachPaymentProofFromBytesInput(Long receiptId, byte[] imageBytes, String originalFileName)`

Both go to the same `AttachPaymentProofInteractor.execute(...)`.

#### `AttachPaymentProofInteractor`

1. Verify receipt exists (fetch by id from `ReceiptRepository`)
2. Validate extension via `ProofFileType.fromExtension(originalFileName)` — throw if unsupported
3. Call `storageService.copyToStorage(...)` or `copyBytesToStorage(...)`
4. Create `PaymentProof` via `PaymentProof.create(...)`
5. Persist via `proofRepository.create(proof)`
6. Return `AttachPaymentProofOutput(proof)`

#### `RemovePaymentProofInteractor`

1. Load proofs for receipt: `proofRepository.findAllByReceiptId(input.receiptId())`
2. Find the proof by `input.proofId()`; throw `NOT_FOUND` if missing
3. `storageService.delete(proof.getStoredFileName())`
4. `proofRepository.delete(input.proofId())`

#### `FindPaymentProofsByReceiptIdInteractor`

1. Verify receipt exists
2. Return `proofRepository.findAllByReceiptId(receiptId)`

### 11. `DeleteReceiptInteractor` changes

Before deleting the receipt:

1. Load proofs: `proofRepository.findAllByReceiptId(receiptId)`
2. For each proof: `storageService.delete(proof.getStoredFileName())`
3. `proofRepository.deleteAllByReceiptId(receiptId)` (or rely on cascade — belt and suspenders, do both)
4. Then delete the receipt as before

### 12. DI and module-info updates

**`ReceiptModule.java`** — add:

```java
bind(PaymentProofRepository .class).

to(JdbcPaymentProofRepository .class);

bind(PaymentProofStorageService .class).

to(PaymentProofStorageServiceImpl .class);
```

**`module-info.java`** — add:

```java
opens com.guilherme.emobiliaria.receipt.domain.entity;   // if not already open
opens com.guilherme.emobiliaria.receipt.domain.repository; // for new interface
opens com.guilherme.emobiliaria.receipt.domain.service;    // for new interface
opens com.
guilherme.emobiliaria.receipt.infrastructure.repository to
com.google.guice; // already open
opens com.
guilherme.emobiliaria.receipt.infrastructure.service to
com.google.guice;    // already open
opens com.
guilherme.emobiliaria.receipt.ui.component to
javafx.fxml,com.google.guice; // NEW
```

### 13. UI: `ProofDropZonePane` component

A `VBox`-based JavaFX component placed inside the receipt form below the existing fields.

**Responsibilities:**

- Display a styled drop zone area with instructions ("Arraste arquivos aqui, clique para selecionar, ou CTRL+V para
  colar imagem")
- Handle `DragEvent.DRAG_OVER` and `DragEvent.DRAG_DROPPED` for file drops
- Handle `MouseEvent.MOUSE_CLICKED` to open `FileChooser` (accepts PDF + image extensions)
- Show a `ListView` of attached proofs (each row: icon + original filename + remove button)
- Expose `pendingFilesToAttach: List<PendingProof>` (staged, not yet saved)
- Expose `proofsToRemove: List<Long>` (IDs of existing proofs marked for removal)
- Display an inline error label for unsupported types, duplicate filenames
- Display an inline warning for duplicates ("Este arquivo já está anexado")
- CTRL+V is handled at the scene level in `ReceiptFormController` and delegates to the drop zone's
  `handleClipboardPaste()` method

**`PendingProof` inner record:**

```java
record PendingProof(Path file, byte[] imageBytes, String originalFileName, ProofFileType fileType) {
}
```

### 14. UI: `ProofSelectionDialog`

A `Dialog<PaymentProof>` subclass (or standard `ChoiceDialog`) for selecting which proof to open when a receipt has
multiple proofs.

- Shows each proof as: `[icon] originalFileName`
- Returns the selected `PaymentProof` or empty if cancelled

Simplest implementation: use `ChoiceDialog<PaymentProof>` with a custom cell factory for icon + name display.

### 15. `ReceiptFormController` changes

**New dependencies** (constructor injection):

- `AttachPaymentProofInteractor attachProof`
- `RemovePaymentProofInteractor removeProof`
- `FindPaymentProofsByReceiptIdInteractor findProofs`
- `Provider<ProofDropZonePane> dropZonePaneProvider`

**Form loading (edit mode)**:

- After loading the receipt, call `findProofs.execute(...)` on a background thread
- Populate `ProofDropZonePane` with the loaded proofs

**Save flow**:

1. Validate and save receipt (existing flow)
2. For each `proofsToRemove` ID: call `removeProof.execute(...)`
3. For each `pendingFilesToAttach`: call `attachProof.execute(...)`
4. Navigate back on success

**CTRL+V handler** (on scene):

```java
scene.addEventFilter(KeyEvent.KEY_PRESSED, e ->{
    if(e.

isControlDown() &&e.

getCode() ==KeyCode.V){
    dropZonePane.

handleClipboardPaste();
    }
        });
```

### 16. `ReceiptListController.ActionsCell` changes

- Add a `proofsBtn` button with label from `receipt.list.button.proofs`
- Button disabled when `!receipt.hasProofs()`
- On click (if exactly 1 proof): `handleOpenProof(proof)`
- On click (if multiple proofs): show `ProofSelectionDialog`, then `handleOpenProof(selected)`
- `handleOpenProof(proof)`: load proof path, call `Desktop.getDesktop().open(file)` in a `Task`
- File not found case: show inline error alert

**Loading proof data in the list:**
The `ReceiptListController` loads a page of receipts, then calls `proofRepository.countByReceiptIds(ids)` via a new
`GetReceiptProofCountsInteractor` (or, to avoid proliferating interactors, this count can be embedded in the list query
result). Simpler: after `loadPage` succeeds, call `findProofsForReceipts(receipts)` which does a single batch count and
sets `receipt.setProofs(loadedProofs)` for each. Since `ReceiptListController` has access to
`FindPaymentProofsByReceiptIdInteractor`, it can call it once per page batch.

Actually, for the list we only need hasProofs (true/false), not the full proof objects. A cleaner approach: add a
`FindReceiptProofSummariesInteractor` that takes a list of receipt IDs and returns a `Map<Long, List<PaymentProof>>`.
The list controller calls this after loading a page and mutates each `Receipt` with its proofs. This is one extra query
per page load.

**Or simplest approach**: in the `JDBC` list query, join `payment_proofs` count:

```sql
SELECT r.*, COUNT(pp.id) as proof_count
FROM receipts r
         LEFT JOIN payment_proofs pp ON pp.receipt_id = r.id
WHERE ...
    GROUP BY r.id
```

Then `Receipt.restore()` with the proof count. But this requires changing the `findAllByContractId` and `search` queries
significantly and adding a field to `Receipt`.

**Decision**: Use the `FindPaymentProofsByReceiptIdInteractor` approach. After `loadPage()` resolves a
`PagedResult<Receipt>`, make one additional async call to load proofs for all receipt IDs on the page (via
`proofRepository.countByReceiptIds(ids)`, which returns a `Map<Long, Integer>`). Use a new
`GetReceiptProofCountsInteractor(proofRepository)` that takes `List<Long> receiptIds` and returns `Map<Long, Integer>`.
Then call `receipt.setProofs(List.of(dummy proof for count))` or — simpler — add a `receipt.setProofCount(int)`
package-private method.

Simplest of all without changing Receipt: keep a `Map<Long, Boolean> receiptHasProofs` in the list controller, populated
after load. The `ActionsCell.updateItem()` reads from this map to set button state. This avoids any change to `Receipt`.

**Final decision on list proof loading**: Keep `Receipt` unchanged for the list case. The `ReceiptListController`
maintains a `Set<Long> receiptsWithProofs` (transient, updated on each page load). After loading a page, dispatch an
async task that calls `proofRepository.countByReceiptIds(pageReceiptIds)` and updates `receiptsWithProofs`, then
refreshes the table. The `ActionsCell` checks `controller.hasProofs(receipt.getId())`. This is the least invasive
approach.

---

## i18n Keys

Add to `messages.properties` (and PT-BR equivalent):

```properties
receipt.list.button.proofs=Comprovantes
receipt.form.proof.section=Comprovantes de Pagamento
receipt.form.proof.drop_zone=Arraste arquivos aqui, clique para selecionar ou pressione CTRL+V
receipt.form.proof.error.unsupported_type=Tipo de arquivo não suportado. Apenas PDF e imagens são aceitos.
receipt.form.proof.error.duplicate=Este arquivo já está anexado.
receipt.form.proof.dialog.title=Selecionar Comprovante
receipt.list.proof.error.file_not_found=Arquivo não encontrado. Ele pode ter sido movido ou excluído.
```
