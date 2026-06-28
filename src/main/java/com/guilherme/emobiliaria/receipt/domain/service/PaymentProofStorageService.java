package com.guilherme.emobiliaria.receipt.domain.service;

import java.nio.file.Path;

public interface PaymentProofStorageService {
  String copyToStorage(Path sourceFile, String originalFileName);

  String copyBytesToStorage(byte[] imageBytes, String originalFileName);

  void delete(String storedFileName);

  Path resolve(String storedFileName);
}
