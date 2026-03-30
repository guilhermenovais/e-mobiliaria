package com.guilherme.emobiliaria.config.domain.repository;

import com.guilherme.emobiliaria.config.domain.entity.Config;

public interface ConfigRepository {

  Config get();

  Config set(Config config);
}
