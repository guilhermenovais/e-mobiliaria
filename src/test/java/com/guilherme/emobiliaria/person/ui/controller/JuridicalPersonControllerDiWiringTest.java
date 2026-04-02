package com.guilherme.emobiliaria.person.ui.controller;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchService;
import com.guilherme.emobiliaria.person.domain.service.CnpjValidationService;
import com.guilherme.emobiliaria.person.domain.service.CpfValidationService;
import com.guilherme.emobiliaria.person.domain.service.FakeAddressSearchService;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import javafx.application.Platform;
import javafx.scene.Node;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JuridicalPersonControllerDiWiringTest {

  @BeforeAll
  static void startFx() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    try {
      Platform.startup(latch::countDown);
    } catch (IllegalStateException ignored) {
      latch.countDown();
    }
    assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX platform did not start");
  }

  @Test
  @DisplayName("Should resolve juridical person controllers from Guice")
  void shouldResolveJuridicalPersonControllersFromGuice() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(PhysicalPersonRepository.class).toInstance(new FakePhysicalPersonRepository());
        bind(JuridicalPersonRepository.class).toInstance(new FakeJuridicalPersonRepository());
        bind(AddressRepository.class).toInstance(new FakeAddressRepository());
        bind(AddressSearchService.class).toInstance(new FakeAddressSearchService());
        bind(CpfValidationService.class).in(Singleton.class);
        bind(CnpjValidationService.class).in(Singleton.class);
        bind(NavigationService.class).in(Singleton.class);
      }

      @Provides
      @Singleton
      GuiceFxmlLoader provideGuiceFxmlLoader(Injector localInjector) {
        return new GuiceFxmlLoader(localInjector);
      }
    });

    assertDoesNotThrow(() -> injector.getInstance(JuridicalPersonListController.class));
    assertDoesNotThrow(() -> injector.getInstance(JuridicalPersonController.class));
  }

  @Test
  @DisplayName("Should build juridical person view with Guice controller instance")
  void shouldBuildJuridicalPersonViewWithGuiceControllerInstance() {
    Injector injector = Guice.createInjector(new AbstractModule() {
      @Override
      protected void configure() {
        bind(PhysicalPersonRepository.class).toInstance(new FakePhysicalPersonRepository());
        bind(JuridicalPersonRepository.class).toInstance(new FakeJuridicalPersonRepository());
        bind(AddressRepository.class).toInstance(new FakeAddressRepository());
        bind(AddressSearchService.class).toInstance(new FakeAddressSearchService());
        bind(CpfValidationService.class).in(Singleton.class);
        bind(CnpjValidationService.class).in(Singleton.class);
        bind(NavigationService.class).in(Singleton.class);
      }

      @Provides
      @Singleton
      GuiceFxmlLoader provideGuiceFxmlLoader(Injector localInjector) {
        return new GuiceFxmlLoader(localInjector);
      }
    });

    JuridicalPersonController controller = injector.getInstance(JuridicalPersonController.class);
    Node view = assertDoesNotThrow(controller::buildView);
    assertNotNull(view);
  }
}
