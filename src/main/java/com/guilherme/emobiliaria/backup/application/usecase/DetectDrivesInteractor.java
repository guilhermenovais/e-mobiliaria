package com.guilherme.emobiliaria.backup.application.usecase;

import com.guilherme.emobiliaria.backup.application.output.DetectDrivesOutput;
import com.guilherme.emobiliaria.backup.domain.entity.RemovableDrive;
import com.guilherme.emobiliaria.backup.domain.service.DriveDetectionService;
import com.guilherme.emobiliaria.shared.exception.NoDrivesFoundException;
import jakarta.inject.Inject;

import java.util.List;

public class DetectDrivesInteractor {

  private final DriveDetectionService driveDetectionService;

  @Inject
  public DetectDrivesInteractor(DriveDetectionService driveDetectionService) {
    this.driveDetectionService = driveDetectionService;
  }

  public DetectDrivesOutput execute() {
    List<RemovableDrive> drives = driveDetectionService.detectRemovableDrives();
    if (drives.isEmpty()) {
      throw new NoDrivesFoundException();
    }
    return new DetectDrivesOutput(drives);
  }
}
