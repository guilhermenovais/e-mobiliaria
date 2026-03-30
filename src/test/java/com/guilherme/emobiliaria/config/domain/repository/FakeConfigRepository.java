package com.guilherme.emobiliaria.config.domain.repository;

import com.guilherme.emobiliaria.config.domain.entity.Config;
import com.guilherme.emobiliaria.shared.fake.FakeImplementation;

public class FakeConfigRepository extends FakeImplementation implements ConfigRepository {

  private Config stored = Config.restore(1L, null);

  @Override
  public Config get() {
    maybeFail();
    return stored;
  }

  @Override
  public Config set(Config config) {
    maybeFail();
    stored = config;
    return stored;
  }
}
