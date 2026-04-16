package com.guilherme.emobiliaria.dashboard.infrastructure.repository;

import com.guilherme.emobiliaria.dashboard.domain.entity.DashboardData;
import com.guilherme.emobiliaria.dashboard.domain.entity.ExpiringContractEntry;
import com.guilherme.emobiliaria.dashboard.domain.entity.TopRentEntry;
import com.guilherme.emobiliaria.dashboard.domain.entity.UnpaidRentEntry;
import com.guilherme.emobiliaria.dashboard.domain.entity.UrgencyLevel;
import com.guilherme.emobiliaria.dashboard.domain.entity.VacantPropertyEntry;
import com.guilherme.emobiliaria.dashboard.domain.repository.DashboardRepository;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class JdbcDashboardRepository implements DashboardRepository {

  private static final int EXPIRING_HORIZON_DAYS = 120;
  private static final int URGENCY_URGENT_DAYS = 30;
  private static final int URGENCY_WARNING_DAYS = 60;
  private static final int UNPAID_ALERT_DAYS = 5;

  private static final String ACTIVE_CONTRACTS_CTE = """
      WITH latest AS (
        SELECT id,
               MAX(id) OVER (PARTITION BY property_id) AS latest_id,
               DATEADD('MONTH',
                 CAST(SUBSTRING(duration, 2, LENGTH(duration) - 2) AS INT),
                 start_date) AS end_date
        FROM contracts
      ),
      active_contracts AS (
        SELECT l.id
        FROM latest l
        WHERE l.id = l.latest_id
          AND l.end_date >= CURRENT_DATE
      )
      """;

  private final DataSource dataSource;

  @Inject
  public JdbcDashboardRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public DashboardData load(LocalDate today) {
    try (Connection conn = dataSource.getConnection()) {
      int[] revenue = loadRevenueSummary(conn);
      List<TopRentEntry> topRents = loadTopRents(conn);
      List<UnpaidRentEntry> unpaidRents = loadUnpaidRents(conn, today);
      List<VacantPropertyEntry> vacantProperties = loadVacantProperties(conn);
      List<ExpiringContractEntry> expiringContracts = loadExpiringContracts(conn, today);
      return new DashboardData(revenue[0], revenue[1], topRents, unpaidRents, vacantProperties, expiringContracts);
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Dashboard.LOAD_ERROR, e);
    }
  }

  private int[] loadRevenueSummary(Connection conn) throws SQLException {
    String sql = ACTIVE_CONTRACTS_CTE + """
        SELECT COALESCE(SUM(c.rent), 0) AS total_revenue, COUNT(*) AS active_count
        FROM active_contracts ac
        JOIN contracts c ON c.id = ac.id
        """;
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      rs.next();
      return new int[]{rs.getInt("total_revenue"), rs.getInt("active_count")};
    }
  }

  private List<TopRentEntry> loadTopRents(Connection conn) throws SQLException {
    String sql = ACTIVE_CONTRACTS_CTE + """
        SELECT p.name AS property_name,
               COALESCE(pp.name, jp.corporate_name) AS tenant_name,
               c.rent
        FROM active_contracts ac
        JOIN contracts c ON c.id = ac.id
        JOIN properties p ON p.id = c.property_id
        LEFT JOIN (
          SELECT ct.contract_id,
                 ct.tenant_id,
                 ct.tenant_type,
                 ROW_NUMBER() OVER (PARTITION BY ct.contract_id ORDER BY ct.id) AS rn
          FROM contract_tenants ct
        ) ct ON ct.contract_id = c.id AND ct.rn = 1
        LEFT JOIN physical_persons pp ON ct.tenant_type = 'PHYSICAL' AND pp.id = ct.tenant_id
        LEFT JOIN juridical_persons jp ON ct.tenant_type = 'JURIDICAL' AND jp.id = ct.tenant_id
        ORDER BY c.rent DESC
        LIMIT 3
        """;
    List<TopRentEntry> results = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      int rank = 1;
      while (rs.next()) {
        results.add(mapTopRent(rs, rank++));
      }
    }
    return results;
  }

  private List<UnpaidRentEntry> loadUnpaidRents(Connection conn, LocalDate today) throws SQLException {
    String fetchSql = ACTIVE_CONTRACTS_CTE + """
        SELECT c.id, c.start_date, c.payment_day, c.rent,
               p.name AS property_name,
               COALESCE(pp.name, jp.corporate_name) AS tenant_name
        FROM active_contracts ac
        JOIN contracts c ON c.id = ac.id
        JOIN properties p ON p.id = c.property_id
        LEFT JOIN (
          SELECT ct.contract_id,
                 ct.tenant_id,
                 ct.tenant_type,
                 ROW_NUMBER() OVER (PARTITION BY ct.contract_id ORDER BY ct.id) AS rn
          FROM contract_tenants ct
        ) ct ON ct.contract_id = c.id AND ct.rn = 1
        LEFT JOIN physical_persons pp ON ct.tenant_type = 'PHYSICAL' AND pp.id = ct.tenant_id
        LEFT JOIN juridical_persons jp ON ct.tenant_type = 'JURIDICAL' AND jp.id = ct.tenant_id
        """;
    List<ContractCandidate> candidates = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(fetchSql);
         ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        candidates.add(new ContractCandidate(
            rs.getLong("id"),
            rs.getDate("start_date").toLocalDate(),
            rs.getInt("payment_day"),
            rs.getInt("rent"),
            rs.getString("property_name"),
            rs.getString("tenant_name")
        ));
      }
    }

    LocalDate alertCutoff = today.plusDays(UNPAID_ALERT_DAYS - 1);
    String receiptSql = """
        SELECT 1 FROM receipts
        WHERE contract_id = ?
          AND interval_start <= ?
          AND interval_end >= ?
        """;
    List<UnpaidRentEntry> results = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(receiptSql)) {
      for (ContractCandidate c : candidates) {
        LocalDate periodStart = computePeriodStart(today, c.startDate().getDayOfMonth());
        LocalDate periodEnd = computePeriodEnd(periodStart);
        LocalDate deadline = computeDeadline(periodStart, c.paymentDay());

        if (deadline.isAfter(alertCutoff)) continue;

        stmt.setLong(1, c.id());
        stmt.setDate(2, Date.valueOf(periodStart));
        stmt.setDate(3, Date.valueOf(periodEnd));
        try (ResultSet rs = stmt.executeQuery()) {
          if (!rs.next()) {
            results.add(new UnpaidRentEntry(c.propertyName(), c.tenantName(), c.rent(), deadline));
          }
        }
      }
    }

    results.sort(Comparator.comparing(UnpaidRentEntry::dueDate));
    return results;
  }

  static LocalDate computePeriodStart(LocalDate today, int startDay) {
    int clampedToCurrentMonth = Math.min(startDay, today.lengthOfMonth());
    if (today.getDayOfMonth() >= clampedToCurrentMonth) {
      return today.withDayOfMonth(clampedToCurrentMonth);
    }
    LocalDate prevMonth = today.minusMonths(1);
    return prevMonth.withDayOfMonth(Math.min(startDay, prevMonth.lengthOfMonth()));
  }

  static LocalDate computePeriodEnd(LocalDate periodStart) {
    return periodStart.plusMonths(1).minusDays(1);
  }

  static LocalDate computeDeadline(LocalDate periodStart, int paymentDay) {
    return periodStart.withDayOfMonth(Math.min(paymentDay, periodStart.lengthOfMonth()));
  }

  private record ContractCandidate(
      long id,
      LocalDate startDate,
      int paymentDay,
      int rent,
      String propertyName,
      String tenantName
  ) {}

  private List<VacantPropertyEntry> loadVacantProperties(Connection conn) throws SQLException {
    String sql = """
        SELECT p.name AS property_name, p.type, a.address, a.number, a.neighborhood
        FROM properties p
        JOIN addresses a ON a.id = p.address_id
        WHERE NOT EXISTS (
          SELECT 1
          FROM contracts c
          JOIN (
            SELECT id,
                   MAX(id) OVER (PARTITION BY property_id) AS latest_id,
                   DATEADD('MONTH',
                     CAST(SUBSTRING(duration, 2, LENGTH(duration) - 2) AS INT),
                     start_date) AS end_date
            FROM contracts
          ) ranked ON ranked.id = c.id
          WHERE c.property_id = p.id
            AND ranked.id = ranked.latest_id
            AND ranked.end_date >= CURRENT_DATE
        )
        ORDER BY p.name ASC
        """;
    List<VacantPropertyEntry> results = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        results.add(mapVacantProperty(rs));
      }
    }
    return results;
  }

  private List<ExpiringContractEntry> loadExpiringContracts(Connection conn, LocalDate today) throws SQLException {
    LocalDate horizon = today.plusDays(EXPIRING_HORIZON_DAYS);
    String sql = """
        WITH latest AS (
          SELECT id,
                 MAX(id) OVER (PARTITION BY property_id) AS latest_id,
                 DATEADD('MONTH',
                   CAST(SUBSTRING(duration, 2, LENGTH(duration) - 2) AS INT),
                   start_date) AS end_date
          FROM contracts
        )
        SELECT p.name AS property_name,
               COALESCE(pp.name, jp.corporate_name) AS tenant_name,
               l.end_date,
               DATEDIFF('DAY', CAST(? AS DATE), l.end_date) AS days_left
        FROM latest l
        JOIN contracts c ON c.id = l.id
        JOIN properties p ON p.id = c.property_id
        LEFT JOIN (
          SELECT ct.contract_id,
                 ct.tenant_id,
                 ct.tenant_type,
                 ROW_NUMBER() OVER (PARTITION BY ct.contract_id ORDER BY ct.id) AS rn
          FROM contract_tenants ct
        ) ct ON ct.contract_id = c.id AND ct.rn = 1
        LEFT JOIN physical_persons pp ON ct.tenant_type = 'PHYSICAL' AND pp.id = ct.tenant_id
        LEFT JOIN juridical_persons jp ON ct.tenant_type = 'JURIDICAL' AND jp.id = ct.tenant_id
        WHERE l.id = l.latest_id
          AND l.end_date >= CAST(? AS DATE)
          AND l.end_date <= CAST(? AS DATE)
        ORDER BY l.end_date ASC
        """;
    List<ExpiringContractEntry> results = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDate(1, Date.valueOf(today));
      stmt.setDate(2, Date.valueOf(today));
      stmt.setDate(3, Date.valueOf(horizon));
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          results.add(mapExpiringContract(rs));
        }
      }
    }
    return results;
  }

  private TopRentEntry mapTopRent(ResultSet rs, int rank) throws SQLException {
    return new TopRentEntry(
        rank,
        rs.getString("property_name"),
        rs.getString("tenant_name"),
        rs.getInt("rent")
    );
  }

  private UnpaidRentEntry mapUnpaidRent(ResultSet rs) throws SQLException {
    return new UnpaidRentEntry(
        rs.getString("property_name"),
        rs.getString("tenant_name"),
        rs.getInt("rent"),
        rs.getDate("due_date").toLocalDate()
    );
  }

  private VacantPropertyEntry mapVacantProperty(ResultSet rs) throws SQLException {
    String address = rs.getString("address") + ", " + rs.getString("number")
        + " — " + rs.getString("neighborhood");
    return new VacantPropertyEntry(
        rs.getString("property_name"),
        rs.getString("type"),
        address
    );
  }

  private ExpiringContractEntry mapExpiringContract(ResultSet rs) throws SQLException {
    int daysLeft = rs.getInt("days_left");
    UrgencyLevel urgency;
    if (daysLeft < URGENCY_URGENT_DAYS) {
      urgency = UrgencyLevel.URGENT;
    } else if (daysLeft < URGENCY_WARNING_DAYS) {
      urgency = UrgencyLevel.WARNING;
    } else {
      urgency = UrgencyLevel.NORMAL;
    }
    return new ExpiringContractEntry(
        rs.getDate("end_date").toLocalDate(),
        rs.getString("property_name"),
        rs.getString("tenant_name"),
        daysLeft,
        urgency
    );
  }
}
