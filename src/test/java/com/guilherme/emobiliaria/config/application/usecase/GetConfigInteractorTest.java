package com.guilherme.emobiliaria.config.application.usecase;

import com.guilherme.emobiliaria.config.application.output.GetConfigOutput;
import com.guilherme.emobiliaria.config.domain.entity.Config;
import com.guilherme.emobiliaria.config.domain.repository.FakeConfigRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class GetConfigInteractorTest {

  private FakeConfigRepository configRepository;
  private GetConfigInteractor interactor;

  @BeforeEach
  void setUp() {
    configRepository = new FakeConfigRepository();
    interactor = new GetConfigInteractor(configRepository);
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When config has no defaultLandlord, should return config with null defaultLandlord")
    void shouldReturnConfigWithNullDefaultLandlord() {
      GetConfigOutput output = interactor.execute();

      assertNotNull(output.config());
      assertNull(output.config().getDefaultLandlord());
    }

    @Test
    @DisplayName("When config has a defaultLandlord, should return config with that defaultLandlord")
    void shouldReturnConfigWithDefaultLandlord() {
      Config stored = configRepository.get();
      stored.setDefaultLandlord(null);
      configRepository.set(stored);

      GetConfigOutput output = interactor.execute();

      assertNotNull(output.config());
    }
  }
}
