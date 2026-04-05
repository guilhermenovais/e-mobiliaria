package com.guilherme.emobiliaria.shared.persistence;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppDataPathsTest {

  private final String originalAppDataDirProperty =
      System.getProperty(AppDataPaths.APP_DATA_DIR_PROPERTY);

  @AfterEach
  void restoreSystemProperty() {
    if (originalAppDataDirProperty == null) {
      System.clearProperty(AppDataPaths.APP_DATA_DIR_PROPERTY);
    } else {
      System.setProperty(AppDataPaths.APP_DATA_DIR_PROPERTY, originalAppDataDirProperty);
    }
  }

  @Test
  @DisplayName("Should use configured app data directory when system property is set")
  void shouldUseConfiguredAppDataDirectory(@TempDir Path tempDir) {
    Path configuredDirectory = tempDir.resolve("custom-app-data");
    System.setProperty(AppDataPaths.APP_DATA_DIR_PROPERTY, configuredDirectory.toString());

    Path resolved = AppDataPaths.resolveAppDataDir();

    assertEquals(configuredDirectory, resolved);
  }

  @Test
  @DisplayName("Should create app data and set system property during initialization")
  void shouldCreateDirectoryAndSetProperty(@TempDir Path tempDir) {
    Path configuredDirectory = tempDir.resolve("initialized-app-data");
    System.setProperty(AppDataPaths.APP_DATA_DIR_PROPERTY, configuredDirectory.toString());

    AppDataPaths.initializeSystemProperties();

    assertEquals(configuredDirectory.toString(),
        System.getProperty(AppDataPaths.APP_DATA_DIR_PROPERTY));
    assertTrue(Files.isDirectory(configuredDirectory));
  }

  @Test
  @DisplayName("Should create database directory and return normalized H2 path")
  void shouldCreateDatabaseDirectoryAndReturnH2Path(@TempDir Path tempDir) {
    Path configuredDirectory = tempDir.resolve("db-app-data");
    System.setProperty(AppDataPaths.APP_DATA_DIR_PROPERTY, configuredDirectory.toString());

    String databaseFilePath = AppDataPaths.h2DatabaseFilePath();
    Path databaseDirectory = configuredDirectory.resolve("database");

    assertTrue(Files.isDirectory(databaseDirectory));
    assertTrue(databaseFilePath.endsWith("/database/emobiliaria"));
  }
}
