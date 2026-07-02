# Quickstart: Embed Payment Proofs as PDF Pages

Manual verification steps once the feature is implemented, mirroring the acceptance scenarios in `spec.md`.

## Setup

1. `mvn clean javafx:run` (or your usual dev run command) to start the app against a local H2 database.
2. Create (or reuse) a contract and a receipt for it via the UI.
3. Prepare a few sample proof files ahead of time:
    - A tall portrait photo (e.g., a phone photo, `.jpg`), ideally one taken with the phone rotated so its EXIF
      orientation tag differs from its stored pixel orientation — the easiest way to get this is a real phone photo
      straight off the camera roll.
    - A wide landscape scan/photo (`.jpg` or `.png`).
    - A small multi-page PDF (e.g., export a 2-page document to PDF).

## Scenario 1 — No proofs (regression check)

1. Generate the PDF for a receipt with **no** attached proofs.
2. Confirm the output is unchanged from current behavior (same page count/content as before this feature).

## Scenario 2 — Single image proof

1. Attach the portrait photo to a receipt (via the existing "Anexar comprovante" flow).
2. Generate the receipt PDF.
3. Confirm the output has the receipt's own page(s) followed by exactly one more page.
4. Open that page and confirm: the full photo is visible, right-side-up (not stretched, not cropped, not
   sideways/upside-down even if the original file's EXIF said otherwise), centered on a page the same size as the
   receipt's own pages, with blank margins where the photo's aspect ratio differs from the page's.

## Scenario 3 — Single PDF proof

1. Attach the multi-page PDF to a receipt.
2. Generate the receipt PDF.
3. Confirm the output has the receipt's own page(s) followed by one page per page of the attached PDF, each showing
   that source page's full content, undistorted, centered on a page the same size as the receipt's own pages.

## Scenario 4 — Mixed proofs, ordering

1. Attach, in this order: the portrait photo, then the PDF, then the landscape photo.
2. Generate the receipt PDF.
3. Confirm the proof pages appear in that same order after the receipt's own pages, and that all pages (receipt +
   every proof page) share identical page dimensions.

## Scenario 5 — Missing/unreadable proof (skip + dialog)

1. Attach two proofs to a receipt.
2. Outside the app, delete or corrupt the underlying stored file for one of them (find it via
   `AppDataPaths.proofStorageDirectory()`, or a quicker way: attach, generate once successfully to know it works,
   then manually truncate/corrupt one stored file on disk).
3. Generate the receipt PDF again.
4. Confirm: generation still succeeds and produces a PDF containing the receipt's pages plus the still-readable
   proof's page(s); a dialog appears after generation finishes listing the omitted proof's display name and a reason.

## Scenario 6 — All proofs unreadable

1. Repeat scenario 5's corruption for every attached proof.
2. Confirm generation still succeeds, producing just the receipt's own pages, with the dialog listing every proof as
   omitted.

## Automated coverage (for reference, not manual steps)

- `GenerateReceiptPdfInteractorTest` — proof loading + skip-list pass-through, using
  `FakePaymentProofPdfEmbeddingService`.
- A dedicated test class for `OpenPdfProofEmbeddingService` (real implementation) covering: no-proofs pass-through
  byte-equality, page-count/page-size assertions for image and PDF proofs (via `PdfReader` on the output), and
  skip behavior for a missing file / a corrupted file, using real temp files under `PaymentProofStorageService`'s
  fake.
