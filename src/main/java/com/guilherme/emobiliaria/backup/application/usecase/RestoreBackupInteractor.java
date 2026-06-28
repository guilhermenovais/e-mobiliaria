package com.guilherme.emobiliaria.backup.application.usecase;

import com.guilherme.emobiliaria.backup.application.input.RestoreBackupInput;
import com.guilherme.emobiliaria.backup.domain.service.RestoreService;
import com.guilherme.emobiliaria.shared.exception.RestoreException;
import jakarta.inject.Inject;

public class RestoreBackupInteractor {

  private final RestoreService restoreService;

  @Inject
  public RestoreBackupInteractor(RestoreService restoreService) {
    this.restoreService = restoreService;
  }

  public void execute(RestoreBackupInput input) throws RestoreException {
    restoreService.restore(input.backupFilePath());
  }
}
