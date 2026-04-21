package com.guilherme.emobiliaria.contract.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.contract.application.input.GenerateContractRescissionPdfInput;
import com.guilherme.emobiliaria.contract.application.output.GenerateContractRescissionPdfOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.contract.domain.service.ContractFileService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class GenerateContractRescissionPdfInteractor {

  private final ContractRepository contractRepository;
  private final ContractFileService contractFileService;

  @Inject
  public GenerateContractRescissionPdfInteractor(ContractRepository contractRepository,
      ContractFileService contractFileService) {
    this.contractRepository = contractRepository;
    this.contractFileService = contractFileService;
  }

  public GenerateContractRescissionPdfOutput execute(GenerateContractRescissionPdfInput input) {
    Contract contract = contractRepository.findById(input.contractId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));
    byte[] pdfBytes = contractFileService.generateRescissionPdf(contract);
    return new GenerateContractRescissionPdfOutput(pdfBytes);
  }
}
