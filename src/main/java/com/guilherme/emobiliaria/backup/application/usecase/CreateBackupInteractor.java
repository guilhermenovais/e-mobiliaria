package com.guilherme.emobiliaria.backup.application.usecase;

import com.guilherme.emobiliaria.backup.application.input.CreateBackupInput;
import com.guilherme.emobiliaria.backup.application.output.CreateBackupOutput;
import com.guilherme.emobiliaria.backup.domain.service.BackupCreationService;
import com.guilherme.emobiliaria.shared.exception.BackupException;
import com.guilherme.emobiliaria.shared.exception.InsufficientSpaceException;
import jakarta.inject.Inject;

public class CreateBackupInteractor {

  private final BackupCreationService backupCreationService;

  @Inject
  public CreateBackupInteractor(BackupCreationService backupCreationService) {
    this.backupCreationService = backupCreationService;
  }

  public CreateBackupOutput execute(CreateBackupInput input)
      throws InsufficientSpaceException, BackupException {
    backupCreationService.createBackup(input.drivePath());
    return new CreateBackupOutput();
  }
}
