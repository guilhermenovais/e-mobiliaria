package com.guilherme.emobiliaria.receipt.infrastructure.service;

import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofStorageService;
import com.guilherme.emobiliaria.shared.persistence.AppDataPaths;
import jakarta.inject.Inject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

public class PaymentProofStorageServiceImpl implements PaymentProofStorageService {

  @Inject
  public PaymentProofStorageServiceImpl() {
  }

  @Override
  public String copyToStorage(Path sourceFile, String originalFileName) {
    String storedName = UUID.randomUUID() + deriveExtension(originalFileName);
    try {
      Files.copy(sourceFile, AppDataPaths.proofStorageDirectory().resolve(storedName));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to copy file to proof storage", e);
    }
    return storedName;
  }

  @Override
  public String copyBytesToStorage(byte[] imageBytes, String originalFileName) {
    String storedName = UUID.randomUUID() + deriveExtension(originalFileName);
    try {
      Files.write(AppDataPaths.proofStorageDirectory().resolve(storedName), imageBytes);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write proof bytes to storage", e);
    }
    return storedName;
  }

  @Override
  public void delete(String storedFileName) {
    try {
      Files.deleteIfExists(AppDataPaths.proofStorageDirectory().resolve(storedFileName));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to delete proof file: " + storedFileName, e);
    }
  }

  @Override
  public Path resolve(String storedFileName) {
    return AppDataPaths.proofStorageDirectory().resolve(storedFileName);
  }

  private String deriveExtension(String filename) {
    if (filename == null) {
      return "";
    }
    int dot = filename.lastIndexOf('.');
    return dot >= 0 ? filename.substring(dot) : "";
  }
}
