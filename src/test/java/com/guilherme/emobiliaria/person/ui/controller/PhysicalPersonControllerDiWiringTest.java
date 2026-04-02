package com.guilherme.emobiliaria.person.ui.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchService;
import com.guilherme.emobiliaria.person.domain.service.FakeAddressSearchService;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class PhysicalPersonControllerDiWiringTest {

  @Test
  @DisplayName("Should resolve physical person controllers from Guice")
  void shouldResolvePhysicalPersonControllersFromGuice() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(PhysicalPersonRepository.class).toInstance(new FakePhysicalPersonRepository());
        bind(AddressRepository.class).toInstance(new FakeAddressRepository());
        bind(AddressSearchService.class).toInstance(new FakeAddressSearchService());
        bind(NavigationService.class).in(Singleton.class);
      }

      @Provides
      @Singleton
      GuiceFxmlLoader provideGuiceFxmlLoader(Injector localInjector) {
        return new GuiceFxmlLoader(localInjector);
      }
    });

    assertDoesNotThrow(() -> injector.getInstance(PhysicalPersonListController.class));
    assertDoesNotThrow(() -> injector.getInstance(PhysicalPersonEditController.class));
    assertDoesNotThrow(() -> injector.getInstance(PhysicalPersonCreateController.class));
  }
}
