package com.guilherme.emobiliaria.receipt.domain.service;

import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

public class FakeReceiptFileService extends FakeImplementation implements ReceiptFileService {

  @Override
  public byte[] generateReceiptPdf(Receipt receipt) {
    maybeFail();
    return new byte[] {0x25, 0x50, 0x44, 0x46};
  }
}
