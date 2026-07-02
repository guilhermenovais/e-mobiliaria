# Phase 1 Data Model: Embed Payment Proofs as PDF Pages

No database schema changes. This feature introduces in-memory value objects only, scoped to a single PDF-generation
call — nothing here is persisted.

## Existing entities used (unchanged)

### `PaymentProof` (`receipt/domain/entity/PaymentProof.java`)

Already exists; this feature only *reads* it. Relevant fields:

| Field              | Type                               | Used for                                                                                   |
|--------------------|------------------------------------|--------------------------------------------------------------------------------------------|
| `displayName`      | `String`                           | Shown to the user in the skip dialog when the proof can't be read                          |
| `storedFileName`   | `String`                           | Resolved via `PaymentProofStorageService.resolve(...)` to a `Path`                         |
| `fileType`         | `ProofFileType` (`PDF` \| `IMAGE`) | Determines whether the proof is decoded as an image or read as a PDF                       |
| `attachedAt`, `id` | —                                  | Not used directly; ordering comes from repository list order (attachment order), unchanged |

### `Receipt` (`receipt/domain/entity/Receipt.java`)

Already exposes `getProofs(): List<PaymentProof>` / `hasProofs(): boolean`. `GenerateReceiptPdfInteractor` must ensure
this list is populated (today, `ReceiptRepository.findById` does **not** load proofs — see Requirements below), so the
interactor loads proofs separately via `PaymentProofRepository` rather than relying on the entity already carrying them.

## New value objects

### `SkipReason` (enum) — `receipt/domain/service/SkipReason.java`

| Value        | Meaning                                                                                                              |
|--------------|----------------------------------------------------------------------------------------------------------------------|
| `MISSING`    | The stored file could not be found at the resolved path                                                              |
| `UNREADABLE` | The file exists but could not be parsed (corrupted image/PDF, encrypted/password-protected PDF, unsupported content) |

Two values are sufficient to answer FR-006's "why" without over-modeling; both edge cases in the spec (missing file,
corrupted/password-protected PDF) map cleanly onto one of these.

### `SkippedProof` (record) — `receipt/domain/service/SkippedProof.java`

```java
public record SkippedProof(PaymentProof proof, SkipReason reason) {}
```

Carries the full domain entity (not just its name) so callers can access any field they need (e.g., `displayName`)
without the domain service needing to decide what's UI-relevant.

### `ProofEmbeddingResult` (record) — `receipt/domain/service/ProofEmbeddingResult.java`

```java
public record ProofEmbeddingResult(byte[] pdfBytes, List<SkippedProof> skippedProofs) {}
```

- `pdfBytes`: the final PDF — either the untouched base receipt bytes (no proofs attached, or all proofs skipped and
  nothing could be appended) or the merged document (receipt pages + successfully embedded proof pages).
- `skippedProofs`: empty list when every attached proof was embedded successfully.

**Invariant**: `skippedProofs.size() <= originalProofsPassedIn.size()`, and every proof in `skippedProofs` must be one
of the proofs originally passed to `embed(...)` — the service never invents skipped proofs.

### `SkippedProofInfo` (record, application layer) — `receipt/application/output/SkippedProofInfo.java`

```java
public record SkippedProofInfo(String displayName, SkipReason reason) {}
```

A UI-facing projection of `SkippedProof`, produced by `GenerateReceiptPdfInteractor` when building
`GenerateReceiptPdfOutput`, so the `ui` layer (which shows the Alert dialog) doesn't need to depend on the domain
entity `PaymentProof` — only on the small piece of data it actually renders (name + reason).

## Updated existing types

### `GenerateReceiptPdfOutput` (`receipt/application/output/GenerateReceiptPdfOutput.java`)

```java
public record GenerateReceiptPdfOutput(byte[] pdfBytes, List<SkippedProofInfo> skippedProofs) {}
```

`skippedProofs` is always non-null (empty list, not null, when nothing was skipped) so the UI layer can iterate it
unconditionally.

## Flow summary

```
GenerateReceiptPdfInteractor.execute(input)
  1. receipt = receiptRepository.findById(input.id()) orElseThrow NOT_FOUND
  2. proofs  = paymentProofRepository.findAllByReceiptId(receipt.getId())   // attachment order
  3. baseBytes = receiptFileService.generateReceiptPdf(receipt)             // unchanged Jasper output
  4. result  = paymentProofPdfEmbeddingService.embed(baseBytes, proofs)
  5. return GenerateReceiptPdfOutput(result.pdfBytes(), map(result.skippedProofs(), -> SkippedProofInfo))
```

No new database tables/columns; no changes to `PaymentProofRepository`, `PaymentProof`, or persistence migrations.
