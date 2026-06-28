package com.guilherme.emobiliaria.backup.domain.service;

import com.guilherme.emobiliaria.shared.exception.RestoreException;

import java.nio.file.Path;

public interface RestoreService {

  void restore(Path backupFilePath) throws RestoreException;
}
