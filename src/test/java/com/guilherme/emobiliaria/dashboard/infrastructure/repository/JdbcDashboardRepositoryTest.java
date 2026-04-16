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

  @BeforeEach
  void setUp() {
    String dbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    config.setPassword("");
    dataSource = new HikariDataSource(config);
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate();
    repository = new JdbcDashboardRepository(dataSource);
  }

  // Inserts minimal required data and returns the contract id.
  // startDate drives start_day; duration is P120M to keep the contract always active.
  private long createContract(LocalDate startDate, int paymentDay) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      long addressId = insertAddress(conn);
      long personId = insertPhysicalPerson(conn, addressId, uniqueCpf());
      long propertyId = insertProperty(conn, addressId);
      long accountId = insertPaymentAccount(conn);
      long contractId = insertContract(conn, startDate, paymentDay, accountId, propertyId, personId);
      insertContractTenant(conn, contractId, personId);
      return contractId;
    }
  }

  private void insertReceipt(long contractId, LocalDate start, LocalDate end) throws SQLException {
    try (Connection conn = dataSource.getConnection()) {
      String sql = "INSERT INTO receipts (date, interval_start, interval_end, discount, fine, contract_id) VALUES (?, ?, ?, 0, 0, ?)";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setObject(1, LocalDate.now());
        stmt.setObject(2, start);
        stmt.setObject(3, end);
        stmt.setLong(4, contractId);
        stmt.executeUpdate();
      }
    }
  }

  private long insertAddress(Connection conn) throws SQLException {
    String sql = "INSERT INTO addresses (cep, address, number, neighborhood, city, state) VALUES ('01001000', 'Rua Teste', '1', 'Centro', 'São Paulo', 'SP')";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertPhysicalPerson(Connection conn, long addressId, String cpf) throws SQLException {
    String sql = "INSERT INTO physical_persons (name, nationality, civil_state, occupation, cpf, id_card_number, address_id) VALUES ('Tenant Name', 'Brasileiro', 'SINGLE', 'Engenheiro', ?, '1234567', ?)";
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
    String sql = "INSERT INTO properties (name, type, cemig, copasa, iptu, address_id) VALUES ('Imóvel Teste', 'Apartamento', 'C001', 'C002', 'I001', ?)";
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
    String sql = "INSERT INTO payment_accounts (bank, bank_branch, account_number) VALUES ('Banco Teste', '0001', '12345-6')";
    try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertContract(Connection conn, LocalDate startDate, int paymentDay,
      long accountId, long propertyId, long landlordId) throws SQLException {
    String sql = "INSERT INTO contracts (start_date, duration, payment_day, rent, purpose, payment_account_id, property_id, landlord_id, landlord_type) VALUES (?, 'P120M', ?, 100000, 'Residencial', ?, ?, ?, 'PHYSICAL')";
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

  private void insertContractTenant(Connection conn, long contractId, long tenantId) throws SQLException {
    String sql = "INSERT INTO contract_tenants (contract_id, tenant_id, tenant_type) VALUES (?, ?, 'PHYSICAL')";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      stmt.setLong(2, tenantId);
      stmt.executeUpdate();
    }
  }

  private int cpfCounter = 0;

  private String uniqueCpf() {
    return String.format("%011d", ++cpfCounter);
  }

  @Nested
  class ComputePeriodStart {

    @Test
    @DisplayName("When today's day equals start day, should return current month period start")
    void shouldReturnCurrentMonthWhenTodayDayEqualsStartDay() {
      LocalDate today = LocalDate.of(2026, 4, 15);
      LocalDate result = JdbcDashboardRepository.computePeriodStart(today, 15);
      assertEquals(LocalDate.of(2026, 4, 15), result);
    }

    @Test
    @DisplayName("When today's day is after start day, should return current month period start")
    void shouldReturnCurrentMonthWhenTodayDayAfterStartDay() {
      LocalDate today = LocalDate.of(2026, 4, 16);
      LocalDate result = JdbcDashboardRepository.computePeriodStart(today, 10);
      assertEquals(LocalDate.of(2026, 4, 10), result);
    }

    @Test
    @DisplayName("When today's day is before start day, should return previous month period start")
    void shouldReturnPreviousMonthWhenTodayDayBeforeStartDay() {
      LocalDate today = LocalDate.of(2026, 4, 16);
      LocalDate result = JdbcDashboardRepository.computePeriodStart(today, 20);
      assertEquals(LocalDate.of(2026, 3, 20), result);
    }

    @Test
    @DisplayName("When start day exceeds days in current month, should clamp to last day of month")
    void shouldClampStartDayToMonthLength() {
      LocalDate today = LocalDate.of(2026, 4, 30);
      LocalDate result = JdbcDashboardRepository.computePeriodStart(today, 31);
      assertEquals(LocalDate.of(2026, 4, 30), result);
    }
  }

  @Nested
  class ComputePeriodEnd {

    @Test
    @DisplayName("When period starts on 15th, should end on 14th of the next month")
    void shouldEndOnDayBeforeNextPeriodStart() {
      LocalDate periodStart = LocalDate.of(2026, 4, 15);
      assertEquals(LocalDate.of(2026, 5, 14), JdbcDashboardRepository.computePeriodEnd(periodStart));
    }
  }

  @Nested
  class ComputeDeadline {

    @Test
    @DisplayName("When payment day is within month, should return that day in period's month")
    void shouldReturnPaymentDayInPeriodStartMonth() {
      LocalDate periodStart = LocalDate.of(2026, 4, 15);
      assertEquals(LocalDate.of(2026, 4, 10), JdbcDashboardRepository.computeDeadline(periodStart, 10));
    }

    @Test
    @DisplayName("When payment day exceeds days in month, should clamp to last day of month")
    void shouldClampPaymentDayToMonthLength() {
      LocalDate periodStart = LocalDate.of(2026, 2, 1);
      assertEquals(LocalDate.of(2026, 2, 28), JdbcDashboardRepository.computeDeadline(periodStart, 31));
    }
  }

  @Nested
  class LoadUnpaidRents {

    // today = 2026-04-16, start_day = 10 → period: Apr 10 – May 9
    // payment_day = 5 → deadline = Apr 5 (overdue)
    @Test
    @DisplayName("When deadline is overdue and no receipt exists, should include entry")
    void shouldIncludeWhenDeadlineIsOverdue() throws SQLException {
      LocalDate today = LocalDate.of(2026, 4, 16);
      createContract(LocalDate.of(2024, 1, 10), 5); // start_day=10, payment_day=5 → deadline Apr 5

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertEquals(1, results.size());
      assertEquals(LocalDate.of(2026, 4, 5), results.getFirst().dueDate());
    }

    // today = 2026-04-16, start_day = 10 → period: Apr 10 – May 9
    // payment_day = 19 → deadline = Apr 19 (3 days away, < 5)
    @Test
    @DisplayName("When deadline is within 4 days, should include entry")
    void shouldIncludeWhenDeadlineIsWithinFourDays() throws SQLException {
      LocalDate today = LocalDate.of(2026, 4, 16);
      createContract(LocalDate.of(2024, 1, 10), 19); // deadline Apr 19 = 3 days away

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertEquals(1, results.size());
      assertEquals(LocalDate.of(2026, 4, 19), results.getFirst().dueDate());
    }

    // today = 2026-04-16, deadline = Apr 20 (4 days away — boundary: still < 5)
    @Test
    @DisplayName("When deadline is exactly 4 days away, should include entry")
    void shouldIncludeWhenDeadlineIsExactlyFourDaysAway() throws SQLException {
      LocalDate today = LocalDate.of(2026, 4, 16);
      createContract(LocalDate.of(2024, 1, 10), 20); // deadline Apr 20 = 4 days away

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertEquals(1, results.size());
      assertEquals(LocalDate.of(2026, 4, 20), results.getFirst().dueDate());
    }

    // today = 2026-04-16, deadline = Apr 21 (5 days away — not in alert window)
    @Test
    @DisplayName("When deadline is 5 or more days away, should not include entry")
    void shouldExcludeWhenDeadlineIsFiveOrMoreDaysAway() throws SQLException {
      LocalDate today = LocalDate.of(2026, 4, 16);
      createContract(LocalDate.of(2024, 1, 10), 21); // deadline Apr 21 = 5 days away

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertTrue(results.isEmpty());
    }

    // today = 2026-04-16, start_day = 10 → period: Apr 10 – May 9
    // Receipt fully covers Apr 10 – May 9 → paid
    @Test
    @DisplayName("When receipt fully covers the period, should not include entry")
    void shouldExcludeWhenReceiptFullyCoversPeriod() throws SQLException {
      LocalDate today = LocalDate.of(2026, 4, 16);
      long contractId = createContract(LocalDate.of(2024, 1, 10), 5);
      insertReceipt(contractId, LocalDate.of(2026, 4, 1), LocalDate.of(2026, 5, 31));

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertTrue(results.isEmpty());
    }

    // Receipt covers only part of the period (ends before period_end)
    @Test
    @DisplayName("When receipt only partially covers the period, should include entry")
    void shouldIncludeWhenReceiptOnlyPartiallyCoversPeriod() throws SQLException {
      LocalDate today = LocalDate.of(2026, 4, 16);
      long contractId = createContract(LocalDate.of(2024, 1, 10), 5);
      // period: Apr 10 – May 9; receipt ends May 1 (before May 9)
      insertReceipt(contractId, LocalDate.of(2026, 4, 10), LocalDate.of(2026, 5, 1));

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertEquals(1, results.size());
    }

    // today = 2026-04-16, start_day = 20 → period starts in previous month (Mar 20 – Apr 19)
    // payment_day = 5 → deadline = Mar 5 (clearly overdue)
    @Test
    @DisplayName("When start day is after today's day, should use previous month period")
    void shouldUsePreviousMonthPeriodWhenStartDayIsAfterTodayDay() throws SQLException {
      LocalDate today = LocalDate.of(2026, 4, 16);
      createContract(LocalDate.of(2024, 1, 20), 5); // start_day=20, payment_day=5 → deadline Mar 5

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertEquals(1, results.size());
      assertEquals(LocalDate.of(2026, 3, 5), results.getFirst().dueDate());
    }

    // Two overdue contracts with different deadlines → sorted ascending by deadline
    @Test
    @DisplayName("When multiple contracts are overdue, should return them sorted by deadline ascending")
    void shouldReturnResultsSortedByDeadlineAscending() throws SQLException {
      LocalDate today = LocalDate.of(2026, 4, 16);
      // start_day=10, payment_day=8 → deadline Apr 8
      createContract(LocalDate.of(2024, 1, 10), 8);
      // start_day=10, payment_day=3 → deadline Apr 3
      createContract(LocalDate.of(2024, 2, 10), 3);

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertEquals(2, results.size());
      assertEquals(LocalDate.of(2026, 4, 3), results.get(0).dueDate());
      assertEquals(LocalDate.of(2026, 4, 8), results.get(1).dueDate());
    }

    @Test
    @DisplayName("When no active contracts exist, should return empty list")
    void shouldReturnEmptyListWhenNoContracts() {
      LocalDate today = LocalDate.of(2026, 4, 16);

      List<UnpaidRentEntry> results = repository.load(today).unpaidRents();

      assertTrue(results.isEmpty());
    }
  }
}
