package com.guilherme.emobiliaria.property.infrastructure.repository;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcPropertyRepository implements PropertyRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcPropertyRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Property create(Property property) {
    String sql = "INSERT INTO properties (name, type, cemig, copasa, iptu, address_id) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, property.getName());
      stmt.setString(2, property.getType());
      stmt.setString(3, property.getCemig());
      stmt.setString(4, property.getCopasa());
      stmt.setString(5, property.getIptu());
      stmt.setLong(6, property.getAddress().getId());
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        property.setId(keys.getLong(1));
      }
      return property;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, e);
    }
  }

  @Override
  public Property update(Property property) {
    String sql = "UPDATE properties SET name=?, type=?, cemig=?, copasa=?, iptu=?, address_id=? WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, property.getName());
      stmt.setString(2, property.getType());
      stmt.setString(3, property.getCemig());
      stmt.setString(4, property.getCopasa());
      stmt.setString(5, property.getIptu());
      stmt.setLong(6, property.getAddress().getId());
      stmt.setLong(7, property.getId());
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, null);
      }
      return property;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, e);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM properties WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, null);
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, e);
    }
  }

  @Override
  public Optional<Property> findById(Long id) {
    String sql = """
        SELECT p.id, p.name, p.type, p.cemig, p.copasa, p.iptu,
               a.id AS address_id, a.cep, a.address, a.number, a.complement, a.neighborhood, a.city, a.state
        FROM properties p
        JOIN addresses a ON a.id = p.address_id
        WHERE p.id = ?
        """;
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        return Optional.of(map(rs));
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<Property> findAll(PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM properties");
          ResultSet countRs = countStmt.executeQuery()) {
        countRs.next();
        total = countRs.getLong(1);
      }
      String sql = """
          SELECT p.id, p.name, p.type, p.cemig, p.copasa, p.iptu,
                 a.id AS address_id, a.cep, a.address, a.number, a.complement, a.neighborhood, a.city, a.state
          FROM properties p
          JOIN addresses a ON a.id = p.address_id
          LIMIT ? OFFSET ?
          """;
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, limit);
        stmt.setInt(2, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<Property> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<Property> searchByName(String query, PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    String pattern = "%" + query + "%";
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement(
          "SELECT COUNT(*) FROM properties WHERE name ILIKE ?")) {
        countStmt.setString(1, pattern);
        try (ResultSet countRs = countStmt.executeQuery()) {
          countRs.next();
          total = countRs.getLong(1);
        }
      }
      String sql = """
          SELECT p.id, p.name, p.type, p.cemig, p.copasa, p.iptu,
                 a.id AS address_id, a.cep, a.address, a.number, a.complement, a.neighborhood, a.city, a.state
          FROM properties p
          JOIN addresses a ON a.id = p.address_id
          WHERE p.name ILIKE ?
          LIMIT ? OFFSET ?
          """;
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, pattern);
        stmt.setInt(2, limit);
        stmt.setInt(3, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<Property> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Property.NOT_FOUND, e);
    }
  }

  private Property map(ResultSet rs) throws SQLException {
    Address address = Address.restore(
        rs.getLong("address_id"),
        rs.getString("cep"),
        rs.getString("address"),
        rs.getString("number"),
        rs.getString("complement"),
        rs.getString("neighborhood"),
        rs.getString("city"),
        BrazilianState.valueOf(rs.getString("state"))
    );
    return Property.restore(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("type"),
        rs.getString("cemig"),
        rs.getString("copasa"),
        rs.getString("iptu"),
        address
    );
  }
}
