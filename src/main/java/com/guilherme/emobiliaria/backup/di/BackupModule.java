package com.guilherme.emobiliaria.backup.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.backup.domain.service.BackupCreationService;
import com.guilherme.emobiliaria.backup.domain.service.DriveDetectionService;
import com.guilherme.emobiliaria.backup.domain.service.RestoreService;
import com.guilherme.emobiliaria.backup.infrastructure.service.ProcessRestoreService;
import com.guilherme.emobiliaria.backup.infrastructure.service.WindowsDriveDetectionService;
import com.guilherme.emobiliaria.backup.infrastructure.service.ZipBackupCreationService;

public class BackupModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(DriveDetectionService.class).to(WindowsDriveDetectionService.class);
    bind(BackupCreationService.class).to(ZipBackupCreationService.class);
    bind(RestoreService.class).to(ProcessRestoreService.class);
  }
}
