package com.guilherme.emobiliaria.person.infrastructure.repository;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
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

public class JdbcJuridicalPersonRepository implements JuridicalPersonRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcJuridicalPersonRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public JuridicalPerson create(JuridicalPerson person) {
    String sql = "INSERT INTO juridical_persons (corporate_name, cnpj, address_id) VALUES (?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, person.getCorporateName());
      stmt.setString(2, person.getCnpj());
      stmt.setLong(3, person.getAddress().getId());
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        person.setId(keys.getLong(1));
      }
      insertRepresentatives(conn, person.getId(), person.getRepresentatives());
      return person;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public JuridicalPerson update(JuridicalPerson person) {
    String sql = "UPDATE juridical_persons SET corporate_name=?, cnpj=?, address_id=? WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, person.getCorporateName());
      stmt.setString(2, person.getCnpj());
      stmt.setLong(3, person.getAddress().getId());
      stmt.setLong(4, person.getId());
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, null);
      }
      deleteRepresentatives(conn, person.getId());
      insertRepresentatives(conn, person.getId(), person.getRepresentatives());
      return person;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM juridical_persons WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, null);
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public Optional<JuridicalPerson> findById(Long id) {
    String sql = "SELECT id, corporate_name, cnpj, address_id FROM juridical_persons WHERE id=?";
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
      throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<JuridicalPerson> findAll(PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM juridical_persons");
          ResultSet countRs = countStmt.executeQuery()) {
        countRs.next();
        total = countRs.getLong(1);
      }
      String sql = "SELECT id, corporate_name, cnpj, address_id FROM juridical_persons LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, limit);
        stmt.setInt(2, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<JuridicalPerson> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs, conn));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, e);
    }
  }

  private JuridicalPerson map(ResultSet rs, Connection conn) throws SQLException {
    long juridicalId = rs.getLong("id");
    List<PhysicalPerson> representatives = loadRepresentatives(conn, juridicalId);
    Address address = loadAddress(conn, rs.getLong("address_id"));
    return JuridicalPerson.restore(
        juridicalId,
        rs.getString("corporate_name"),
        rs.getString("cnpj"),
        representatives,
        address
    );
  }

  private List<PhysicalPerson> loadRepresentatives(Connection conn, long juridicalPersonId) throws SQLException {
    String sql = "SELECT physical_person_id FROM juridical_person_representatives WHERE juridical_person_id=?";
    List<PhysicalPerson> result = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, juridicalPersonId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.add(loadPhysicalPerson(conn, rs.getLong("physical_person_id")));
        }
      }
    }
    return result;
  }

  private void insertRepresentatives(Connection conn, long juridicalPersonId, List<PhysicalPerson> representatives) throws SQLException {
    String sql = "INSERT INTO juridical_person_representatives (juridical_person_id, physical_person_id) VALUES (?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (PhysicalPerson rep : representatives) {
        stmt.setLong(1, juridicalPersonId);
        stmt.setLong(2, rep.getId());
        stmt.addBatch();
      }
      stmt.executeBatch();
    }
  }

  private void deleteRepresentatives(Connection conn, long juridicalPersonId) throws SQLException {
    String sql = "DELETE FROM juridical_person_representatives WHERE juridical_person_id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, juridicalPersonId);
      stmt.executeUpdate();
    }
  }

  private PhysicalPerson loadPhysicalPerson(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, name, nationality, civil_state, occupation, cpf, id_card_number, address_id FROM physical_persons WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
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
    }
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
