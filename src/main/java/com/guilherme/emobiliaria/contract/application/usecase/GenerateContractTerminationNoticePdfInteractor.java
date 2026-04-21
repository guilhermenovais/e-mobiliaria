package com.guilherme.emobiliaria.contract.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.contract.application.input.GenerateContractTerminationNoticePdfInput;
import com.guilherme.emobiliaria.contract.application.output.GenerateContractTerminationNoticePdfOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.contract.domain.service.ContractFileService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class GenerateContractTerminationNoticePdfInteractor {

  private final ContractRepository contractRepository;
  private final ContractFileService contractFileService;

  @Inject
  public GenerateContractTerminationNoticePdfInteractor(ContractRepository contractRepository,
      ContractFileService contractFileService) {
    this.contractRepository = contractRepository;
    this.contractFileService = contractFileService;
  }

  public GenerateContractTerminationNoticePdfOutput execute(
      GenerateContractTerminationNoticePdfInput input) {
    Contract contract = contractRepository.findById(input.contractId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));
    byte[] pdfBytes = contractFileService.generateTerminationNoticePdf(contract);
    return new GenerateContractTerminationNoticePdfOutput(pdfBytes);
  }
}
