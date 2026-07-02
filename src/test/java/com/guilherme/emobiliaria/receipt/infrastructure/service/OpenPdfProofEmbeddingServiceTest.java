package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import com.guilherme.emobiliaria.receipt.domain.service.FakePaymentProofStorageService;
import com.guilherme.emobiliaria.receipt.domain.service.ProofEmbeddingResult;
import com.guilherme.emobiliaria.receipt.domain.service.SkipReason;
import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OpenPdfProofEmbeddingServiceTest {

  private static final float PAGE_WIDTH = 595f;
  private static final float PAGE_HEIGHT = 842f;
  private static final float TOLERANCE = 1f;

  private FakePaymentProofStorageService storage;
  private OpenPdfProofEmbeddingService service;

  @BeforeEach
  void setUp() {
    storage = new FakePaymentProofStorageService();
    service = new OpenPdfProofEmbeddingService(storage);
  }

  @AfterEach
  void tearDown() {
    // no explicit cleanup needed - FakePaymentProofStorageService uses a temp directory
  }

  // ── Fixture helpers ──────────────────────────────────────────────────────

  private byte[] createReceiptPdf(float width, float height, int pageCount) throws Exception {
    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(new Rectangle(width, height));
    PdfWriter.getInstance(document, out);
    document.open();
    for (int i = 0; i < pageCount; i++) {
      if (i > 0) {
        document.newPage();
      }
      document.add(new Paragraph("Receipt page " + (i + 1)));
    }
    document.close();
    return out.toByteArray();
  }

  private PaymentProof imageProof(int width, int height, String displayName) throws IOException {
    BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.BLUE);
    g.fillRect(0, 0, width, height);
    g.dispose();
    Path tmp = Files.createTempFile("proof-image", ".jpg");
    ImageIO.write(image, "jpg", tmp.toFile());
    String storedName = storage.copyToStorage(tmp, "photo.jpg");
    Files.deleteIfExists(tmp);
    return PaymentProof.create("photo.jpg", displayName, storedName, ProofFileType.IMAGE,
        LocalDate.now(), 1L);
  }

  private PaymentProof exifOrientedImageProof(int rawWidth, int rawHeight, int orientation,
      String displayName) throws IOException {
    BufferedImage image = new BufferedImage(rawWidth, rawHeight, BufferedImage.TYPE_INT_RGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Color.RED);
    g.fillRect(0, 0, rawWidth, rawHeight);
    g.dispose();
    ByteArrayOutputStream raw = new ByteArrayOutputStream();
    ImageIO.write(image, "jpg", raw);
    byte[] withExif = injectExifOrientation(raw.toByteArray(), orientation);
    Path tmp = Files.createTempFile("proof-image-exif", ".jpg");
    Files.write(tmp, withExif);
    String storedName = storage.copyToStorage(tmp, "photo.jpg");
    Files.deleteIfExists(tmp);
    return PaymentProof.create("photo.jpg", displayName, storedName, ProofFileType.IMAGE,
        LocalDate.now(), 1L);
  }

  private byte[] injectExifOrientation(byte[] jpegBytes, int orientation) {
    byte[] tiff = {'I', 'I', 0x2A, 0x00, 0x08, 0x00, 0x00, 0x00, // TIFF header, IFD0 at offset 8
        0x01, 0x00, // 1 IFD entry
        0x12, 0x01, // tag 0x0112 (Orientation)
        0x03, 0x00, // type SHORT
        0x01, 0x00, 0x00, 0x00, // count 1
        (byte) orientation, 0x00, 0x00, 0x00, // value + padding
        0x00, 0x00, 0x00, 0x00 // next IFD offset (none)
    };
    byte[] exifHeader = "Exif\0\0".getBytes(StandardCharsets.US_ASCII);
    int app1Length = 2 + exifHeader.length + tiff.length;

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    out.write(0xFF);
    out.write(0xD8); // SOI
    out.write(0xFF);
    out.write(0xE1); // APP1
    out.write((app1Length >> 8) & 0xFF);
    out.write(app1Length & 0xFF);
    out.writeBytes(exifHeader);
    out.writeBytes(tiff);
    out.write(jpegBytes, 2, jpegBytes.length - 2);
    return out.toByteArray();
  }

  private PaymentProof pdfProof(float width, float height, int pageCount, String displayName)
      throws Exception {
    byte[] pdfBytes = createReceiptPdf(width, height, pageCount);
    Path tmp = Files.createTempFile("proof-pdf", ".pdf");
    Files.write(tmp, pdfBytes);
    String storedName = storage.copyToStorage(tmp, "document.pdf");
    Files.deleteIfExists(tmp);
    return PaymentProof.create("document.pdf", displayName, storedName, ProofFileType.PDF,
        LocalDate.now(), 1L);
  }

  private PaymentProof missingProof(String displayName) {
    return PaymentProof.create("missing.jpg", displayName, UUID.randomUUID() + "-missing.jpg",
        ProofFileType.IMAGE, LocalDate.now(), 1L);
  }

  private PaymentProof corruptedImageProof(String displayName) throws IOException {
    Path tmp = Files.createTempFile("proof-corrupt", ".jpg");
    Files.write(tmp, "not a real image".getBytes(StandardCharsets.UTF_8));
    String storedName = storage.copyToStorage(tmp, "corrupt.jpg");
    Files.deleteIfExists(tmp);
    return PaymentProof.create("corrupt.jpg", displayName, storedName, ProofFileType.IMAGE,
        LocalDate.now(), 1L);
  }

  /**
   * Extracts the [a b c d e f] matrix of the "cm" operator immediately preceding the image/
   * template placement on the given page, as emitted by PdfContentByte.addImage/addTemplate.
   */
  private float[] extractCtm(byte[] pdfBytes, int pageNum) throws IOException {
    PdfReader reader = new PdfReader(pdfBytes);
    byte[] content = reader.getPageContent(pageNum);
    reader.close();
    String text = new String(content, StandardCharsets.ISO_8859_1);
    Pattern pattern = Pattern.compile(
        "(-?[0-9.]+) (-?[0-9.]+) (-?[0-9.]+) (-?[0-9.]+) (-?[0-9.]+) (-?[0-9.]+) cm");
    Matcher matcher = pattern.matcher(text);
    assertTrue(matcher.find(), "Expected a 'cm' transform operator in page content: " + text);
    return new float[] {Float.parseFloat(matcher.group(1)), Float.parseFloat(matcher.group(2)),
        Float.parseFloat(matcher.group(3)), Float.parseFloat(matcher.group(4)),
        Float.parseFloat(matcher.group(5)), Float.parseFloat(matcher.group(6))};
  }

  // ── Tests ───────────────────────────────────────────────────────────────


  @Nested
  class Embed {

    @Test
    @DisplayName("When proofs list is empty, should return the receipt bytes untouched")
    void shouldReturnUntouchedBytesWhenProofsEmpty() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of());

      assertArrayEquals(receiptPdf, result.pdfBytes());
      assertTrue(result.skippedProofs().isEmpty());
    }

    @Test
    @DisplayName("When one image proof is attached, should append exactly one page")
    void shouldAppendOnePageForImageProof() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      PaymentProof proof = imageProof(800, 600, "Comprovante");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(proof));

      PdfReader reader = new PdfReader(result.pdfBytes());
      assertEquals(2, reader.getNumberOfPages());
      reader.close();
      assertTrue(result.skippedProofs().isEmpty());
    }

    @Test
    @DisplayName(
        "When one multi-page PDF proof is attached, should append one page per source page")
    void shouldAppendOnePagePerSourcePageForPdfProof() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      PaymentProof proof = pdfProof(400, 300, 3, "Comprovante PDF");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(proof));

      PdfReader reader = new PdfReader(result.pdfBytes());
      assertEquals(4, reader.getNumberOfPages());
      reader.close();
      assertTrue(result.skippedProofs().isEmpty());
    }

    @Test
    @DisplayName(
        "When mixed proofs are attached, should keep attachment order and shared page size")
    void shouldKeepAttachmentOrderAndSharedPageSizeForMixedProofs() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      PaymentProof firstImage = imageProof(300, 600, "First");
      PaymentProof pdf = pdfProof(400, 300, 2, "Second");
      PaymentProof secondImage = imageProof(900, 400, "Third");

      ProofEmbeddingResult result =
          service.embed(receiptPdf, List.of(firstImage, pdf, secondImage));

      PdfReader reader = new PdfReader(result.pdfBytes());
      assertEquals(5, reader.getNumberOfPages());
      for (int i = 1; i <= reader.getNumberOfPages(); i++) {
        Rectangle size = reader.getPageSizeWithRotation(i);
        assertEquals(PAGE_WIDTH, size.getWidth(), TOLERANCE);
        assertEquals(PAGE_HEIGHT, size.getHeight(), TOLERANCE);
      }
      reader.close();
      assertTrue(result.skippedProofs().isEmpty());
    }

    @Test
    @DisplayName(
        "When a proof's stored file is missing, should skip it with MISSING and still succeed")
    void shouldSkipMissingProofAndStillSucceed() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      PaymentProof readable = imageProof(400, 400, "Readable");
      PaymentProof missing = missingProof("Missing");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(readable, missing));

      PdfReader reader = new PdfReader(result.pdfBytes());
      assertEquals(2, reader.getNumberOfPages());
      reader.close();
      assertEquals(1, result.skippedProofs().size());
      assertEquals(missing, result.skippedProofs().get(0).proof());
      assertEquals(SkipReason.MISSING, result.skippedProofs().get(0).reason());
    }

    @Test
    @DisplayName(
        "When a proof's file is corrupted, should skip it with UNREADABLE and still succeed")
    void shouldSkipCorruptedProofAndStillSucceed() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      PaymentProof corrupted = corruptedImageProof("Corrupted");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(corrupted));

      PdfReader reader = new PdfReader(result.pdfBytes());
      assertEquals(1, reader.getNumberOfPages());
      reader.close();
      assertEquals(1, result.skippedProofs().size());
      assertEquals(SkipReason.UNREADABLE, result.skippedProofs().get(0).reason());
    }

    @Test
    @DisplayName(
        "When every proof is unreadable, should keep only the receipt's own pages and list all as skipped")
    void shouldKeepOnlyReceiptPagesWhenAllProofsUnreadable() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 2);
      PaymentProof missing = missingProof("Missing");
      PaymentProof corrupted = corruptedImageProof("Corrupted");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(missing, corrupted));

      PdfReader reader = new PdfReader(result.pdfBytes());
      assertEquals(2, reader.getNumberOfPages());
      reader.close();
      assertEquals(2, result.skippedProofs().size());
      assertTrue(result.skippedProofs().stream().anyMatch(s -> s.proof().equals(missing)));
      assertTrue(result.skippedProofs().stream().anyMatch(s -> s.proof().equals(corrupted)));
    }

    @Test
    @DisplayName(
        "When a portrait image is narrower than the page, should scale uniformly and center with margins on X")
    void shouldScaleUniformlyAndCenterPortraitImage() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      int contentWidth = 200;
      int contentHeight = 400;
      PaymentProof proof = imageProof(contentWidth, contentHeight, "Portrait");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(proof));

      float[] ctm = extractCtm(result.pdfBytes(), 2);
      float placedWidth = ctm[0];
      float placedHeight = ctm[3];
      float offsetX = ctm[4];
      float offsetY = ctm[5];

      assertEquals((double) contentWidth / contentHeight, (double) placedWidth / placedHeight,
          0.01);
      assertTrue(placedHeight >= PAGE_HEIGHT - TOLERANCE * 2,
          "Portrait image should be scaled to fill the page height");
      assertTrue(offsetX > 0, "Narrower-than-page content should have blank margins on X");
      assertEquals(0f, offsetY, TOLERANCE);
    }

    @Test
    @DisplayName(
        "When a landscape image is wider than the page, should scale down uniformly and center with margins on Y")
    void shouldScaleDownUniformlyAndCenterLandscapeImage() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      int contentWidth = 1600;
      int contentHeight = 800;
      PaymentProof proof = imageProof(contentWidth, contentHeight, "Landscape");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(proof));

      float[] ctm = extractCtm(result.pdfBytes(), 2);
      float placedWidth = ctm[0];
      float placedHeight = ctm[3];
      float offsetY = ctm[5];

      assertEquals((double) contentWidth / contentHeight, (double) placedWidth / placedHeight,
          0.01);
      assertTrue(placedWidth >= PAGE_WIDTH - TOLERANCE * 2,
          "Landscape image should be scaled to fill the page width");
      assertTrue(offsetY > 0, "Shorter-than-page content should have blank margins on Y");
    }

    @Test
    @DisplayName(
        "When an image is smaller than the page, should scale it up while preserving aspect ratio")
    void shouldScaleUpSmallImagePreservingAspectRatio() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      int contentSize = 100;
      PaymentProof proof = imageProof(contentSize, contentSize, "Small");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(proof));

      float[] ctm = extractCtm(result.pdfBytes(), 2);
      float placedWidth = ctm[0];
      float placedHeight = ctm[3];

      assertTrue(placedWidth > contentSize, "Small content should be scaled up to fill the page");
      assertEquals(placedWidth, placedHeight, TOLERANCE);
    }

    @Test
    @DisplayName(
        "When an image has a non-default EXIF orientation, should place it using its visually-correct dimensions")
    void shouldUseVisuallyCorrectDimensionsForExifOrientedImage() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 1);
      int rawWidth = 800;
      int rawHeight = 400;
      PaymentProof proof = exifOrientedImageProof(rawWidth, rawHeight, 6, "Rotated");

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of(proof));

      float[] ctm = extractCtm(result.pdfBytes(), 2);
      float placedWidth = ctm[0];
      float placedHeight = ctm[3];

      // Orientation 6 means the raw (landscape) pixels must be rotated 90deg to display
      // correctly, so the effective content is portrait (rawHeight x rawWidth), not the raw
      // landscape dimensions.
      assertTrue(placedHeight > placedWidth,
          "EXIF-rotated content should be placed as portrait, not raw landscape");
      assertEquals((double) rawHeight / rawWidth, (double) placedWidth / placedHeight, 0.02);
    }

    @Test
    @DisplayName(
        "When proofs list is empty, should return the exact same byte array content (SC-004)")
    void shouldReturnByteIdenticalOutputWhenProofsEmpty() throws Exception {
      byte[] receiptPdf = createReceiptPdf(PAGE_WIDTH, PAGE_HEIGHT, 3);

      ProofEmbeddingResult result = service.embed(receiptPdf, List.of());

      assertArrayEquals(receiptPdf, result.pdfBytes());
    }
  }
}
