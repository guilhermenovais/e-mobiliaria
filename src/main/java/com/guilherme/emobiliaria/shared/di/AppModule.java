package com.guilherme.emobiliaria.shared.di;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.guilherme.emobiliaria.config.di.ConfigModule;
import com.guilherme.emobiliaria.contract.di.ContractModule;
import com.guilherme.emobiliaria.person.di.PersonModule;
import com.guilherme.emobiliaria.property.di.PropertyModule;
import com.guilherme.emobiliaria.receipt.di.ReceiptModule;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import jakarta.inject.Singleton;

import javax.sql.DataSource;

public class AppModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DataSource.class).toProvider(DataSourceProvider.class).in(Scopes.SINGLETON);
    bind(PdfGenerationService.class);
    bind(NavigationService.class).in(Scopes.SINGLETON);

    install(new ConfigModule());
    install(new PersonModule());
    install(new ContractModule());
    install(new PropertyModule());
    install(new ReceiptModule());
  }

  @Provides
  @Singleton
  GuiceFxmlLoader provideGuiceFxmlLoader(Injector injector) {
    return new GuiceFxmlLoader(injector);
  }
}
