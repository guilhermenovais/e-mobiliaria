package com.guilherme.emobiliaria.backup.application.output;

import com.guilherme.emobiliaria.backup.domain.entity.BackupFile;

import java.util.List;

public record ListDriveBackupsOutput(List<BackupFile> backups) {
}
