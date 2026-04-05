package com.guilherme.emobiliaria.shared.di;

import com.guilherme.emobiliaria.shared.persistence.AppDataPaths;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import jakarta.inject.Provider;
import org.flywaydb.core.Flyway;

import javax.sql.DataSource;

public class DataSourceProvider implements Provider<DataSource> {

  @Override
  public DataSource get() {
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:file:" + AppDataPaths.h2DatabaseFilePath() + ";AUTO_SERVER=TRUE");
    config.setUsername("sa");
    config.setPassword("");
    config.setMaximumPoolSize(10);
    HikariDataSource dataSource = new HikariDataSource(config);

    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate();

    return dataSource;
  }
}
