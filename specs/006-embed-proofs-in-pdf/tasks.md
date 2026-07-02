---

description: "Task list for Embed Payment Proofs as PDF Pages"
---

# Tasks: Embed Payment Proofs as PDF Pages

**Input**: Design documents from `/specs/006-embed-proofs-in-pdf/`
**Prerequisites**: plan.md, spec.md, research.md, data-model.md, contracts/payment-proof-pdf-embedding-service.md,
quickstart.md

**Tests**: Included — `docs/test.md` and `docs/domain-services.md` require unit tests for interactors and, per this
feature's contract doc, a dedicated real-implementation test class for `OpenPdfProofEmbeddingService`.

**Organization**: Tasks are grouped by user story (US1 = P1, US2 = P2) per `spec.md`. FR-006 (skip + post-generation
dialog) and FR-005/FR-007 (no-proof regression safety, receipt content untouched) are load-bearing for US1's own
acceptance scenarios (scenario 1, scenario 4 "no proof omitted") and are implemented there; US2 exclusively refines
*how* each proof page's content is placed (aspect-ratio-preserving scale + centering, EXIF rotation) without changing
page count/order, so it can be implemented and tested after US1 without touching US1's tests.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no dependencies)
- **[Story]**: Which user story this task belongs to (US1, US2)

## Path Conventions

Single JavaFX desktop project — production code under `src/main/java/com/guilherme/emobiliaria/`, tests under
`src/test/java/com/guilherme/emobiliaria/`, both mirroring the `receipt` feature's existing package-by-feature layout.

---

## Phase 1: Setup

**Purpose**: Bring in the one new dependency and expose the already-present OpenPDF dependency to the module system.

- [X] T001 Add the `com.drewnoakes:metadata-extractor:2.19.0` dependency to `pom.xml` (new `<dependency>` entry,
  alongside the existing `com.github.librepdf:openpdf` entry)
