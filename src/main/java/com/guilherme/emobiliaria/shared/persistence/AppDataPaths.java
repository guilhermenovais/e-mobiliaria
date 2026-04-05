package com.guilherme.emobiliaria.shared.persistence;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Locale;

public final class AppDataPaths {

  public static final String APP_DATA_DIR_PROPERTY = "emobiliaria.app.data.dir";
  private static final String APP_DIRECTORY_NAME = "e-mobiliaria";

  private AppDataPaths() {
  }

  public static void initializeSystemProperties() {
    Path appDataDirectory = ensureDirectory(resolveAppDataDir());
    System.setProperty(APP_DATA_DIR_PROPERTY, appDataDirectory.toString());
  }

  public static Path resolveAppDataDir() {
    String configuredDirectory = System.getProperty(APP_DATA_DIR_PROPERTY);
    if (configuredDirectory != null && !configuredDirectory.isBlank()) {
      return Paths.get(configuredDirectory);
    }

    boolean isWindows = System.getProperty("os.name", "").toLowerCase(Locale.ROOT).contains("win");
    if (isWindows) {
      String localAppData = System.getenv("LOCALAPPDATA");
      if (localAppData != null && !localAppData.isBlank()) {
        return Paths.get(localAppData, APP_DIRECTORY_NAME);
      }
      return Paths.get(System.getProperty("user.home"), "AppData", "Local", APP_DIRECTORY_NAME);
    }

    return Paths.get(System.getProperty("user.home"), "." + APP_DIRECTORY_NAME);
  }

  public static Path logsDirectory() {
    return ensureDirectory(resolveAppDataDir().resolve("logs"));
  }

  public static Path databaseDirectory() {
    return ensureDirectory(resolveAppDataDir().resolve("database"));
  }

  public static String h2DatabaseFilePath() {
    Path databaseFile = databaseDirectory().resolve("emobiliaria").toAbsolutePath().normalize();
    return databaseFile.toString().replace('\\', '/');
  }

  private static Path ensureDirectory(Path path) {
    try {
      return Files.createDirectories(path);
    } catch (IOException ex) {
      throw new IllegalStateException("Failed to create application directory: " + path, ex);
    }
  }
}
