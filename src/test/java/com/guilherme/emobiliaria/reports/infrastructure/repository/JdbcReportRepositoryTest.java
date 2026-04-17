package com.guilherme.emobiliaria.reports.infrastructure.repository;

import com.guilherme.emobiliaria.inflation.domain.repository.InflationIndexRepository;
import com.guilherme.emobiliaria.inflation.infrastructure.repository.JdbcInflationIndexRepository;
import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcReportRepositoryTest {

  private JdbcReportRepository repository;
  private DataSource dataSource;
  private int cpfCounter = 0;

  @BeforeEach
  void setUp() {
    String dbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    config.setPassword("");
    dataSource = new HikariDataSource(config);
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    InflationIndexRepository inflationIndexRepository =
        new JdbcInflationIndexRepository(dataSource);
    repository = new JdbcReportRepository(dataSource, inflationIndexRepository);
  }

  private String uniqueCpf() {
    return String.format("%011d", ++cpfCounter);
  }

  private long insertAddress(Connection conn) throws SQLException {
    String sql =
        "INSERT INTO addresses (cep, address, number, neighborhood, city, state) VALUES ('01001000', 'Rua Teste', '1', 'Centro', 'São Paulo', 'SP')";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertPhysicalPerson(Connection conn, long addressId) throws SQLException {
    String sql =
        "INSERT INTO physical_persons (name, nationality, civil_state, occupation, cpf, id_card_number, address_id) VALUES ('Pessoa Teste', 'Brasileiro', 'SINGLE', 'Engenheiro', ?, '1234567', ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, uniqueCpf());
      stmt.setLong(2, addressId);
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertProperty(Connection conn, long addressId) throws SQLException {
    String sql =
        "INSERT INTO properties (name, type, cemig, copasa, iptu, address_id) VALUES ('Imóvel Teste', 'Apartamento', 'C001', 'C002', 'I001', ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setLong(1, addressId);
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertPaymentAccount(Connection conn) throws SQLException {
    String sql =
        "INSERT INTO payment_accounts (bank, bank_branch, account_number) VALUES ('Banco', '0001', '12345-6')";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertContract(Connection conn, LocalDate startDate, String duration, long rent,
      long accountId, long propertyId, long landlordId) throws SQLException {
    String sql =
        "INSERT INTO contracts (start_date, duration, payment_day, rent, purpose, payment_account_id, property_id, landlord_id, landlord_type) VALUES (?, ?, 10, ?, 'Residencial', ?, ?, ?, 'PHYSICAL')";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setObject(1, startDate);
      stmt.setString(2, duration);
      stmt.setLong(3, rent);
      stmt.setLong(4, accountId);
      stmt.setLong(5, propertyId);
      stmt.setLong(6, landlordId);
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private void insertContractTenant(Connection conn, long contractId, long tenantId)
      throws SQLException {
    String sql =
        "INSERT INTO contract_tenants (contract_id, tenant_id, tenant_type) VALUES (?, ?, 'PHYSICAL')";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      stmt.setLong(2, tenantId);
      stmt.executeUpdate();
    }
  }

  private void insertReceipt(Connection conn, long contractId, LocalDate date, LocalDate start,
      LocalDate end) throws SQLException {
    String sql =
        "INSERT INTO receipts (date, interval_start, interval_end, discount, fine, contract_id) VALUES (?, ?, ?, 0, 0, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, date);
      stmt.setObject(2, start);
      stmt.setObject(3, end);
      stmt.setLong(4, contractId);
      stmt.executeUpdate();
    }
  }

  @Nested
  class LoadRentEvolutionData {

    @Test
    @DisplayName("When no contracts exist, should return empty data")
    void shouldReturnEmptyDataWhenNoContracts() {
      RentEvolutionData data = repository.loadRentEvolutionData();

      assertNotNull(data);
      assertTrue(data.months().isEmpty());
      assertTrue(data.monthlyTotalCents().isEmpty());
      assertTrue(data.propertyHistories().isEmpty());
    }

    @Test
    @DisplayName("When contracts exist, should return month range and property histories")
    void shouldReturnMonthRangeAndPropertyHistoriesWhenContractsExist() throws SQLException {
      try (Connection conn = dataSource.getConnection()) {
        long addressId = insertAddress(conn);
        long personId = insertPhysicalPerson(conn, addressId);
        long propertyId = insertProperty(conn, addressId);
        long accountId = insertPaymentAccount(conn);
        long contractId =
            insertContract(conn, LocalDate.of(2026, 1, 1), "P3M", 150000L, accountId, propertyId,
                personId);
        insertContractTenant(conn, contractId, personId);
        insertReceipt(conn, contractId, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31));
      }

      RentEvolutionData data = repository.loadRentEvolutionData();

      assertNotNull(data);
      assertFalse(data.months().isEmpty());
      assertTrue(data.months().contains(YearMonth.of(2026, 1)));
      assertEquals(1, data.propertyHistories().size());
    }

    @Test
    @DisplayName(
        "When receipts exist for a month, should include that month's total in monthly totals")
    void shouldIncludeReceiptTotalsInMonthlyTotals() throws SQLException {
      try (Connection conn = dataSource.getConnection()) {
        long addressId = insertAddress(conn);
        long personId = insertPhysicalPerson(conn, addressId);
        long propertyId = insertProperty(conn, addressId);
        long accountId = insertPaymentAccount(conn);
        long contractId =
            insertContract(conn, LocalDate.of(2026, 1, 1), "P12M", 150000L, accountId, propertyId,
                personId);
        insertContractTenant(conn, contractId, personId);
        insertReceipt(conn, contractId, LocalDate.of(2026, 1, 15), LocalDate.of(2026, 1, 1),
            LocalDate.of(2026, 1, 31));
      }

      RentEvolutionData data = repository.loadRentEvolutionData();

      int janIndex = data.months().indexOf(YearMonth.of(2026, 1));
      assertTrue(janIndex >= 0);
      assertEquals(150000L, data.monthlyTotalCents().get(janIndex));
    }
  }


  @Nested
  class LoadOccupationRateData {

    @Test
    @DisplayName("When no contracts exist, should return zero total properties and empty months")
    void shouldReturnEmptyDataWhenNoContracts() {
      OccupationRateData data = repository.loadOccupationRateData();

      assertNotNull(data);
      assertEquals(0, data.totalProperties());
      assertTrue(data.months().isEmpty());
      assertTrue(data.occupiedCounts().isEmpty());
    }

    @Test
    @DisplayName(
        "When a property has an active contract, should show it as occupied in the correct months")
    void shouldMarkPropertyAsOccupiedWhenActiveContract() throws SQLException {
      try (Connection conn = dataSource.getConnection()) {
        long addressId = insertAddress(conn);
        long personId = insertPhysicalPerson(conn, addressId);
        long propertyId = insertProperty(conn, addressId);
        long accountId = insertPaymentAccount(conn);
        long contractId =
            insertContract(conn, LocalDate.of(2026, 1, 1), "P3M", 100000L, accountId, propertyId,
                personId);
        insertContractTenant(conn, contractId, personId);
      }

      OccupationRateData data = repository.loadOccupationRateData();

      assertNotNull(data);
      assertEquals(1, data.totalProperties());
      assertFalse(data.months().isEmpty());
      assertTrue(data.months().contains(YearMonth.of(2026, 1)));
      assertEquals(1, data.propertyHistories().size());

      int janIndex = data.months().indexOf(YearMonth.of(2026, 1));
      assertTrue(data.occupiedCounts().get(janIndex) >= 1);
      assertTrue(data.propertyHistories().get(0).occupied().get(janIndex));
    }

    @Test
    @DisplayName(
        "When total properties are counted, should include all properties even without contracts")
    void shouldCountAllPropertiesInTotal() throws SQLException {
      try (Connection conn = dataSource.getConnection()) {
        long addressId = insertAddress(conn);
        insertProperty(conn, addressId);
        insertProperty(conn, addressId);
      }

      OccupationRateData data = repository.loadOccupationRateData();

      assertEquals(2, data.totalProperties());
    }
  }


  @Nested
  class Project {

    @Test
    @DisplayName("When from equals to, should return initial value unchanged")
    void shouldReturnInitialValueWhenFromEqualsTo() {
      long result =
          JdbcReportRepository.project(100000L, YearMonth.of(2026, 1), YearMonth.of(2026, 1),
              java.util.Map.of());

      assertEquals(100000L, result);
    }

    @Test
    @DisplayName("When rate is zero for all months, should return initial value unchanged")
    void shouldReturnInitialValueWhenRatesAreZero() {
      long result =
          JdbcReportRepository.project(100000L, YearMonth.of(2026, 1), YearMonth.of(2026, 3),
              java.util.Map.of(YearMonth.of(2026, 1), 0.0, YearMonth.of(2026, 2), 0.0));

      assertEquals(100000L, result);
    }

    @Test
    @DisplayName("When rate is applied for one month, should return compounded value")
    void shouldReturnCompoundedValueAfterApplyingRate() {
      long result =
          JdbcReportRepository.project(100000L, YearMonth.of(2026, 1), YearMonth.of(2026, 2),
              java.util.Map.of(YearMonth.of(2026, 1), 0.01));

      assertEquals(101000L, result);
    }
  }
}
