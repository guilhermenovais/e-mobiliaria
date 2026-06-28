package com.guilherme.emobiliaria.backup.domain.service;

import com.guilherme.emobiliaria.shared.exception.BackupException;
import com.guilherme.emobiliaria.shared.exception.InsufficientSpaceException;

import java.nio.file.Path;

public interface BackupCreationService {

  void createBackup(Path targetDrivePath) throws InsufficientSpaceException, BackupException;
}
