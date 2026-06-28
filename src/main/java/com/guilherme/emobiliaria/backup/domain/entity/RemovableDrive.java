package com.guilherme.emobiliaria.backup.domain.entity;

import java.nio.file.Path;

public record RemovableDrive(String label, Path path) {

  @Override
  public String toString() {
    return label;
  }
}
