package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifIFD0Directory;
import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofPdfEmbeddingService;
import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofStorageService;
import com.guilherme.emobiliaria.receipt.domain.service.ProofEmbeddingResult;
import com.guilherme.emobiliaria.receipt.domain.service.SkipReason;
import com.guilherme.emobiliaria.receipt.domain.service.SkippedProof;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.inject.Inject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class OpenPdfProofEmbeddingService implements PaymentProofPdfEmbeddingService {

  private final PaymentProofStorageService paymentProofStorageService;

  @Inject
  public OpenPdfProofEmbeddingService(PaymentProofStorageService paymentProofStorageService) {
    this.paymentProofStorageService = paymentProofStorageService;
  }

  @Override
  public ProofEmbeddingResult embed(byte[] receiptPdf, List<PaymentProof> proofs) {
    if (proofs.isEmpty()) {
      return new ProofEmbeddingResult(receiptPdf, List.of());
    }

    try {
      PdfReader baseReader = new PdfReader(receiptPdf);
      Rectangle pageSize = baseReader.getPageSizeWithRotation(1);

      ByteArrayOutputStream out = new ByteArrayOutputStream();
      Document document = new Document(pageSize);
      PdfCopy copy = new PdfCopy(document, out);
      document.open();

      int baseCount = baseReader.getNumberOfPages();
      for (int i = 1; i <= baseCount; i++) {
        copy.addPage(copy.getImportedPage(baseReader, i));
      }
      baseReader.close();

      List<SkippedProof> skippedProofs = new ArrayList<>();
      for (PaymentProof proof : proofs) {
        try {
          byte[] stagedProofPdf = buildProofPdf(proof, pageSize);
          PdfReader stagedReader = new PdfReader(stagedProofPdf);
          int stagedCount = stagedReader.getNumberOfPages();
          for (int p = 1; p <= stagedCount; p++) {
            copy.addPage(copy.getImportedPage(stagedReader, p));
          }
          stagedReader.close();
        } catch (ProofMissingException e) {
          skippedProofs.add(new SkippedProof(proof, SkipReason.MISSING));
        } catch (Exception e) {
          skippedProofs.add(new SkippedProof(proof, SkipReason.UNREADABLE));
        }
      }

      document.close();
      return new ProofEmbeddingResult(out.toByteArray(), skippedProofs);
    } catch (IOException | DocumentException e) {
      throw new IllegalStateException("Failed to embed payment proofs into receipt PDF", e);
    }
  }

  private byte[] buildProofPdf(PaymentProof proof, Rectangle pageSize)
      throws IOException, DocumentException {
    Path resolved = paymentProofStorageService.resolve(proof.getStoredFileName());
    if (!Files.exists(resolved)) {
      throw new ProofMissingException();
    }
    return switch (proof.getFileType()) {
      case IMAGE -> buildImageProofPdf(resolved, pageSize);
      case PDF -> buildPdfProofPdf(resolved, pageSize);
    };
  }

  private byte[] buildImageProofPdf(Path file, Rectangle pageSize)
      throws IOException, DocumentException {
    BufferedImage rawImage = ImageIO.read(file.toFile());
    if (rawImage == null) {
      throw new IOException("Unsupported or corrupted image file: " + file);
    }
    BufferedImage orientedImage = applyExifOrientation(file, rawImage);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(pageSize);
    PdfWriter writer = PdfWriter.getInstance(document, out);
    document.open();
    document.newPage();

    Image pdfImage = Image.getInstance(orientedImage, null);
    PdfContentByte cb = writer.getDirectContent();
    drawContainedImage(cb, pdfImage, orientedImage.getWidth(), orientedImage.getHeight(), pageSize);

    document.close();
    return out.toByteArray();
  }

  private byte[] buildPdfProofPdf(Path file, Rectangle pageSize)
      throws IOException, DocumentException {
    PdfReader sourceReader = new PdfReader(file.toString());
    int sourcePages = sourceReader.getNumberOfPages();

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    Document document = new Document(pageSize);
    PdfWriter writer = PdfWriter.getInstance(document, out);
    document.open();

    for (int i = 1; i <= sourcePages; i++) {
      document.newPage();
      Rectangle sourceSize = sourceReader.getPageSizeWithRotation(i);
      PdfImportedPage template = writer.getImportedPage(sourceReader, i);
      PdfContentByte cb = writer.getDirectContent();
      drawContainedTemplate(cb, template, sourceSize.getWidth(), sourceSize.getHeight(), pageSize);
    }

    document.close();
    sourceReader.close();
    return out.toByteArray();
  }

  private void drawContainedImage(PdfContentByte cb, Image image, float contentWidth,
      float contentHeight, Rectangle pageSize) throws DocumentException {
    float scale =
        Math.min(pageSize.getWidth() / contentWidth, pageSize.getHeight() / contentHeight);
    float scaledWidth = contentWidth * scale;
    float scaledHeight = contentHeight * scale;
    float offsetX = (pageSize.getWidth() - scaledWidth) / 2f;
    float offsetY = (pageSize.getHeight() - scaledHeight) / 2f;
    cb.addImage(image, scaledWidth, 0, 0, scaledHeight, offsetX, offsetY);
  }

  private void drawContainedTemplate(PdfContentByte cb, PdfImportedPage template,
      float contentWidth, float contentHeight, Rectangle pageSize) {
    float scale =
        Math.min(pageSize.getWidth() / contentWidth, pageSize.getHeight() / contentHeight);
    float offsetX = (pageSize.getWidth() - contentWidth * scale) / 2f;
    float offsetY = (pageSize.getHeight() - contentHeight * scale) / 2f;
    cb.addTemplate(template, scale, 0, 0, scale, offsetX, offsetY);
  }

  // ── EXIF orientation correction ────────────────────────────────────────────

  private BufferedImage applyExifOrientation(Path file, BufferedImage image) {
    int orientation = readExifOrientation(file);
    if (orientation <= 1 || orientation > 8) {
      return image;
    }
    return transformForOrientation(image, orientation);
  }

  private int readExifOrientation(Path file) {
    try {
      Metadata metadata = ImageMetadataReader.readMetadata(file.toFile());
      ExifIFD0Directory directory = metadata.getFirstDirectoryOfType(ExifIFD0Directory.class);
      if (directory != null && directory.containsTag(ExifIFD0Directory.TAG_ORIENTATION)) {
        return directory.getInt(ExifIFD0Directory.TAG_ORIENTATION);
      }
    } catch (Exception e) {
      // No EXIF metadata (or unreadable metadata) — treat as default orientation.
    }
    return 1;
  }

  /**
   * Maps the eight standard EXIF orientation values to the affine transform that makes the decoded
   * pixels match the visually-correct orientation.
   */
  private BufferedImage transformForOrientation(BufferedImage image, int orientation) {
    int width = image.getWidth();
    int height = image.getHeight();
    AffineTransform t = new AffineTransform();

    switch (orientation) {
      case 2 -> {
        t.scale(-1.0, 1.0);
        t.translate(-width, 0);
      }
      case 3 -> {
        t.translate(width, height);
        t.rotate(Math.PI);
      }
      case 4 -> {
        t.scale(1.0, -1.0);
        t.translate(0, -height);
      }
      case 5 -> {
        t.rotate(-Math.PI / 2);
        t.scale(-1.0, 1.0);
      }
      case 6 -> {
        t.translate(height, 0);
        t.rotate(Math.PI / 2);
      }
      case 7 -> {
        t.scale(-1.0, 1.0);
        t.translate(-height, 0);
        t.translate(0, width);
        t.rotate(3 * Math.PI / 2);
      }
      case 8 -> {
        t.translate(0, width);
        t.rotate(3 * Math.PI / 2);
      }
      default -> {
        return image;
      }
    }

    AffineTransformOp op = new AffineTransformOp(t, AffineTransformOp.TYPE_BILINEAR);
    BufferedImage destination = op.createCompatibleDestImage(image,
        image.getType() == BufferedImage.TYPE_BYTE_GRAY ? null : image.getColorModel());
    Graphics2D g = destination.createGraphics();
    g.drawImage(image, t, null);
    g.dispose();
    return destination;
  }

  // ── Skip classification ─────────────────────────────────────────────────────


  private static class ProofMissingException extends RuntimeException {
  }
}
