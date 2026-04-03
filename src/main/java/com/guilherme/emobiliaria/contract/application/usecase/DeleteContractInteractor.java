package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.DeleteContractInput;
import com.guilherme.emobiliaria.contract.application.output.DeleteContractOutput;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.google.inject.Inject;

public class DeleteContractInteractor {

  private final ContractRepository contractRepository;

  @Inject
  public DeleteContractInteractor(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  public DeleteContractOutput execute(DeleteContractInput input) {
    contractRepository.delete(input.id());
    return new DeleteContractOutput();
  }
}
