package com.guilherme.emobiliaria.contract.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.contract.domain.repository.PaymentAccountRepository;
import com.guilherme.emobiliaria.contract.domain.service.ContractFileService;
import com.guilherme.emobiliaria.contract.infrastructure.repository.JdbcContractRepository;
import com.guilherme.emobiliaria.contract.infrastructure.repository.JdbcPaymentAccountRepository;
import com.guilherme.emobiliaria.contract.infrastructure.service.ContractFileServiceImpl;

public class ContractModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(PaymentAccountRepository.class).to(JdbcPaymentAccountRepository.class);
    bind(ContractRepository.class).to(JdbcContractRepository.class);
    bind(ContractFileService.class).to(ContractFileServiceImpl.class);
  }
}
