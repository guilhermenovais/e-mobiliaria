package com.guilherme.emobiliaria.person.infrastructure.repository;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.repository.AddressRepository;
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

public class JdbcAddressRepository implements AddressRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcAddressRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Address create(Address address) {
    String sql = "INSERT INTO addresses (cep, address, number, complement, neighborhood, city, state) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, address.getCep());
      stmt.setString(2, address.getAddress());
      stmt.setString(3, address.getNumber());
      stmt.setString(4, address.getComplement());
      stmt.setString(5, address.getNeighborhood());
      stmt.setString(6, address.getCity());
      stmt.setString(7, address.getState().name());
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        address.setId(keys.getLong(1));
      }
      return address;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Address.NOT_FOUND, e);
    }
  }

  @Override
  public Address update(Address address) {
    String sql = "UPDATE addresses SET cep=?, address=?, number=?, complement=?, neighborhood=?, city=?, state=? WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, address.getCep());
      stmt.setString(2, address.getAddress());
      stmt.setString(3, address.getNumber());
      stmt.setString(4, address.getComplement());
      stmt.setString(5, address.getNeighborhood());
      stmt.setString(6, address.getCity());
      stmt.setString(7, address.getState().name());
      stmt.setLong(8, address.getId());
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.Address.NOT_FOUND, null);
      }
      return address;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Address.NOT_FOUND, e);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM addresses WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.Address.NOT_FOUND, null);
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Address.NOT_FOUND, e);
    }
  }

  @Override
  public Optional<Address> findById(Long id) {
    String sql = "SELECT id, cep, address, number, complement, neighborhood, city, state FROM addresses WHERE id=?";
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
      throw new PersistenceException(ErrorMessage.Address.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<Address> findAll(PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM addresses");
          ResultSet countRs = countStmt.executeQuery()) {
        countRs.next();
        total = countRs.getLong(1);
      }
      String sql = "SELECT id, cep, address, number, complement, neighborhood, city, state FROM addresses LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, limit);
        stmt.setInt(2, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<Address> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Address.NOT_FOUND, e);
    }
  }

  private Address map(ResultSet rs) throws SQLException {
    return Address.restore(
        rs.getLong("id"),
        rs.getString("cep"),
        rs.getString("address"),
        rs.getString("number"),
        rs.getString("complement"),
        rs.getString("neighborhood"),
        rs.getString("city"),
        BrazilianState.valueOf(rs.getString("state"))
    );
  }
}
