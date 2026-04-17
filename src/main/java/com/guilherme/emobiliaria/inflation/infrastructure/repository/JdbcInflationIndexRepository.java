package com.guilherme.emobiliaria.inflation.infrastructure.repository;

import com.guilherme.emobiliaria.inflation.domain.entity.IndexType;
import com.guilherme.emobiliaria.inflation.domain.repository.InflationIndexRepository;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class JdbcInflationIndexRepository implements InflationIndexRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcInflationIndexRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Map<YearMonth, Double> findAll(IndexType type) {
    String sql = "SELECT year_month, monthly_rate FROM inflation_indexes WHERE index_type = ?";
    Map<YearMonth, Double> result = new HashMap<>();
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, type.name());
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          YearMonth ym = YearMonth.from(rs.getDate("year_month").toLocalDate());
          result.put(ym, rs.getDouble("monthly_rate"));
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to load inflation indexes for " + type, e);
    }
    return result;
  }

  @Override
  public Optional<YearMonth> findLatestMonth(IndexType type) {
    String sql = "SELECT MAX(year_month) AS latest FROM inflation_indexes WHERE index_type = ?";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, type.name());
      try (ResultSet rs = stmt.executeQuery()) {
        if (rs.next()) {
          Date date = rs.getDate("latest");
          if (date != null) {
            return Optional.of(YearMonth.from(date.toLocalDate()));
          }
        }
      }
    } catch (SQLException e) {
      throw new RuntimeException("Failed to find latest inflation month for " + type, e);
    }
    return Optional.empty();
  }

  @Override
  public void saveAll(IndexType type, Map<YearMonth, Double> rates) {
    String sql = "MERGE INTO inflation_indexes (index_type, year_month, monthly_rate) KEY (index_type, year_month) VALUES (?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
         PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (Map.Entry<YearMonth, Double> entry : rates.entrySet()) {
        stmt.setString(1, type.name());
        stmt.setDate(2, Date.valueOf(entry.getKey().atDay(1)));
        stmt.setDouble(3, entry.getValue());
        stmt.addBatch();
      }
      stmt.executeBatch();
    } catch (SQLException e) {
      throw new RuntimeException("Failed to save inflation indexes for " + type, e);
    }
  }
}
