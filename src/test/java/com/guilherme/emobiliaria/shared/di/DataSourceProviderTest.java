package com.guilherme.emobiliaria.shared.di;

import com.guilherme.emobiliaria.shared.persistence.AppDataPaths;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import javax.sql.DataSource;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class DataSourceProviderTest {

  private static final String MIGRATION_VERSION = "8";

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
  @DisplayName("Should repair Flyway checksum mismatches and still open the datasource")
  void shouldRepairChecksumMismatchAndOpenDatasource(@TempDir Path tempDir) throws SQLException {
    System.setProperty(AppDataPaths.APP_DATA_DIR_PROPERTY, tempDir.resolve("app-data").toString());

    long originalChecksum;
    try (HikariDataSource dataSource = new HikariDataSource(hikariConfig())) {
      Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load()
          .migrate();
      originalChecksum = readMigrationChecksum(dataSource);
      updateMigrationChecksum(dataSource, originalChecksum + 1);
    }

    DataSource repairedDataSource = new DataSourceProvider().get();
    try {
      assertNotNull(repairedDataSource);
      assertEquals(originalChecksum, readMigrationChecksum(repairedDataSource));
    } finally {
      if (repairedDataSource instanceof HikariDataSource hikariDataSource) {
        hikariDataSource.close();
      }
    }
  }

  private HikariConfig hikariConfig() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:file:" + AppDataPaths.h2DatabaseFilePath() + ";AUTO_SERVER=TRUE");
    config.setUsername("sa");
    config.setPassword("");
    config.setMaximumPoolSize(10);
    return config;
  }

  private long readMigrationChecksum(DataSource dataSource) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        // noinspection SqlDialectInspection
        PreparedStatement statement = connection.prepareStatement(
            "SELECT \"checksum\" FROM \"flyway_schema_history\" WHERE \"version\" = ?")) {
      statement.setString(1, MIGRATION_VERSION);
      try (ResultSet resultSet = statement.executeQuery()) {
        resultSet.next();
        return resultSet.getLong(1);
      }
    }
  }

  private void updateMigrationChecksum(DataSource dataSource, long checksum) throws SQLException {
    try (Connection connection = dataSource.getConnection();
        // noinspection SqlDialectInspection
        PreparedStatement statement = connection.prepareStatement(
            "UPDATE \"flyway_schema_history\" SET \"checksum\" = ? WHERE \"version\" = ?")) {
      statement.setLong(1, checksum);
      statement.setString(2, MIGRATION_VERSION);
      statement.executeUpdate();
    }
  }
}




