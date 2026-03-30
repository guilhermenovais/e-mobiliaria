package com.guilherme.emobiliaria.config.application.usecase;

import com.guilherme.emobiliaria.config.application.output.GetConfigOutput;
import com.guilherme.emobiliaria.config.domain.repository.ConfigRepository;
import jakarta.inject.Inject;

public class GetConfigInteractor {

  private final ConfigRepository configRepository;

  @Inject
  public GetConfigInteractor(ConfigRepository configRepository) {
    this.configRepository = configRepository;
  }

  public GetConfigOutput execute() {
    return new GetConfigOutput(configRepository.get());
  }
}
