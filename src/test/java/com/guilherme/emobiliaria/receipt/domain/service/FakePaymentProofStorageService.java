package com.guilherme.emobiliaria.receipt.domain.service;

import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class FakePaymentProofStorageService extends FakeImplementation
    implements PaymentProofStorageService {

  private final List<String> deletedFiles = new ArrayList<>();
  private final Path storageDir;

  public FakePaymentProofStorageService() {
    try {
      this.storageDir = Files.createTempDirectory("fake-proof-storage");
    } catch (IOException e) {
      throw new IllegalStateException("Failed to create temp storage directory", e);
    }
  }

  @Override
  public String copyToStorage(Path sourceFile, String originalFileName) {
    maybeFail();
    String storedName = UUID.randomUUID() + deriveExtension(originalFileName);
    try {
      Files.copy(sourceFile, storageDir.resolve(storedName));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to copy file to fake storage", e);
    }
    return storedName;
  }

  @Override
  public String copyBytesToStorage(byte[] imageBytes, String originalFileName) {
    maybeFail();
    String storedName = UUID.randomUUID() + deriveExtension(originalFileName);
    try {
      Files.write(storageDir.resolve(storedName), imageBytes);
    } catch (IOException e) {
      throw new IllegalStateException("Failed to write bytes to fake storage", e);
    }
    return storedName;
  }

  @Override
  public void delete(String storedFileName) {
    maybeFail();
    deletedFiles.add(storedFileName);
    try {
      Files.deleteIfExists(storageDir.resolve(storedFileName));
    } catch (IOException e) {
      throw new IllegalStateException("Failed to delete file from fake storage", e);
    }
  }

  @Override
  public Path resolve(String storedFileName) {
    maybeFail();
    return storageDir.resolve(storedFileName);
  }

  public List<String> getDeletedFiles() {
    return new ArrayList<>(deletedFiles);
  }

  private String deriveExtension(String filename) {
    if (filename == null) {
      return "";
    }
    int dot = filename.lastIndexOf('.');
    return dot >= 0 ? filename.substring(dot) : "";
  }
}
