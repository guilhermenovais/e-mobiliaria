package com.guilherme.emobiliaria.config.infrastructure.repository;

import com.guilherme.emobiliaria.config.domain.entity.Config;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.infrastructure.repository.JdbcAddressRepository;
import com.guilherme.emobiliaria.person.infrastructure.repository.JdbcJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.infrastructure.repository.JdbcPhysicalPersonRepository;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class JdbcConfigRepositoryTest {

  private JdbcConfigRepository repository;
  private JdbcAddressRepository addressRepository;
  private JdbcPhysicalPersonRepository physicalPersonRepository;
  private JdbcJuridicalPersonRepository juridicalPersonRepository;

  @BeforeEach
  void setUp() {
    String dbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    config.setPassword("");
    DataSource dataSource = new HikariDataSource(config);
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate();
    repository = new JdbcConfigRepository(dataSource);
    addressRepository = new JdbcAddressRepository(dataSource);
    physicalPersonRepository = new JdbcPhysicalPersonRepository(dataSource);
    juridicalPersonRepository = new JdbcJuridicalPersonRepository(dataSource);
  }

  private Address createAddress() {
    return addressRepository.create(
        Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo", BrazilianState.SP));
  }

  private PhysicalPerson createPhysicalPerson() {
    return physicalPersonRepository.create(
        PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
            "529.982.247-25", "MG-1234567", createAddress()));
  }

  private JuridicalPerson createJuridicalPerson() {
    PhysicalPerson representative = createPhysicalPerson();
    return juridicalPersonRepository.create(
        JuridicalPerson.create("Empresa LTDA", "11.222.333/0001-81", representative,
            createAddress()));
  }

  @Nested
  class Get {

    @Test
    @DisplayName("When no defaultLandlord is set, should return config with null defaultLandlord")
    void shouldReturnConfigWithNullDefaultLandlordByDefault() {
      Config config = repository.get();

      assertEquals(1L, config.getId());
      assertNull(config.getDefaultLandlord());
    }
  }

  @Nested
  class Set {

    @Test
    @DisplayName("When set with a PhysicalPerson, should persist and return config with that person")
    void shouldPersistPhysicalPersonAsDefaultLandlord() {
      PhysicalPerson person = createPhysicalPerson();
      Config config = repository.get();
      config.setDefaultLandlord(person);

      Config updated = repository.set(config);
      Config fetched = repository.get();

      assertEquals(person.getId(), updated.getDefaultLandlord().getId());
      assertEquals(person.getId(), fetched.getDefaultLandlord().getId());
    }

    @Test
    @DisplayName("When set with a JuridicalPerson, should persist and return config with that person")
    void shouldPersistJuridicalPersonAsDefaultLandlord() {
      JuridicalPerson person = createJuridicalPerson();
      Config config = repository.get();
      config.setDefaultLandlord(person);

      Config updated = repository.set(config);
      Config fetched = repository.get();

      assertEquals(person.getId(), updated.getDefaultLandlord().getId());
      assertEquals(person.getId(), fetched.getDefaultLandlord().getId());
    }

    @Test
    @DisplayName("When set with null defaultLandlord, should persist and return config with null")
    void shouldPersistNullDefaultLandlord() {
      PhysicalPerson person = createPhysicalPerson();
      Config config = repository.get();
      config.setDefaultLandlord(person);
      repository.set(config);

      config.setDefaultLandlord(null);
      Config updated = repository.set(config);
      Config fetched = repository.get();

      assertNull(updated.getDefaultLandlord());
      assertNull(fetched.getDefaultLandlord());
    }
  }
}
