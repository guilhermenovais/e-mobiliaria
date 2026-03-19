package com.guilherme.emobiliaria.receipt.domain.service;

import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;

public interface ReceiptFileService {
  byte[] generateReceiptPdf(Receipt receipt);
}
