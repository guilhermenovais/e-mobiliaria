package com.guilherme.emobiliaria.backup.application.input;

import java.nio.file.Path;

public record RestoreBackupInput(Path backupFilePath) {
}
