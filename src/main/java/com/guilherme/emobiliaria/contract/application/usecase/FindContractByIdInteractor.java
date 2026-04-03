package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.FindContractByIdInput;
import com.guilherme.emobiliaria.contract.application.output.FindContractByIdOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.google.inject.Inject;

public class FindContractByIdInteractor {

  private final ContractRepository contractRepository;

  @Inject
  public FindContractByIdInteractor(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  public FindContractByIdOutput execute(FindContractByIdInput input) {
    Contract contract = contractRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));
    return new FindContractByIdOutput(contract);
  }
}
