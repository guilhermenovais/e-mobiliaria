package com.guilherme.emobiliaria.dashboard.infrastructure.repository;

import com.guilherme.emobiliaria.dashboard.domain.entity.UnpaidRentEntry;
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
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcDashboardRepositoryTest {

  private JdbcDashboardRepository repository;
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
    repository = new JdbcDashboardRepository(dataSource,
        new com.guilherme.emobiliaria.contract.domain.service.PaymentDueDateService());
  }

  // Inserts minimal required data and returns the contract id.
  // startDate drives start_day; duration is P120M to keep the contract always active.
  private long createContract(LocalDate startDate, int paymentDay) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      long addressId = insertAddress(conn);
      long personId = insertPhysicalPerson(conn, addressId, uniqueCpf());
      long propertyId = insertProperty(conn, addressId);
      long accountId = insertPaymentAccount(conn);
      long contractId =
          insertContract(conn, startDate, paymentDay, accountId, propertyId, personId);
      insertContractTenant(conn, contractId, personId);
      return contractId;
    }
  }

  private void insertReceipt(long contractId, LocalDate paymentDueDate) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      String sql =
          "INSERT INTO receipts (date, payment_due_date, interval_start, interval_end, discount, fine, contract_id) VALUES (?, ?, ?, ?, 0, 0, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setObject(1, LocalDate.now());
        stmt.setObject(2, paymentDueDate);
        stmt.setObject(3, paymentDueDate);
        stmt.setObject(4, paymentDueDate.plusMonths(1).minusDays(1));
        stmt.setLong(5, contractId);
        stmt.executeUpdate();
      }
    }
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

  private long insertPhysicalPerson(Connection conn, long addressId, String cpf)
      throws SQLException {
    String sql =
        "INSERT INTO physical_persons (name, nationality, civil_state, occupation, cpf, id_card_number, address_id) VALUES ('Tenant Name', 'Brasileiro', 'SINGLE', 'Engenheiro', ?, '1234567', ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, cpf);
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
        "INSERT INTO payment_accounts (bank, bank_branch, account_number) VALUES ('Banco Teste', '0001', '12345-6')";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertContract(Connection conn, LocalDate startDate, int paymentDay, long accountId,
      long propertyId, long landlordId) throws SQLException {
    String sql =
        "INSERT INTO contracts (start_date, duration, payment_day, rent, purpose, payment_account_id, property_id, landlord_id, landlord_type) VALUES (?, 'P120M', ?, 100000, 'Residencial', ?, ?, ?, 'PHYSICAL')";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setObject(1, startDate);
      stmt.setInt(2, paymentDay);
      stmt.setLong(3, accountId);
      stmt.setLong(4, propertyId);
      stmt.setLong(5, landlordId);
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

  private String uniqueCpf() {
    return String.format("%011d", ++cpfCounter);
  }

  @Nested
  class LoadUnpaidRents {

    @Test
    @DisplayName("Active contract with 3 due dates and 1 receipt shows 2 pending")
    void shouldShowTwoPendingWhenOneOfThreeDatesReceipted() throws SQLException {
      // Contract: starts 2026-02-01, paymentDay=15
      // Today = 2026-04-15 → due dates: Feb 15, Mar 15, Apr 15
      long contractId = createContract(LocalDate.of(2026, 2, 1), 15);
      insertReceipt(contractId, LocalDate.of(2026, 2, 15));

      List<UnpaidRentEntry> results = repository.load(LocalDate.of(2026, 4, 15)).unpaidRents();

      assertEquals(2, results.size());
      assertEquals(LocalDate.of(2026, 3, 15), results.get(0).dueDate());
      assertEquals(LocalDate.of(2026, 4, 15), results.get(1).dueDate());
    }

    @Test
    @DisplayName("All due dates receipted shows 0 pending")
    void shouldShowZeroPendingWhenAllDatesReceipted() throws SQLException {
      long contractId = createContract(LocalDate.of(2026, 2, 1), 15);
      insertReceipt(contractId, LocalDate.of(2026, 2, 15));
      insertReceipt(contractId, LocalDate.of(2026, 3, 15));
      insertReceipt(contractId, LocalDate.of(2026, 4, 15));

      List<UnpaidRentEntry> results = repository.load(LocalDate.of(2026, 4, 15)).unpaidRents();

      assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Contract starting next month shows 0 pending")
    void shouldShowZeroPendingWhenContractStartsInFuture() throws SQLException {
      // Contract starts 2026-06-01, today is 2026-05-15 → first due date is Jun 15, future
      createContract(LocalDate.of(2026, 6, 1), 15);

      List<UnpaidRentEntry> results = repository.load(LocalDate.of(2026, 5, 15)).unpaidRents();

      assertTrue(results.isEmpty());
    }

    @Test
    @DisplayName("Results are sorted ascending by due date")
    void shouldReturnResultsSortedByDueDateAscending() throws SQLException {
      long contractId = createContract(LocalDate.of(2026, 2, 1), 15);

      List<UnpaidRentEntry> results = repository.load(LocalDate.of(2026, 4, 15)).unpaidRents();

      assertEquals(3, results.size());
      assertEquals(LocalDate.of(2026, 2, 15), results.get(0).dueDate());
      assertEquals(LocalDate.of(2026, 3, 15), results.get(1).dueDate());
      assertEquals(LocalDate.of(2026, 4, 15), results.get(2).dueDate());
    }

    @Test
    @DisplayName("When no active contracts exist, should return empty list")
    void shouldReturnEmptyListWhenNoContracts() {
      List<UnpaidRentEntry> results = repository.load(LocalDate.of(2026, 4, 16)).unpaidRents();
      assertTrue(results.isEmpty());
    }
  }
}
