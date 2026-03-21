package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.DeleteContractInput;
import com.guilherme.emobiliaria.contract.application.output.DeleteContractOutput;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;

public class DeleteContractInteractor {

  private final ContractRepository contractRepository;

  public DeleteContractInteractor(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  public DeleteContractOutput execute(DeleteContractInput input) {
    contractRepository.delete(input.id());
    return new DeleteContractOutput();
  }
}