- [X] T002 [P] In `src/main/java/module-info.java`, add `requires com.github.librepdf.openpdf;` and
  `requires metadata.extractor;` (the latter is the automatic module name derived from the
  `metadata-extractor` jar's base file name, dashes → dots — see research.md §5)

**Checkpoint**: `mvn compile` succeeds with both dependencies resolvable from `com.guilherme.emobiliaria` module code.

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: New domain-layer value objects and the service interface/output DTO that both user stories build on.
No proof-embedding behavior yet — just the shapes.

**⚠️ CRITICAL**: Must complete before Phase 3 (US1) begins.

- [X] T003 [P] Create `SkipReason` enum (`MISSING`, `UNREADABLE`) in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/service/SkipReason.java` per data-model.md
- [X] T004 [P] Create `SkippedProof` record (`PaymentProof proof, SkipReason reason`) in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/service/SkippedProof.java`
- [X] T005 [P] Create `ProofEmbeddingResult` record (`byte[] pdfBytes, List<SkippedProof> skippedProofs`) in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/service/ProofEmbeddingResult.java`
- [X] T006 Create `PaymentProofPdfEmbeddingService` interface (`ProofEmbeddingResult embed(byte[] receiptPdf,
      List<PaymentProof> proofs)`) in
  `src/main/java/com/guilherme/emobiliaria/receipt/domain/service/PaymentProofPdfEmbeddingService.java`,
  matching contracts/payment-proof-pdf-embedding-service.md (depends on T003–T005)
- [X] T007 [P] Create `SkippedProofInfo` record (`String displayName, SkipReason reason`) in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/output/SkippedProofInfo.java`
- [X] T008 Update `GenerateReceiptPdfOutput` to
  `public record GenerateReceiptPdfOutput(byte[] pdfBytes, List<SkippedProofInfo> skippedProofs) {}` in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/output/GenerateReceiptPdfOutput.java` (depends on T007)
- [X] T009 Create `FakePaymentProofPdfEmbeddingService` extending
  `com.guilherme.emobiliaria.shared.fake.FakeImplementation` in
  `src/test/java/com/guilherme/emobiliaria/receipt/domain/service/FakePaymentProofPdfEmbeddingService.java`.
  Default `embed(...)` returns `new ProofEmbeddingResult(receiptPdf, List.of())` after `maybeFail()`; expose a
  setter/configuration method so tests can pre-load a non-empty `skippedProofs` result (depends on T006)

**Checkpoint**: New types compile; both user stories can now be implemented against
`PaymentProofPdfEmbeddingService`.

---

## Phase 3: User Story 1 - Receive Proofs Bundled Into the Receipt PDF (Priority: P1) 🎯 MVP

**Goal**: Generating a receipt's PDF appends one page per attached, readable payment proof after the receipt's own
pages, in attachment order, all sharing the receipt's fixed page size; unreadable proofs are skipped (not fatal) and
reported to the user in a dialog after generation; a receipt with no proofs is unaffected.

**Independent Test**: Create a receipt, attach an image proof and a PDF proof, generate the receipt PDF, and confirm
the output contains the receipt's page(s) followed by one page per proof, in attachment order (spec.md Acceptance
Scenarios 1–4; quickstart.md Scenarios 1, 2 (page count only), 3, 4, 5, 6).

### Implementation for User Story 1

- [X] T010 [US1] Implement `OpenPdfProofEmbeddingService` skeleton in
  `src/main/java/com/guilherme/emobiliaria/receipt/infrastructure/service/OpenPdfProofEmbeddingService.java`:
  implements `PaymentProofPdfEmbeddingService`; if `proofs.isEmpty()` return
  `new ProofEmbeddingResult(receiptPdf, List.of())` untouched (no OpenPDF round-trip — FR-005/SC-004); otherwise
  open `receiptPdf` with `PdfReader`, read `reader.getPageSizeWithRotation(1)` as the fixed page size, and use
  `PdfCopy` to import every page of `receiptPdf` unchanged into the output document (research.md §2–§3)
- [X] T011 [US1] Extend `OpenPdfProofEmbeddingService` to append proof pages: for each `PaymentProof` in order,
  resolve its file via `PaymentProofStorageService.resolve(...)`, branch on `fileType` (`IMAGE` → decode via
  `com.lowagie.text.Image.getInstance`, one output page; `PDF` → `PdfReader` the proof file, one output page per
  source page via `PdfContentByte.addTemplate`), each new page sized exactly like the page size from T010; wrap
  each proof's processing in its own try/catch so a missing file (→ `SkipReason.MISSING`) or a decode/read
  failure at any point, including partway through a multi-page PDF proof (→ `SkipReason.UNREADABLE`), discards
  that proof's staged pages, appends a `SkippedProof` to the result, and moves on to the next proof without
  aborting the document (contract postconditions 2, 5, 6, 7, 8; research.md §4, §6) (depends on T010)
- [X] T012 [US1] Bind `PaymentProofPdfEmbeddingService` to `OpenPdfProofEmbeddingService` in
  `src/main/java/com/guilherme/emobiliaria/receipt/di/ReceiptModule.java` (depends on T011)
- [X] T013 [US1] Update `GenerateReceiptPdfInteractor` in
  `src/main/java/com/guilherme/emobiliaria/receipt/application/usecase/GenerateReceiptPdfInteractor.java` to
  inject `PaymentProofRepository` and `PaymentProofPdfEmbeddingService`; after generating the base PDF via the
  unchanged `ReceiptFileService`, load `paymentProofRepository.findAllByReceiptId(receipt.getId())`, call
  `paymentProofPdfEmbeddingService.embed(baseBytes, proofs)`, and return
  `new GenerateReceiptPdfOutput(result.pdfBytes(), mapped SkippedProofInfo list)` per data-model.md's flow
  summary (depends on T008, T009, T012)
- [X] T014 [US1] Update `GenerateReceiptPdfInteractorTest` in
  `src/test/java/com/guilherme/emobiliaria/receipt/application/usecase/GenerateReceiptPdfInteractorTest.java` to
  construct the interactor with a `FakePaymentProofRepository` and `FakePaymentProofPdfEmbeddingService`, and add
  cases: (a) receipt with no proofs → `skippedProofs()` is empty and `pdfBytes()` unchanged from today's
  assertion, (b) fake configured with a non-empty skip list → `GenerateReceiptPdfOutput.skippedProofs()` passes
  it through unchanged (depends on T013)
- [X] T015 [US1] Update `ReceiptListController.handleGeneratePdf` in
  `src/main/java/com/guilherme/emobiliaria/receipt/ui/controller/ReceiptListController.java`: after the
  background `Task` succeeds, if `output.skippedProofs()` is non-empty, show an
  `javafx.scene.control.Alert(Alert.AlertType.INFORMATION)` (mirroring the existing delete-confirmation `Alert`
  pattern) listing each skipped proof's display name and reason; do nothing extra when the list is empty
  (depends on T013)
- [X] T016 [P] [US1] Add the skip-dialog's title/header/content and per-`SkipReason` message keys to
  `src/main/resources/messages.properties` and `src/main/resources/messages_pt_BR.properties` (e.g.
  `receipt.list.pdf.skipped_proofs.title`, `.message`, `.reason.missing`, `.reason.unreadable`), used by T015
- [X] T017 [US1] Create `OpenPdfProofEmbeddingServiceTest` in
  `src/test/java/com/guilherme/emobiliaria/receipt/infrastructure/service/OpenPdfProofEmbeddingServiceTest.java`
  using `FakePaymentProofStorageService` and real temp files (per contracts doc's "Test doubles" section),
  covering: empty-proofs pass-through returns the same bytes untouched; one image proof → output page count is
  base + 1; one PDF proof → output page count is base + (source page count); mixed proofs → page order matches
  attachment order; a proof pointing at a non-existent stored file → skipped with `SkipReason.MISSING` and
  generation still succeeds; a proof file with corrupted/truncated bytes → skipped with `SkipReason.UNREADABLE`;
  every proof unreadable → output page count equals base page count and every input proof appears in
  `skippedProofs()` (depends on T011)
- [ ] T018 [US1] Run quickstart.md Scenarios 1, 3, 4, 5, 6 manually against `mvn clean javafx:run` (Scenario 2 is
  revisited fully in US2 — here just confirm page count/presence, not orientation/centering) (depends on T015,
  T016)

**Checkpoint**: User Story 1 is fully functional — proofs are bundled into the output PDF in order, unreadable
proofs are skipped with a dialog, and a receipt with no proofs is unaffected. Deliverable as MVP.

---

## Phase 4: User Story 2 - Proof Pages Remain Undistorted (Priority: P2)

**Goal**: Each embedded proof page shows the original proof's full content at its own proportions — scaled uniformly
to fit the receipt's fixed page size and centered (never stretched/cropped), with photographed proofs shown in their
visually correct (EXIF-corrected) orientation.

**Independent Test**: Attach a tall portrait photo, a wide landscape scan, and a standard PDF page as proofs; confirm
each embedded page shows the full original content, undistorted, at correct orientation (spec.md Acceptance
Scenarios 1–3 for this story; quickstart.md Scenario 2 in full, Scenario 3's proportion check).

### Implementation for User Story 2

- [X] T019 [US2] In `OpenPdfProofEmbeddingService`, replace/complete the per-proof placement logic from T011 with
  "contain" scaling: for each proof page's content (decoded image dimensions, or the imported source PDF page's
  own `Rectangle`), compute `scale = min(pageWidth / contentWidth, pageHeight / contentHeight)`, draw the content
  at that uniform scale via a `PdfContentByte` transform (never independent X/Y scaling), and center it on the
  page — leaving blank margins where the proof's aspect ratio differs from the receipt's page (contract
  postconditions 3, 4; research.md §4) (depends on T011)
- [X] T020 [US2] In `OpenPdfProofEmbeddingService`, before wrapping a decoded image proof in
  `com.lowagie.text.Image`, read its EXIF `Orientation` tag via
  `com.drewnoakes.metadata.extractor`'s `ImageMetadataReader.readMetadata` /
  `ExifIFD0Directory.TAG_ORIENTATION`; if present and non-default, rotate/flip the decoded `BufferedImage` with
  an `AffineTransform` so its in-memory pixels match the visually correct orientation before it's placed on the
  page (research.md §5) (depends on T011; independent of T019 but both touch the same method, do sequentially)
- [X] T021 [US2] Extend `OpenPdfProofEmbeddingServiceTest` with: a portrait image proof narrower than the page →
  placed content preserves its aspect ratio (no independent X/Y stretch) and is centered with margins on the
  wider axis; a landscape image proof wider than the page's usable width → scaled down preserving aspect ratio;
  a small image proof smaller than the page → scaled up to fill available space while preserving aspect ratio; a
  sample JPEG with a non-default EXIF orientation tag → the placed image's effective (post-rotation) dimensions
  reflect the visually-correct orientation, not the raw stored pixel orientation (depends on T019, T020)
- [ ] T022 [US2] Run quickstart.md Scenario 2 in full (EXIF-rotated portrait photo, centering/margins) and Scenario 3
  (multi-page PDF proof proportions) manually against `mvn clean javafx:run` (depends on T019, T020)

**Checkpoint**: All acceptance scenarios in spec.md (both user stories) pass; proof pages are both structurally
correct (US1) and visually faithful (US2).

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final regression safety net across both stories.

- [X] T023 Run the full test suite (`mvn test`) and fix any regressions introduced in `receipt` or elsewhere
- [X] T024 [P] Add an explicit byte-equality assertion (`Arrays.equals`) in
  `OpenPdfProofEmbeddingServiceTest` for the empty-proofs case, directly verifying SC-004's "byte-identical
  output" requirement rather than only checking page count

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately.
- **Foundational (Phase 2)**: Depends on Setup (needs `PdfReader`/OpenPDF types available for T006's signature to
  compile against `byte[]`/`List<PaymentProof>`, though the interface itself has no OpenPDF import — practically only
  needs T001/T002 done so the module builds). BLOCKS User Story 1.
- **User Story 1 (Phase 3)**: Depends on Foundational (Phase 2) completion. No dependency on User Story 2.
- **User Story 2 (Phase 4)**: Depends on User Story 1 (Phase 3) — it edits the same
  `OpenPdfProofEmbeddingService`/`OpenPdfProofEmbeddingServiceTest` files T010/T011/T017 create, refining placement
  logic in place rather than adding a parallel path.
- **Polish (Phase 5)**: Depends on both user stories being complete.

### Within User Story 1

T010 → T011 → T012 → T013 → (T014 [P] with T015/T016) → T017 → T018. T014, T015 both depend on T013 and touch
different files so can run in parallel; T016 (properties files) can run in parallel with T014/T015/T017.

### Within User Story 2

T019 and T020 both edit `OpenPdfProofEmbeddingService` (sequential, not [P]); T021 depends on both; T022 depends on
both.

---

## Parallel Example: Foundational Phase

```bash
# T003, T004, T005 are independent new files:
Task: "Create SkipReason enum in receipt/domain/service/SkipReason.java"
Task: "Create SkippedProof record in receipt/domain/service/SkippedProof.java"
Task: "Create ProofEmbeddingResult record in receipt/domain/service/ProofEmbeddingResult.java"
# Then T006 (needs all three), then T007 [P] can run alongside T006:
Task: "Create SkippedProofInfo record in receipt/application/output/SkippedProofInfo.java"
```

## Parallel Example: User Story 1

```bash
# After T013 (interactor updated), these touch different files:
Task: "Update GenerateReceiptPdfInteractorTest for proof loading + skip pass-through"
Task: "Update ReceiptListController.handleGeneratePdf to show skipped-proofs Alert"
Task: "Add skip-dialog i18n keys to messages.properties and messages_pt_BR.properties"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1 (Setup) and Phase 2 (Foundational).
2. Complete Phase 3 (User Story 1) — proofs are bundled into the PDF, in order, with skip/dialog handling.
3. **STOP and VALIDATE**: run quickstart.md Scenarios 1, 3–6; confirm SC-002, SC-004.
4. This is deployable — proofs travel with the receipt, even if a portrait photo isn't yet perfectly centered.

### Incremental Delivery

1. Setup + Foundational → build passes with the new dependency and value objects in place.
2. User Story 1 → validate independently → MVP.
3. User Story 2 → validate independently (quickstart Scenario 2 in full) → refined visual correctness (SC-003).
4. Phase 5 → full regression pass.

---

## Notes

- [P] tasks touch different files with no unmet dependency.
- [Story] labels map every Phase 3/4 task to spec.md's user stories for traceability.
- No task in this feature touches the database — `data-model.md` confirms this is in-memory-only, request-scoped
  behavior.
- Commit after each task or logical group; stop at either checkpoint to validate that story independently before
  continuing.
