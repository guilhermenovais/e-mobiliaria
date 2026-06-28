package com.guilherme.emobiliaria.backup.application.output;

import com.guilherme.emobiliaria.backup.domain.entity.RemovableDrive;

import java.util.List;

public record DetectDrivesOutput(List<RemovableDrive> drives) {
}
