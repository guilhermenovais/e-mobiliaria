package com.guilherme.emobiliaria.shared.di;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.guilherme.emobiliaria.contract.di.ContractModule;
import com.guilherme.emobiliaria.person.di.PersonModule;
import com.guilherme.emobiliaria.property.di.PropertyModule;
import com.guilherme.emobiliaria.receipt.di.ReceiptModule;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;

import javax.sql.DataSource;

public class AppModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataSource.class).toProvider(DataSourceProvider.class).in(Scopes.SINGLETON);
    bind(PdfGenerationService.class);

    install(new PersonModule());
    install(new ContractModule());
    install(new PropertyModule());
    install(new ReceiptModule());
  }
}
