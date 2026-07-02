# Feature Specification: Embed Payment Proofs as PDF Pages

**Feature Branch**: `006-embed-proofs-in-pdf`
**Created**: 2026-07-02
**Status**: Draft
**Input**: User description: "I would like the payment proofs of a receipt to be attached as pages in the generated PDF
file. I want the proportions of the original page to be kept, the attached pages should be adjusted to it."

## Clarifications

### Session 2026-07-02

- Q: FR-006 says the system must "inform the user which proof(s) were omitted" when a proof can't be read. How should
  that notification be delivered? → A: Dialog after generation — a modal/alert dialog appears once PDF generation
  finishes, listing which proof(s) were skipped and why.
- Q: If a PDF proof is partially corrupted — some of its pages are readable and others aren't — how should the system
  handle it? → A: Skip the entire proof (all-or-nothing); no partial page inclusion.

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Receive Proofs Bundled Into the Receipt PDF (Priority: P1)

A user generates the PDF for a receipt that has one or more payment proofs attached. Instead of receiving only the
receipt document, the user receives a single PDF file where the receipt content is followed by one page per payment
proof, so the proof of payment travels together with the receipt without needing to be opened or sent separately.

**Why this priority**: This is the entire value of the feature. Without it, proofs remain scattered as separate files
and the generated receipt PDF provides no evidence that payment occurred.

**Independent Test**: Can be fully tested by creating a receipt, attaching an image proof and a PDF proof to it,
generating the receipt PDF, and confirming the output file contains the receipt page(s) followed by a page for each
attached proof, in the order the proofs were attached.

**Acceptance Scenarios**:

1. **Given** a receipt with no payment proofs attached, **When** the user generates the receipt PDF, **Then** the
   output contains only the receipt content, unchanged from current behavior.
2. **Given** a receipt with one image proof attached, **When** the user generates the receipt PDF, **Then** the output
   contains the receipt content followed by one additional page showing that image.
3. **Given** a receipt with one PDF proof attached, **When** the user generates the receipt PDF, **Then** the output
   contains the receipt content followed by the page(s) of that proof PDF.
4. **Given** a receipt with multiple proofs attached (a mix of images and PDFs), **When** the user generates the
   receipt PDF, **Then** every proof appears as one or more pages in the output, in the order the proofs were
   attached, with no proof omitted.

---

### User Story 2 - Proof Pages Remain Undistorted (Priority: P2)

A user viewing the generated PDF wants each embedded proof page to look like a faithful reproduction of the original
file — a photographed receipt should not appear stretched, squeezed, or cropped in a way that hides its content.

**Why this priority**: A distorted or unreadable proof defeats the purpose of attaching it as evidence. This story
refines how User Story 1 renders each page but the feature has no value if proofs are unreadable.

**Independent Test**: Can be fully tested by attaching proofs of different shapes (a tall portrait photo, a wide
landscape scan, and a standard PDF page) and confirming that each embedded page shows the full original content
without stretching the image out of shape.

**Acceptance Scenarios**:

1. **Given** a proof image whose width-to-height ratio differs from the receipt's page ratio, **When** the proof page
   is rendered in the output PDF, **Then** the image is shown in full, at its original proportions, with no
   stretching or distortion.
2. **Given** a proof image smaller than the space available on its page, **When** the proof page is rendered, **Then**
   the image is scaled up to use the available space as fully as possible while keeping its original proportions.
3. **Given** a proof image larger than the space available on its page, **When** the proof page is rendered, **Then**
   the image is scaled down to fit while keeping its original proportions.

---

### Edge Cases

- What happens when a proof file referenced by the receipt is missing or unreadable at generation time (e.g. deleted
  from disk after being attached)? PDF generation continues for the remaining proofs and the receipt content; the
  unreadable proof is skipped and the user is informed which proof(s) could not be included.
- What happens when a PDF proof is itself password-protected or corrupted and cannot be read? It is treated the same
  as a missing/unreadable proof: skipped, with the user informed.
- What happens when a PDF proof is only partially readable (some pages readable, others corrupted)? The entire proof
  is skipped (all-or-nothing) rather than including only the readable pages.
- What happens when an attached image uses an orientation flag (e.g. EXIF rotation) different from its stored pixel
  orientation? The proof page reflects the visually correct (rotated) orientation.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: When generating a receipt PDF, the system MUST append one page per attached payment proof after the
  receipt's own content, in the order the proofs were attached to the receipt.
- **FR-002**: For an image payment proof, the system MUST render the full image content on its page without
  stretching, cropping, or distorting it relative to the image's original width-to-height ratio.
- **FR-003**: For a PDF payment proof, the system MUST include every page of that proof file in the generated
  output, preserving each source page's own proportions.
- **FR-004**: When an attached proof's proportions differ from the main receipt page's proportions, the system MUST
  scale the proof to fit within a page of the same fixed size and proportions as the receipt's own pages, centering
  it and leaving empty margins where the ratios don't match, rather than distorting the proof or changing the output
  page size.
- **FR-005**: A receipt with no attached payment proofs MUST generate a PDF identical in content to the current
  (pre-feature) behavior — no empty or placeholder pages are added.
- **FR-006**: If a payment proof cannot be read at PDF generation time (file missing, corrupted, or otherwise
  inaccessible), the system MUST continue generating the PDF with that proof skipped and, once generation finishes,
  MUST show the user a dialog listing which proof(s) were omitted and why, rather than failing the entire generation
  or silently producing an incomplete document.
- **FR-007**: The system MUST preserve the existing receipt PDF generation behavior (layout, content, and formatting
  of the receipt's own pages) unchanged; this feature only affects what is appended after those pages.

### Key Entities

- **Payment Proof**: An existing file (image or PDF) already attached to a receipt as evidence of payment. This
  feature reads its content and file type to determine how to render it as page(s) in the generated PDF; it does not
  change how proofs are attached, stored, or removed.
- **Generated Receipt PDF**: The output document produced when a user requests a PDF for a receipt. Previously
  contained only the receipt's own content; now also contains one page per page of content found in the receipt's
  attached payment proofs, appended in attachment order, all sharing the same page size as the receipt's own pages.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: A user generating a PDF for a receipt with attached payment proofs receives a single file containing
  both the receipt and all of its proofs, with no need to open or send proof files separately.
- **SC-002**: 100% of attached payment proofs that are readable at generation time appear as page(s) in the generated
  PDF, in the order they were attached.
- **SC-003**: Every embedded proof page shows the original proof content in full, without visible stretching or
  cropping, verifiable by visual comparison between the original proof file and its page in the output PDF.
- **SC-004**: Generating a PDF for a receipt with no attached proofs produces output identical to the current
  behavior, with zero regressions for existing receipts.

## Assumptions

- Only payment proofs already supported by the existing attachment feature (PDF and image files) need to be embedded;
  no new proof file types are introduced by this feature.
- Proof pages are appended after all of the receipt's own content pages, not interleaved or placed before it.
- The order of embedded proof pages follows the order proofs were attached to the receipt (the same order shown in
  the existing proof list/selection UI).
- Every page in the generated PDF, including proof pages, shares the same page size and proportions as the receipt's
  own pages — proofs are scaled and centered to fit rather than causing the output to have mixed page sizes.
- This feature applies to the single existing receipt PDF generation flow; no new entry point for generating PDFs is
  introduced.
