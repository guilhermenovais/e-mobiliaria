package com.guilherme.emobiliaria.receipt.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptFileService;
import com.guilherme.emobiliaria.receipt.infrastructure.repository.JdbcReceiptRepository;
import com.guilherme.emobiliaria.receipt.infrastructure.service.ReceiptFileServiceImpl;

public class ReceiptModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ReceiptRepository.class).to(JdbcReceiptRepository.class);
    bind(ReceiptFileService.class).to(ReceiptFileServiceImpl.class);
  }
}
