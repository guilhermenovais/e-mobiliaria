package com.guilherme.emobiliaria.person.infrastructure.repository;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
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

public class JdbcPhysicalPersonRepository implements PhysicalPersonRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcPhysicalPersonRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public PhysicalPerson create(PhysicalPerson person) {
    String sql = "INSERT INTO physical_persons (name, nationality, civil_state, occupation, cpf, id_card_number, address_id) VALUES (?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, person.getName());
      stmt.setString(2, person.getNationality());
      stmt.setString(3, person.getCivilState().name());
      stmt.setString(4, person.getOccupation());
      stmt.setString(5, person.getCpf());
      stmt.setString(6, person.getIdCardNumber());
      stmt.setLong(7, person.getAddress().getId());
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        person.setId(keys.getLong(1));
      }
      return person;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public PhysicalPerson update(PhysicalPerson person) {
    String sql = "UPDATE physical_persons SET name=?, nationality=?, civil_state=?, occupation=?, cpf=?, id_card_number=?, address_id=? WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, person.getName());
      stmt.setString(2, person.getNationality());
      stmt.setString(3, person.getCivilState().name());
      stmt.setString(4, person.getOccupation());
      stmt.setString(5, person.getCpf());
      stmt.setString(6, person.getIdCardNumber());
      stmt.setLong(7, person.getAddress().getId());
      stmt.setLong(8, person.getId());
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, null);
      }
      return person;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM physical_persons WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, null);
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public Optional<PhysicalPerson> findById(Long id) {
    String sql = "SELECT id, name, nationality, civil_state, occupation, cpf, id_card_number, address_id FROM physical_persons WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        if (!rs.next()) {
          return Optional.empty();
        }
        return Optional.of(map(rs, conn));
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<PhysicalPerson> findAll(PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM physical_persons");
          ResultSet countRs = countStmt.executeQuery()) {
        countRs.next();
        total = countRs.getLong(1);
      }
      String sql = "SELECT id, name, nationality, civil_state, occupation, cpf, id_card_number, address_id FROM physical_persons LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, limit);
        stmt.setInt(2, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<PhysicalPerson> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs, conn));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<PhysicalPerson> findByName(String name, PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    String searchTerm = "%" + name + "%";
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM physical_persons WHERE name ILIKE ?")) {
        countStmt.setString(1, searchTerm);
        try (ResultSet countRs = countStmt.executeQuery()) {
          countRs.next();
          total = countRs.getLong(1);
        }
      }
      String sql = "SELECT id, name, nationality, civil_state, occupation, cpf, id_card_number, address_id FROM physical_persons WHERE name ILIKE ? LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, searchTerm);
        stmt.setInt(2, limit);
        stmt.setInt(3, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<PhysicalPerson> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs, conn));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, e);
    }
  }

  private PhysicalPerson map(ResultSet rs, Connection conn) throws SQLException {
    Address address = loadAddress(conn, rs.getLong("address_id"));
    return PhysicalPerson.restore(
        rs.getLong("id"),
        rs.getString("name"),
        rs.getString("nationality"),
        CivilState.valueOf(rs.getString("civil_state")),
        rs.getString("occupation"),
        rs.getString("cpf"),
        rs.getString("id_card_number"),
        address
    );
  }

  private Address loadAddress(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, cep, address, number, complement, neighborhood, city, state FROM addresses WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
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
  }
}
