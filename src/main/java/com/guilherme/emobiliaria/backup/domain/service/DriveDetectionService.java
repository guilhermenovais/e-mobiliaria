package com.guilherme.emobiliaria.backup.domain.service;

import com.guilherme.emobiliaria.backup.domain.entity.RemovableDrive;

import java.util.List;

public interface DriveDetectionService {

  List<RemovableDrive> detectRemovableDrives();
}
