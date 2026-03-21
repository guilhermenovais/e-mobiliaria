package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.FindAllContractsByPropertyIdInput;
import com.guilherme.emobiliaria.contract.application.output.FindAllContractsByPropertyIdOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public class FindAllContractsByPropertyIdInteractor {

  private final ContractRepository contractRepository;

  public FindAllContractsByPropertyIdInteractor(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  public FindAllContractsByPropertyIdOutput execute(FindAllContractsByPropertyIdInput input) {
    PagedResult<Contract> result =
        contractRepository.findAllByPropertyId(input.propertyId(), input.pagination());
    return new FindAllContractsByPropertyIdOutput(result);
  }
}
