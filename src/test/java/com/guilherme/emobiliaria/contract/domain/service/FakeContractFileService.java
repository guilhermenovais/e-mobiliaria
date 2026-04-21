package com.guilherme.emobiliaria.contract.domain.service;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

public class FakeContractFileService extends FakeImplementation implements ContractFileService {
  private static final byte[] DUMMY_PDF_BYTES = new byte[] {0x25, 0x50, 0x44, 0x46};

  @Override
  public byte[] generateContractPdf(Contract contract) {
    maybeFail();
    return DUMMY_PDF_BYTES;
  }

  @Override
  public byte[] generateRescissionPdf(Contract contract) {
    maybeFail();
    return DUMMY_PDF_BYTES;
  }

  @Override
  public byte[] generateTerminationNoticePdf(Contract contract) {
    maybeFail();
    return DUMMY_PDF_BYTES;
  }
}
