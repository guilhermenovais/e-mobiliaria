package com.guilherme.emobiliaria.contract.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.contract.application.input.RescindContractInput;
import com.guilherme.emobiliaria.contract.application.output.RescindContractOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.contract.domain.service.ContractFileService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class RescindContractInteractor {

  private final ContractRepository contractRepository;
  private final ContractFileService contractFileService;

  @Inject
  public RescindContractInteractor(ContractRepository contractRepository,
      ContractFileService contractFileService) {
    this.contractRepository = contractRepository;
    this.contractFileService = contractFileService;
  }

  public RescindContractOutput execute(RescindContractInput input) {
    Contract contract = contractRepository.findById(input.contractId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));
    contract.rescind(input.rescissionDate());
    Contract rescindedContract = contractRepository.update(contract);
    byte[] pdfBytes = contractFileService.generateRescissionPdf(rescindedContract);
    return new RescindContractOutput(rescindedContract, pdfBytes);
  }
}
