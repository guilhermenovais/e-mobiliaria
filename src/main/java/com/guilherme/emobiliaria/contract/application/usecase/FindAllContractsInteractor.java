package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.FindAllContractsInput;
import com.guilherme.emobiliaria.contract.application.output.FindAllContractsOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.google.inject.Inject;

public class FindAllContractsInteractor {

  private final ContractRepository contractRepository;

  @Inject
  public FindAllContractsInteractor(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  public FindAllContractsOutput execute(FindAllContractsInput input) {
    PagedResult<Contract> result = contractRepository.findAll(input.pagination(), input.filter());
    return new FindAllContractsOutput(result);
  }
}
