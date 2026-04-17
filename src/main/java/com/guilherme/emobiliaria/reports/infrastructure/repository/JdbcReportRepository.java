package com.guilherme.emobiliaria.reports.infrastructure.repository;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyOccupationHistory;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyRentHistory;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.reports.domain.repository.ReportRepository;
import com.guilherme.emobiliaria.shared.chart.InflationIndexes;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcReportRepository implements ReportRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcReportRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public RentEvolutionData loadRentEvolutionData() {
    try (Connection conn = dataSource.getConnection()) {
      List<YearMonth> months = loadMonthRange(conn);
      if (months.isEmpty()) {
        return new RentEvolutionData(months, List.of(), List.of());
      }
      List<Long> monthlyTotals = loadMonthlyTotals(conn, months);
      List<PropertyRentHistory> propertyHistories = loadPropertyRentHistories(conn, months);
      return new RentEvolutionData(months, monthlyTotals, propertyHistories);
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Report.LOAD_ERROR, e);
    }
  }

  @Override
  public OccupationRateData loadOccupationRateData() {
    try (Connection conn = dataSource.getConnection()) {
      List<YearMonth> months = loadMonthRange(conn);
      int totalProperties = loadTotalPropertyCount(conn);
      if (months.isEmpty()) {
        return new OccupationRateData(months, List.of(), totalProperties, List.of());
      }
      List<Integer> occupiedCounts = loadMonthlyOccupiedCounts(conn, months);
      List<PropertyOccupationHistory> propertyHistories =
          loadPropertyOccupationHistories(conn, months);
      return new OccupationRateData(months, occupiedCounts, totalProperties, propertyHistories);
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Report.LOAD_ERROR, e);
    }
  }

  private List<YearMonth> loadMonthRange(Connection conn) throws SQLException {
    String sql = """
        SELECT MIN(start_date) AS earliest, CURRENT_DATE AS today
        FROM contracts
        """;
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      if (!rs.next() || rs.getDate("earliest") == null) {
        return List.of();
      }
      YearMonth start = YearMonth.from(rs.getDate("earliest").toLocalDate());
      YearMonth end = YearMonth.from(rs.getDate("today").toLocalDate());
      List<YearMonth> months = new ArrayList<>();
      for (YearMonth m = start; !m.isAfter(end); m = m.plusMonths(1)) {
        months.add(m);
      }
      return months;
    }
  }

  private List<Long> loadMonthlyTotals(Connection conn, List<YearMonth> months)
      throws SQLException {
    String sql = """
        SELECT YEAR(date) AS yr, MONTH(date) AS mo, SUM(c.rent) AS total
        FROM receipts r
        JOIN contracts c ON c.id = r.contract_id
        GROUP BY YEAR(date), MONTH(date)
        """;
    Map<YearMonth, Long> totalsMap = new LinkedHashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        YearMonth ym = YearMonth.of(rs.getInt("yr"), rs.getInt("mo"));
        totalsMap.put(ym, rs.getLong("total"));
      }
    }
    List<Long> result = new ArrayList<>();
    for (YearMonth m : months) {
      result.add(totalsMap.getOrDefault(m, 0L));
    }
    return result;
  }

  private List<PropertyRentHistory> loadPropertyRentHistories(Connection conn,
      List<YearMonth> months) throws SQLException {
    String sql = """
        SELECT p.name AS property_name, c.start_date, c.rent AS initial_rent,
               DATEADD('MONTH',
                 CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                 c.start_date) AS end_date
        FROM contracts c
        JOIN properties p ON p.id = c.property_id
        ORDER BY p.name, c.start_date
        """;
    Map<String, List<ContractSegment>> byProperty = new LinkedHashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        String name = rs.getString("property_name");
        LocalDate start = rs.getDate("start_date").toLocalDate();
        LocalDate end = rs.getDate("end_date").toLocalDate();
        long initialRent = rs.getLong("initial_rent");
        byProperty.computeIfAbsent(name, k -> new ArrayList<>())
            .add(new ContractSegment(start, end, initialRent));
      }
    }

    List<PropertyRentHistory> histories = new ArrayList<>();
    for (Map.Entry<String, List<ContractSegment>> entry : byProperty.entrySet()) {
      String propertyName = entry.getKey();
      List<ContractSegment> segments = entry.getValue();
      List<Long> actual = new ArrayList<>();
      List<Long> ipca = new ArrayList<>();
      List<Long> igpm = new ArrayList<>();

      for (YearMonth month : months) {
        ContractSegment active = findActiveSegment(segments, month);
        if (active == null) {
          actual.add(0L);
          ipca.add(0L);
          igpm.add(0L);
        } else {
          actual.add(active.initialRent());
          ipca.add(project(active.initialRent(), YearMonth.from(active.start()), month,
              InflationIndexes.IPCA));
          igpm.add(project(active.initialRent(), YearMonth.from(active.start()), month,
              InflationIndexes.IGP_M));
        }
      }
      histories.add(new PropertyRentHistory(propertyName, months, actual, ipca, igpm));
    }
    return histories;
  }

  private int loadTotalPropertyCount(Connection conn) throws SQLException {
    String sql = "SELECT COUNT(*) AS cnt FROM properties";
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      rs.next();
      return rs.getInt("cnt");
    }
  }

  private List<Integer> loadMonthlyOccupiedCounts(Connection conn, List<YearMonth> months)
      throws SQLException {
    String sql = """
        SELECT p.id,
               c.start_date,
               DATEADD('MONTH',
                 CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                 c.start_date) AS end_date
        FROM contracts c
        JOIN properties p ON p.id = c.property_id
        """;
    List<ContractInterval> intervals = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        intervals.add(new ContractInterval(
            rs.getLong("id"),
            rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate()
        ));
      }
    }
    List<Integer> counts = new ArrayList<>();
    for (YearMonth month : months) {
      int count = (int) intervals.stream().filter(ci -> isOccupied(ci, month)).count();
      counts.add(count);
    }
    return counts;
  }

  private List<PropertyOccupationHistory> loadPropertyOccupationHistories(Connection conn,
      List<YearMonth> months) throws SQLException {
    String sql = """
        SELECT p.id AS property_id, p.name AS property_name,
               c.start_date,
               DATEADD('MONTH',
                 CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                 c.start_date) AS end_date
        FROM properties p
        LEFT JOIN contracts c ON c.property_id = p.id
        ORDER BY p.name
        """;
    Map<Long, String> propertyNames = new LinkedHashMap<>();
    Map<Long, List<ContractInterval>> propertyContracts = new LinkedHashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql);
         ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        long propertyId = rs.getLong("property_id");
        propertyNames.put(propertyId, rs.getString("property_name"));
        propertyContracts.computeIfAbsent(propertyId, k -> new ArrayList<>());
        if (rs.getDate("start_date") != null) {
          propertyContracts.get(propertyId).add(new ContractInterval(
              propertyId,
              rs.getDate("start_date").toLocalDate(),
              rs.getDate("end_date").toLocalDate()
          ));
        }
      }
    }

    List<PropertyOccupationHistory> histories = new ArrayList<>();
    for (Map.Entry<Long, String> entry : propertyNames.entrySet()) {
      long propertyId = entry.getKey();
      String name = entry.getValue();
      List<ContractInterval> contracts = propertyContracts.get(propertyId);
      List<Boolean> occupiedList = new ArrayList<>();
      for (YearMonth month : months) {
        occupiedList.add(contracts.stream().anyMatch(ci -> isOccupied(ci, month)));
      }
      histories.add(new PropertyOccupationHistory(name, months, occupiedList));
    }
    return histories;
  }

  private static boolean isOccupied(ContractInterval ci, YearMonth month) {
    YearMonth contractStart = YearMonth.from(ci.start());
    YearMonth contractEnd = YearMonth.from(ci.end());
    return !month.isBefore(contractStart) && !month.isAfter(contractEnd);
  }

  private static ContractSegment findActiveSegment(List<ContractSegment> segments,
      YearMonth month) {
    for (ContractSegment seg : segments) {
      YearMonth segStart = YearMonth.from(seg.start());
      YearMonth segEnd = YearMonth.from(seg.end());
      if (!month.isBefore(segStart) && !month.isAfter(segEnd)) {
        return seg;
      }
    }
    return null;
  }

  static long project(long initialCents, YearMonth from, YearMonth to,
      Map<YearMonth, Double> rates) {
    double value = initialCents;
    for (YearMonth m = from; m.isBefore(to); m = m.plusMonths(1)) {
      double rate = rates.getOrDefault(m, 0.0);
      value = value * (1 + rate);
    }
    return Math.round(value);
  }

  private record ContractSegment(LocalDate start, LocalDate end, long initialRent) {}

  private record ContractInterval(long propertyId, LocalDate start, LocalDate end) {}
}
