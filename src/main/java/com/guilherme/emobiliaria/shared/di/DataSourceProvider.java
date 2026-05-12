package com.guilherme.emobiliaria.shared.di;

import com.guilherme.emobiliaria.shared.persistence.AppDataPaths;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Provider;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.exception.FlywayValidateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.sql.DataSource;

public class DataSourceProvider implements Provider<DataSource> {

  private static final Logger log = LoggerFactory.getLogger(DataSourceProvider.class);

  @Override
  public DataSource get() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:file:" + AppDataPaths.h2DatabaseFilePath() + ";AUTO_SERVER=TRUE");
    config.setUsername("sa");
    config.setPassword("");
    config.setMaximumPoolSize(10);
    HikariDataSource dataSource = new HikariDataSource(config);

    try {
      migrateSchema(dataSource);
      return dataSource;
    } catch (RuntimeException exception) {
      dataSource.close();
      throw exception;
    }
  }

  private void migrateSchema(DataSource dataSource) {
    Flyway flyway =
        Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load();

    try {
      flyway.migrate();
    } catch (FlywayValidateException exception) {
      log.warn("Flyway validation failed; repairing schema history and retrying migration.",
          exception);
      flyway.repair();
      flyway.migrate();
    }
  }
}
