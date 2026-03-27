package com.guilherme.emobiliaria.shared.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.contract.di.ContractModule;
import com.guilherme.emobiliaria.person.di.PersonModule;
import com.guilherme.emobiliaria.property.di.PropertyModule;
import com.guilherme.emobiliaria.receipt.di.ReceiptModule;

public class AppModule extends AbstractModule {

  @Override
  protected void configure() {
    install(new PersonModule());
    install(new ContractModule());
    install(new PropertyModule());
    install(new ReceiptModule());
  }
}
