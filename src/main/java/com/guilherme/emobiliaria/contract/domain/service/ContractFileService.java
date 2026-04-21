package com.guilherme.emobiliaria.contract.domain.service;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;

public interface ContractFileService {
  byte[] generateContractPdf(Contract contract);

  byte[] generateRescissionPdf(Contract contract);

  byte[] generateTerminationNoticePdf(Contract contract);
}
