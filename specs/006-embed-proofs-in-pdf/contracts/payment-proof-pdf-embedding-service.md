# Contract: `PaymentProofPdfEmbeddingService`

This is not a network/API contract — the app is a JavaFX desktop application with no external interface. This
document instead specifies the contract of the new internal domain service interface, since it's the seam between
this feature's business rules (spec FR-001–FR-006) and its OpenPDF-based implementation, and the seam the interactor
test suite fakes.

```java
package com.guilherme.emobiliaria.receipt.domain.service;

public interface PaymentProofPdfEmbeddingService {
  ProofEmbeddingResult embed(byte[] receiptPdf, List<PaymentProof> proofs);
}
```

## Preconditions

- `receiptPdf` is a well-formed, non-empty PDF byte array (the output of `ReceiptFileService.generateReceiptPdf`).
  The service is not responsible for validating the receipt's own PDF — only proofs are subject to skip logic.
- `proofs` is the list of `PaymentProof` entities attached to the receipt, in attachment order (as returned by
  `PaymentProofRepository.findAllByReceiptId`). May be empty.

## Postconditions / behavioral guarantees

1. **Empty input**: If `proofs.isEmpty()`, returns `new ProofEmbeddingResult(receiptPdf, List.of())` — the exact same
   byte array reference/content passed in, untouched, and an empty skip list. (FR-005, SC-004)
2. **Page order**: For a non-empty `proofs` list, the returned PDF contains, in order: every page of `receiptPdf`
   unchanged, followed by one page per page of content found in each successfully-processed proof, in the same order
   as `proofs`. An image proof contributes exactly one page; a PDF proof contributes one page per page in that source
   PDF. (FR-001)
3. **Undistorted content**: Each proof page's content (the full image, or one full source PDF page) is drawn in full,
   scaled uniformly (never independently on X/Y) to fit within the page without cropping, and centered. (FR-002,
   FR-003, SC-003)
4. **Fixed page size**: Every page in the returned PDF — receipt pages and proof pages alike — has the same page size
   as `receiptPdf`'s own pages. No proof page is ever wider or taller than the receipt's page. (FR-004)
5. **Per-proof isolation**: A proof that cannot be resolved, decoded, or fully read is entirely omitted from the
   output (no partial pages from a partially-corrupt multi-page PDF proof) and is instead appended to
   `result.skippedProofs()` with a `SkipReason`. Processing continues for the remaining proofs; the method does not
   throw for this case. (FR-006, and the "partially readable PDF" clarification)
6. **All proofs fail**: If every proof fails, the returned PDF still contains all of the receipt's own pages
   (unchanged) with no proof pages appended, and `skippedProofs()` lists every input proof.
7. **Never throws for proof-level problems**: The method only throws for a fundamentally invalid `receiptPdf` input
   (a programmer/integration error, not a runtime user-triggered condition) — never for a bad/missing/corrupt proof
   file, which is the expected, handled case covered by rule 5.
8. **Skip list fidelity**: Every element of `result.skippedProofs()` refers to one of the `PaymentProof` instances
   passed into `proofs` (by identity/id) — the service never fabricates or duplicates skipped-proof entries, and never
   lists a proof that was successfully embedded.

## Non-goals

- Does not modify, move, or delete the underlying proof files on disk (read-only with respect to
  `PaymentProofStorageService`).
- Does not change `receiptPdf`'s own content, layout, or metadata beyond what's structurally required to append pages
  to it. (FR-007)
- Does not attempt to open password-protected PDFs even if a password happens to be discoverable — any encrypted/
  unreadable-without-credentials PDF proof is treated as `SkipReason.UNREADABLE`.

## Test doubles

- `FakePaymentProofPdfEmbeddingService` (test package `receipt/domain/service`) extends
  `com.guilherme.emobiliaria.shared.fake.FakeImplementation` per `docs/fake-services.md` / `docs/domain-services.md`.
  Default behavior: returns `new ProofEmbeddingResult(receiptPdf, List.of())` (pass-through, nothing skipped) unless
  configured otherwise by the test, mirroring how `FakeReceiptFileService` returns fixed bytes. Should expose a way
  for tests to pre-configure a non-empty `skippedProofs` result, so `GenerateReceiptPdfInteractorTest` can assert the
  skip list passes through into `GenerateReceiptPdfOutput` without depending on real OpenPDF/EXIF behavior.
