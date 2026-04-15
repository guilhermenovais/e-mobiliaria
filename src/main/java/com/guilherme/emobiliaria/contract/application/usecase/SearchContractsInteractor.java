package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.SearchContractsInput;
import com.guilherme.emobiliaria.contract.application.output.SearchContractsOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import jakarta.inject.Inject;

public class SearchContractsInteractor {

  private final ContractRepository contractRepository;

  @Inject
  public SearchContractsInteractor(ContractRepository contractRepository) {
    this.contractRepository = contractRepository;
  }

  public SearchContractsOutput execute(SearchContractsInput input) {
    PagedResult<Contract> result = contractRepository.search(input.query(), input.pagination(), input.filter());
    return new SearchContractsOutput(result);
  }
}
