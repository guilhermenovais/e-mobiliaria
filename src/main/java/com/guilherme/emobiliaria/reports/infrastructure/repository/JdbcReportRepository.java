package com.guilherme.emobiliaria.reports.infrastructure.repository;

import com.guilherme.emobiliaria.inflation.domain.entity.IndexType;
import com.guilherme.emobiliaria.inflation.domain.repository.InflationIndexRepository;
import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRow;
import com.guilherme.emobiliaria.reports.domain.entity.PaymentReportRowStatus;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyOccupationHistory;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyRentHistory;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.reports.domain.entity.VacancyTableRow;
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
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class JdbcReportRepository implements ReportRepository {

  private static final String[] PT_MONTHS_SHORT =
      {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
  private final DataSource dataSource;
  private final InflationIndexRepository inflationIndexRepository;

  @Inject
  public JdbcReportRepository(DataSource dataSource,
      InflationIndexRepository inflationIndexRepository) {
    this.dataSource = dataSource;
    this.inflationIndexRepository = inflationIndexRepository;
  }

  private static int maxConsecutiveFalse(List<Boolean> occupied) {
    int max = 0, current = 0;
    for (boolean b : occupied) {
      if (!b) {
        current++;
        if (current > max)
          max = current;
      } else {
        current = 0;
      }
    }
    return max;
  }

  private static String lastOccupiedMonth(List<YearMonth> months, List<Boolean> occupied) {
    for (int i = occupied.size() - 1; i >= 0; i--) {
      if (occupied.get(i)) {
        YearMonth ym = months.get(i);
        return PT_MONTHS_SHORT[ym.getMonthValue() - 1] + "/" + String.format("%02d",
            ym.getYear() % 100);
      }
    }
    return "";
  }

  private static boolean isOccupied(ContractInterval ci, YearMonth month) {
    YearMonth contractStart = YearMonth.from(ci.start());
    LocalDate endThreshold = month.equals(YearMonth.now()) ? LocalDate.now() : month.atDay(1);
    return !month.isBefore(contractStart) && ci.end().isAfter(endThreshold);
  }

  private static ContractSegment findMostRecentActiveSegment(List<ContractSegment> segments,
      YearMonth month) {
    ContractSegment mostRecent = null;
    LocalDate endThreshold = month.equals(YearMonth.now()) ? LocalDate.now() : month.atDay(1);
    for (ContractSegment seg : segments) {
      YearMonth segStart = YearMonth.from(seg.start());
      if (!month.isBefore(segStart) && !seg.end().isBefore(endThreshold)) {
        if (mostRecent == null || seg.start().isAfter(mostRecent.start())) {
          mostRecent = seg;
        }
      }
    }
    return mostRecent;
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

  @Override
  public RentEvolutionData loadRentEvolutionData() {
    try (Connection conn = dataSource.getConnection()) {
      List<YearMonth> months = loadMonthRange(conn);
      if (months.isEmpty()) {
        return new RentEvolutionData(months, List.of(), List.of());
      }
      List<Long> monthlyTotals = loadMonthlyTotals(conn, months);
      List<PropertyRentHistory> propertyHistories = loadPropertyRentHistories(conn);
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
        return new OccupationRateData(months, List.of(), totalProperties, List.of(), 0.0,
            totalProperties, 0, "", List.of());
      }
      List<Integer> occupiedCounts = loadMonthlyOccupiedCounts(conn, months);
      List<PropertyOccupationHistory> propertyHistories =
          loadPropertyOccupationHistories(conn, months);
      return buildOccupationRateData(months, occupiedCounts, totalProperties, propertyHistories);
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Report.LOAD_ERROR, e);
    }
  }

  private OccupationRateData buildOccupationRateData(List<YearMonth> months,
      List<Integer> occupiedCounts, int totalProperties,
      List<PropertyOccupationHistory> propertyHistories) {
    double avgVacancyRate = 0.0;
    if (totalProperties > 0 && !months.isEmpty()) {
      double sumVacancy = 0;
      for (int count : occupiedCounts) {
        sumVacancy += (totalProperties - count) * 100.0 / totalProperties;
      }
      avgVacancyRate = sumVacancy / months.size();
    }

    int currentVacancyCount =
        totalProperties > 0 ? totalProperties - occupiedCounts.get(occupiedCounts.size() - 1) : 0;

    int longestStreakMonths = 0;
    String longestStreakProperty = "";

    List<VacancyTableRow> tableRows = new ArrayList<>();
    for (PropertyOccupationHistory history : propertyHistories) {
      List<Boolean> occupied = history.occupied();
      int vacantMonths = (int) occupied.stream().filter(b -> !b).count();
      int maxStreak = maxConsecutiveFalse(occupied);
      String lastTenantEnd = lastOccupiedMonth(history.months(), occupied);
      String currentStatus = occupied.get(occupied.size() - 1) ? "Ocupado" : "Vago";

      tableRows.add(
          new VacancyTableRow(history.propertyName(), vacantMonths, maxStreak, lastTenantEnd,
              currentStatus));

      if (maxStreak > longestStreakMonths) {
        longestStreakMonths = maxStreak;
        longestStreakProperty = history.propertyName();
      }
    }
    tableRows.sort(Comparator.comparingInt(VacancyTableRow::vacantMonths).reversed());

    return new OccupationRateData(months, occupiedCounts, totalProperties, propertyHistories,
        avgVacancyRate, currentVacancyCount, longestStreakMonths, longestStreakProperty, tableRows);
  }

  private List<YearMonth> loadMonthRange(Connection conn) throws SQLException {
    String sql = """
        SELECT MIN(start_date) AS earliest, CURRENT_DATE AS today
        FROM contracts
        """;
    try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
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
        SELECT p.id AS property_id, c.start_date, c.rent,
               CASE WHEN c.rescinded_at IS NOT NULL
                    THEN c.rescinded_at
                    ELSE DATEADD('MONTH',
                           CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                           c.start_date)
               END AS end_date
        FROM contracts c
        JOIN properties p ON p.id = c.property_id
        ORDER BY p.id, c.start_date
        """;
    Map<Long, List<ContractSegment>> byProperty = new LinkedHashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        long propertyId = rs.getLong("property_id");
        LocalDate start = rs.getDate("start_date").toLocalDate();
        LocalDate end = rs.getDate("end_date").toLocalDate();
        long rent = rs.getLong("rent");
        byProperty.computeIfAbsent(propertyId, k -> new ArrayList<>())
            .add(new ContractSegment(start, end, rent));
      }
    }
    List<Long> result = new ArrayList<>();
    for (YearMonth month : months) {
      long total = 0L;
      for (List<ContractSegment> segments : byProperty.values()) {
        ContractSegment active = findMostRecentActiveSegment(segments, month);
        if (active != null) {
          total += active.initialRent();
        }
      }
      result.add(total);
    }
    return result;
  }

  private List<PropertyRentHistory> loadPropertyRentHistories(Connection conn) throws SQLException {
    String sql = """
        SELECT p.name AS property_name, c.start_date, c.rent AS initial_rent,
               CASE WHEN c.rescinded_at IS NOT NULL
                    THEN c.rescinded_at
                    ELSE DATEADD('MONTH',
                           CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                           c.start_date)
               END AS end_date
        FROM contracts c
        JOIN properties p ON p.id = c.property_id
        ORDER BY p.name, c.start_date
        """;
    Map<String, List<ContractSegment>> byProperty = new LinkedHashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        String name = rs.getString("property_name");
        LocalDate start = rs.getDate("start_date").toLocalDate();
        LocalDate end = rs.getDate("end_date").toLocalDate();
        long initialRent = rs.getLong("initial_rent");
        byProperty.computeIfAbsent(name, k -> new ArrayList<>())
            .add(new ContractSegment(start, end, initialRent));
      }
    }

    Map<YearMonth, Double> ipcaRates = new HashMap<>(InflationIndexes.IPCA);
    ipcaRates.putAll(inflationIndexRepository.findAll(IndexType.IPCA));
    Map<YearMonth, Double> igpmRates = new HashMap<>(InflationIndexes.IGP_M);
    igpmRates.putAll(inflationIndexRepository.findAll(IndexType.IGP_M));

    YearMonth today = YearMonth.now();
    List<PropertyRentHistory> histories = new ArrayList<>();
    for (Map.Entry<String, List<ContractSegment>> entry : byProperty.entrySet()) {
      String propertyName = entry.getKey();
      List<ContractSegment> segments = entry.getValue();

      // segments are ordered by start_date from SQL; first segment is the earliest
      YearMonth propertyStart = YearMonth.from(segments.get(0).start());
      long baselineRent = segments.get(0).initialRent();

      List<YearMonth> propertyMonths = new ArrayList<>();
      for (YearMonth m = propertyStart; !m.isAfter(today); m = m.plusMonths(1)) {
        propertyMonths.add(m);
      }

      List<Long> actual = new ArrayList<>();
      List<Long> ipca = new ArrayList<>();
      List<Long> igpm = new ArrayList<>();

      for (YearMonth month : propertyMonths) {
        ContractSegment active = findMostRecentActiveSegment(segments, month);
        actual.add(active != null ? active.initialRent() : 0L);
        ipca.add(project(baselineRent, propertyStart, month, ipcaRates));
        igpm.add(project(baselineRent, propertyStart, month, igpmRates));
      }
      histories.add(new PropertyRentHistory(propertyName, propertyMonths, actual, ipca, igpm));
    }
    return histories;
  }

  private int loadTotalPropertyCount(Connection conn) throws SQLException {
    String sql = "SELECT COUNT(*) AS cnt FROM properties";
    try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
      rs.next();
      return rs.getInt("cnt");
    }
  }

  private List<Integer> loadMonthlyOccupiedCounts(Connection conn, List<YearMonth> months)
      throws SQLException {
    String sql = """
        SELECT p.id,
               c.start_date,
               CASE WHEN c.rescinded_at IS NOT NULL
                    THEN c.rescinded_at
                    ELSE DATEADD('MONTH',
                           CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                           c.start_date)
               END AS end_date
        FROM contracts c
        JOIN properties p ON p.id = c.property_id
        """;
    List<ContractInterval> intervals = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        intervals.add(new ContractInterval(rs.getLong("id"), rs.getDate("start_date").toLocalDate(),
            rs.getDate("end_date").toLocalDate()));
      }
    }
    List<Integer> counts = new ArrayList<>();
    for (YearMonth month : months) {
      int count = (int) intervals.stream().filter(ci -> isOccupied(ci, month))
          .map(ContractInterval::propertyId).distinct().count();
      counts.add(count);
    }
    return counts;
  }

  private List<PropertyOccupationHistory> loadPropertyOccupationHistories(Connection conn,
      List<YearMonth> months) throws SQLException {
    String sql = """
        SELECT p.id AS property_id, p.name AS property_name,
               c.start_date,
               CASE WHEN c.rescinded_at IS NOT NULL
                    THEN c.rescinded_at
                    ELSE DATEADD('MONTH',
                           CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                           c.start_date)
               END AS end_date
        FROM properties p
        LEFT JOIN contracts c ON c.property_id = p.id
        ORDER BY p.name
        """;
    Map<Long, String> propertyNames = new LinkedHashMap<>();
    Map<Long, List<ContractInterval>> propertyContracts = new LinkedHashMap<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
      while (rs.next()) {
        long propertyId = rs.getLong("property_id");
        propertyNames.put(propertyId, rs.getString("property_name"));
        propertyContracts.computeIfAbsent(propertyId, k -> new ArrayList<>());
        if (rs.getDate("start_date") != null) {
          propertyContracts.get(propertyId).add(
              new ContractInterval(propertyId, rs.getDate("start_date").toLocalDate(),
                  rs.getDate("end_date").toLocalDate()));
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


  @Override
  public List<YearMonth> loadPaymentReportMonths() {
    String sql = "SELECT MIN(start_date) AS earliest FROM contracts";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      if (!rs.next() || rs.getDate("earliest") == null) {
        return List.of(YearMonth.now());
      }
      YearMonth start = YearMonth.from(rs.getDate("earliest").toLocalDate());
      YearMonth end = YearMonth.now();
      List<YearMonth> months = new ArrayList<>();
      for (YearMonth m = start; !m.isAfter(end); m = m.plusMonths(1)) {
        months.add(m);
      }
      Collections.reverse(months);
      return months;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Report.LOAD_ERROR, e);
    }
  }

  @Override
  public List<PaymentReportRow> loadPaymentReportData(YearMonth month) {
    LocalDate firstDay = month.atDay(1);
    LocalDate lastDay = month.atEndOfMonth();
    String sql = """
        SELECT
            p.name                                     AS property_name,
            c.id                                       AS contract_id,
            r.id                                       AS receipt_id,
            COALESCE(pp.name, jp.corporate_name)       AS tenant_name,
            COALESCE(pp.cpf, jp.cnpj)                  AS tenant_tax_id,
            r.date                                     AS payment_date,
            c.rent                                     AS rent,
            r.discount                                 AS discount,
            r.fine                                     AS fine,
            r.interval_start                           AS period_start,
            r.interval_end                             AS period_end,
            CASE WHEN c.id IS NOT NULL AND EXISTS (
                SELECT 1 FROM receipts r3
                WHERE r3.contract_id = c.id
                  AND r3.payment_due_date >= ?
                  AND r3.payment_due_date <= ?
            ) THEN true ELSE false END                 AS due_date_paid
        FROM properties p
        LEFT JOIN contracts c ON c.property_id = p.id
            AND c.start_date <= ?
            AND CASE WHEN c.rescinded_at IS NOT NULL
                     THEN c.rescinded_at
                     ELSE DATEADD('MONTH',
                            CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT),
                            c.start_date)
                END > ?
            AND c.id = (
                SELECT c2.id FROM contracts c2
                WHERE c2.property_id = p.id
                  AND c2.start_date <= ?
                  AND CASE WHEN c2.rescinded_at IS NOT NULL
                           THEN c2.rescinded_at
                           ELSE DATEADD('MONTH',
                                  CAST(SUBSTRING(c2.duration, 2, LENGTH(c2.duration) - 2) AS INT),
                                  c2.start_date)
                      END > ?
                ORDER BY c2.start_date DESC, c2.id DESC
                LIMIT 1
            )
        LEFT JOIN contract_tenants ct ON ct.contract_id = c.id
            AND ct.id = (SELECT MIN(id) FROM contract_tenants WHERE contract_id = c.id)
        LEFT JOIN physical_persons pp  ON pp.id = ct.tenant_id AND ct.tenant_type = 'PHYSICAL'
        LEFT JOIN juridical_persons jp ON jp.id = ct.tenant_id AND ct.tenant_type = 'JURIDICAL'
        LEFT JOIN receipts r ON r.contract_id = c.id
            AND r.date >= ?
            AND r.date <= ?
        ORDER BY r.date NULLS LAST, p.name
        """;
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setObject(1, firstDay);
      stmt.setObject(2, lastDay);
      stmt.setObject(3, lastDay);
      stmt.setObject(4, firstDay);
      stmt.setObject(5, lastDay);
      stmt.setObject(6, firstDay);
      stmt.setObject(7, firstDay);
      stmt.setObject(8, lastDay);
      List<PaymentReportRow> rows = new ArrayList<>();
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          String propertyName = rs.getString("property_name");
          long contractId = rs.getLong("contract_id");
          boolean hasContract = !rs.wasNull() && contractId != 0;
          long receiptId = rs.getLong("receipt_id");
          boolean hasReceipt = !rs.wasNull() && receiptId != 0;

          PaymentReportRowStatus status;
          String tenantName = null;
          String tenantTaxId = null;
          LocalDate paymentDate = null;
          Integer rent = null;
          LocalDate periodStart = null;
          LocalDate periodEnd = null;

          if (!hasContract) {
            status = PaymentReportRowStatus.VACANT;
          } else if (!hasReceipt) {
            boolean dueDatePaid = rs.getBoolean("due_date_paid");
            if (dueDatePaid) {
              continue;
            }
            status = PaymentReportRowStatus.UNPAID;
            tenantName = rs.getString("tenant_name");
            tenantTaxId = rs.getString("tenant_tax_id");
            rent = rs.getInt("rent");
          } else {
            status = PaymentReportRowStatus.PAID;
            tenantName = rs.getString("tenant_name");
            tenantTaxId = rs.getString("tenant_tax_id");
            java.sql.Date pd = rs.getDate("payment_date");
            paymentDate = pd != null ? pd.toLocalDate() : null;
            int baseRent = rs.getInt("rent");
            int discount = rs.getInt("discount");
            int fine = rs.getInt("fine");
            rent = baseRent - discount + fine;
            java.sql.Date ps = rs.getDate("period_start");
            java.sql.Date pe = rs.getDate("period_end");
            periodStart = ps != null ? ps.toLocalDate() : null;
            periodEnd = pe != null ? pe.toLocalDate() : null;
          }

          rows.add(new PaymentReportRow(propertyName, tenantName, tenantTaxId, paymentDate, rent,
              periodStart, periodEnd, status));
        }
      }
      return rows;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Report.LOAD_ERROR, e);
    }
  }

  private record ContractSegment(LocalDate start, LocalDate end, long initialRent) {
  }


  private record ContractInterval(long propertyId, LocalDate start, LocalDate end) {
  }
}
