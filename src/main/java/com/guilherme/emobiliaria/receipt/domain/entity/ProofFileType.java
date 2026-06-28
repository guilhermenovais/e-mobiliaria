package com.guilherme.emobiliaria.receipt.domain.entity;

import java.util.Locale;
import java.util.Optional;
import java.util.Set;

public enum ProofFileType {
  PDF, IMAGE;

  private static final Set<String> PDF_EXTENSIONS = Set.of(".pdf");
  private static final Set<String> IMAGE_EXTENSIONS =
      Set.of(".jpg", ".jpeg", ".png", ".gif", ".bmp", ".webp", ".tiff", ".tif");

  public static Optional<ProofFileType> fromExtension(String filename) {
    if (filename == null) {
      return Optional.empty();
    }
    String lower = filename.toLowerCase(Locale.ROOT);
    if (PDF_EXTENSIONS.stream().anyMatch(lower::endsWith)) {
      return Optional.of(PDF);
    }
    if (IMAGE_EXTENSIONS.stream().anyMatch(lower::endsWith)) {
      return Optional.of(IMAGE);
    }
    return Optional.empty();
  }
}
