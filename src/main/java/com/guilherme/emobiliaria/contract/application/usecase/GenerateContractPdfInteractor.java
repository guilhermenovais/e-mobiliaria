package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.GenerateContractPdfInput;
import com.guilherme.emobiliaria.contract.application.output.GenerateContractPdfOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.contract.domain.service.ContractFileService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class GenerateContractPdfInteractor {

  private final ContractRepository contractRepository;
  private final ContractFileService contractFileService;

  public GenerateContractPdfInteractor(
      ContractRepository contractRepository,
      ContractFileService contractFileService
  ) {
    this.contractRepository = contractRepository;
    this.contractFileService = contractFileService;
  }

  public GenerateContractPdfOutput execute(GenerateContractPdfInput input) {
    Contract contract = contractRepository.findById(input.contractId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));
    byte[] pdfBytes = contractFileService.generateContractPdf(contract);
    return new GenerateContractPdfOutput(pdfBytes);
  }
}
