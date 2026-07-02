# Implementation Plan: Embed Payment Proofs as PDF Pages

**Branch**: `006-embed-proofs-in-pdf` | **Date**: 2026-07-02 | **Spec**: [spec.md](./spec.md)
**Input**: Feature specification from `/specs/006-embed-proofs-in-pdf/spec.md`

## Summary

When a user generates a receipt's PDF, the system currently returns only the Jasper-rendered receipt document. This
feature appends one page per attached payment proof (image or PDF) after the receipt's own pages, in attachment
order, scaling each proof to fit the receipt's own fixed page size and centering it to avoid distortion. Proofs that
can't be read at generation time are skipped (not the whole generation), and the user is shown a dialog listing what
was omitted and why. A receipt with no proofs must produce byte-identical output to today's behavior.

The approach: keep the existing Jasper-based `ReceiptFileService` untouched (it keeps producing the receipt's own
pages exactly as today). A new domain service, `PaymentProofPdfEmbeddingService`, takes those bytes plus the
receipt's `PaymentProof` list and returns the final merged PDF bytes together with a list of any proofs it had to
skip. `GenerateReceiptPdfInteractor` orchestrates: load receipt → load proofs → generate base PDF → embed proofs →
return merged bytes + skip list. The real implementation uses OpenPDF (already a dependency) for all PDF page
manipulation, plus a new lightweight dependency (`metadata-extractor`) to read EXIF orientation so photographed
proofs are rotated correctly before being placed on the page.

## Technical Context

**Language/Version**: Java 24
**Primary Dependencies**: JavaFX 21 (UI), Google Guice 7 (DI), JasperReports 7.0.3 (existing receipt PDF rendering),
OpenPDF 1.3.30 / `com.github.librepdf.openpdf` (existing dependency, newly used directly for PDF page
merging/scaling), `com.drewnoakes:metadata-extractor` 2.19.0 (**new** dependency, for EXIF orientation of image
proofs)
**Storage**: H2 (JDBC via HikariCP, migrations via Flyway) for receipt/proof metadata; proof files already stored on
disk under `AppDataPaths.proofStorageDirectory()` — this feature only reads those existing files, no schema changes
**Testing**: JUnit 5, fakes extending `com.guilherme.emobiliaria.shared.fake.FakeImplementation`
**Target Platform**: Cross-platform desktop app (JavaFX)
**Project Type**: Single desktop application, package-by-feature — this feature extends the existing `receipt`
feature, no new feature module
**Performance Goals**: No new hard targets; generation of a receipt PDF with a handful of proofs (typical case) should
remain interactive (sub-few-seconds), consistent with today's single-receipt PDF generation
**Constraints**: Output for a receipt with zero proofs MUST be byte-identical to current behavior (FR-005/SC-004); all
pages in the output (receipt + proofs) MUST share the receipt's own fixed page size (FR-004); a failure on one proof
MUST NOT abort generation of the rest (FR-006)
**Scale/Scope**: Single receipt processed per generation request, typically 0–5 attached proofs; no batch/bulk
generation entry point is introduced

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

The project constitution at `.specify/memory/constitution.md` is still the unfilled template (placeholder principles
only — no concrete rules have been ratified for this project). There are no constitutional gates to evaluate against.
This plan instead follows the conventions documented in `docs/general-instructions.md` and the per-directory
`CLAUDE.md` guides (layering, DI, domain-service fakes, testing style), which are treated as the binding project
conventions in place of a ratified constitution.

**Result**: PASS (no gates defined).

## Project Structure

### Documentation (this feature)

```text
specs/006-embed-proofs-in-pdf/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md         # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
├── contracts/           # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

This feature extends the existing `receipt` feature within the single package-by-feature project; no new project or
top-level directory is introduced.

```text
src/main/java/com/guilherme/emobiliaria/receipt/
├─ domain/
│  ├─ entity/
│  │  └─ PaymentProof.java                          # existing, unchanged
│  └─ service/
│     ├─ PaymentProofPdfEmbeddingService.java        # NEW interface — embeds proofs into a base PDF
│     ├─ ProofEmbeddingResult.java                   # NEW value record (pdfBytes + skippedProofs)
│     ├─ SkippedProof.java                           # NEW value record (proof + reason)
│     └─ SkipReason.java                             # NEW enum (MISSING, UNREADABLE)
│
├─ application/
│  ├─ output/
│  │  ├─ GenerateReceiptPdfOutput.java               # UPDATED — add skippedProofs field
│  │  └─ SkippedProofInfo.java                       # NEW small DTO (displayName + reason) for the UI layer
│  └─ usecase/
│     └─ GenerateReceiptPdfInteractor.java           # UPDATED — loads proofs, calls embedding service
│
├─ infrastructure/
│  └─ service/
│     └─ OpenPdfProofEmbeddingService.java           # NEW — OpenPDF + metadata-extractor implementation
│
├─ di/
│  └─ ReceiptModule.java                             # UPDATED — bind PaymentProofPdfEmbeddingService
│
└─ ui/
   └─ controller/
      └─ ReceiptListController.java                  # UPDATED — show skipped-proofs Alert after generation

src/test/java/com/guilherme/emobiliaria/receipt/
├─ domain/service/
│  └─ FakePaymentProofPdfEmbeddingService.java        # NEW fake, per docs/domain-services.md (PDF generation → fake only)
└─ application/usecase/
   └─ GenerateReceiptPdfInteractorTest.java           # UPDATED — cover proof loading + skip pass-through

pom.xml                # UPDATED — add com.drewnoakes:metadata-extractor dependency
src/main/java/module-info.java   # UPDATED — requires com.github.librepdf.openpdf, requires metadata-extractor,
                                  #           opens/exports as needed for the new infra class
```

**Structure Decision**: No new project structure — this is additive work inside the existing `receipt` feature
module, following the same domain/application/infrastructure/di/ui layering as the rest of the codebase. Per
`docs/domain-services.md`, `PaymentProofPdfEmbeddingService` gets a real implementation (`OpenPdfProofEmbeddingService`,
in-process, no external system, but PDF generation is explicitly called out there as fake-only for tests) **and** a
fake (`FakePaymentProofPdfEmbeddingService`) for use by interactor tests — mirroring the existing
`ReceiptFileService`/`FakeReceiptFileService` pair.

## Complexity Tracking

*No entries — no constitutional gates were violated (see Constitution Check above).*
