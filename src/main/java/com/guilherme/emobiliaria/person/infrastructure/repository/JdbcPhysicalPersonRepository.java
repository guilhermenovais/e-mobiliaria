package com.guilherme.emobiliaria.person.infrastructure.repository;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PersonFilter;
import com.guilherme.emobiliaria.person.domain.entity.PersonRole;
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

  private static final String ACTIVE_CONTRACTS_CONDITION = """
      pp.id IN (
        SELECT landlord_id FROM contracts
        WHERE landlord_type = 'PHYSICAL'
          AND id = (SELECT MAX(id) FROM contracts c2 WHERE c2.property_id = contracts.property_id)
          AND DATEADD('MONTH', CAST(SUBSTRING(duration, 2, LENGTH(duration) - 2) AS INT), start_date) >= CURRENT_DATE
        UNION
        SELECT tenant_id FROM contract_tenants ct
        JOIN contracts c ON c.id = ct.contract_id
        WHERE ct.tenant_type = 'PHYSICAL'
          AND c.id = (SELECT MAX(id) FROM contracts c2 WHERE c2.property_id = c.property_id)
          AND DATEADD('MONTH', CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT), c.start_date) >= CURRENT_DATE
        UNION
        SELECT witness_id FROM contract_witnesses cw
        JOIN contracts c ON c.id = cw.contract_id
        WHERE cw.witness_type = 'PHYSICAL'
          AND c.id = (SELECT MAX(id) FROM contracts c2 WHERE c2.property_id = c.property_id)
          AND DATEADD('MONTH', CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT), c.start_date) >= CURRENT_DATE
        UNION
        SELECT guarantor_id FROM contract_guarantors cg
        JOIN contracts c ON c.id = cg.contract_id
        WHERE cg.guarantor_type = 'PHYSICAL'
          AND c.id = (SELECT MAX(id) FROM contracts c2 WHERE c2.property_id = c.property_id)
          AND DATEADD('MONTH', CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT), c.start_date) >= CURRENT_DATE
      )""";

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
      if (isConstraintViolation(e)) {
        throw new PersistenceException(ErrorMessage.PhysicalPerson.HAS_ASSOCIATED_RECORDS, e);
      }
      throw new PersistenceException(ErrorMessage.PhysicalPerson.NOT_FOUND, e);
    }
  }

  private static boolean isConstraintViolation(SQLException e) {
    return e.getSQLState() != null && e.getSQLState().startsWith("23");
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
  public Optional<PhysicalPerson> findByCpf(String cpf) {
    String normalizedCpf = cpf == null ? null : cpf.replaceAll("[^0-9]", "");
    if (normalizedCpf == null || normalizedCpf.isBlank()) {
      return Optional.empty();
    }
    String sql = "SELECT id, name, nationality, civil_state, occupation, cpf, id_card_number, address_id FROM physical_persons WHERE cpf=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, normalizedCpf);
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
  public PagedResult<PhysicalPerson> findAll(PaginationInput pagination, PersonFilter filter) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    String whereClause = buildFilterWhereClause(filter, "pp");
    try (Connection conn = dataSource.getConnection()) {
      long total;
      String countSql = "SELECT COUNT(*) FROM physical_persons pp" + whereClause;
      try (PreparedStatement countStmt = conn.prepareStatement(countSql);
          ResultSet countRs = countStmt.executeQuery()) {
        countRs.next();
        total = countRs.getLong(1);
      }
      String sql = "SELECT pp.id, pp.name, pp.nationality, pp.civil_state, pp.occupation, pp.cpf, pp.id_card_number, pp.address_id FROM physical_persons pp"
          + whereClause + " LIMIT ? OFFSET ?";
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

  @Override
  public PagedResult<PhysicalPerson> search(String query, PaginationInput pagination, PersonFilter filter) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    String searchTerm = "%" + query + "%";
    String filterClause = buildFilterWhereClause(filter, "pp");
    String searchCondition = filterClause.isEmpty()
        ? " WHERE (pp.name ILIKE ? OR pp.cpf ILIKE ? OR pp.id_card_number ILIKE ?)"
        : filterClause + " AND (pp.name ILIKE ? OR pp.cpf ILIKE ? OR pp.id_card_number ILIKE ?)";
    try (Connection conn = dataSource.getConnection()) {
      long total;
      String countSql = "SELECT COUNT(*) FROM physical_persons pp" + searchCondition;
      try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
        countStmt.setString(1, searchTerm);
        countStmt.setString(2, searchTerm);
        countStmt.setString(3, searchTerm);
        try (ResultSet countRs = countStmt.executeQuery()) {
          countRs.next();
          total = countRs.getLong(1);
        }
      }
      String sql = "SELECT pp.id, pp.name, pp.nationality, pp.civil_state, pp.occupation, pp.cpf, pp.id_card_number, pp.address_id FROM physical_persons pp"
          + searchCondition + " LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setString(1, searchTerm);
        stmt.setString(2, searchTerm);
        stmt.setString(3, searchTerm);
        stmt.setInt(4, limit);
        stmt.setInt(5, offset);
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

  // ── Filter helpers ─────────────────────────────────────────────────────────

  private String buildFilterWhereClause(PersonFilter filter, String alias) {
    if (filter == null || filter.equals(PersonFilter.NONE)) {
      return "";
    }
    List<String> conditions = new ArrayList<>();
    if (filter.role() != null) {
      conditions.add(roleCondition(filter.role(), alias));
    }
    if (filter.activeContractsOnly()) {
      conditions.add(ACTIVE_CONTRACTS_CONDITION.replace("pp.", alias + "."));
    }
    if (conditions.isEmpty()) {
      return "";
    }
    return " WHERE " + String.join(" AND ", conditions);
  }

  private String roleCondition(PersonRole role, String alias) {
    return switch (role) {
      case LANDLORD -> alias + ".id IN (SELECT landlord_id FROM contracts WHERE landlord_type = 'PHYSICAL')";
      case TENANT -> alias + ".id IN (SELECT tenant_id FROM contract_tenants WHERE tenant_type = 'PHYSICAL')";
      case WITNESS -> alias + ".id IN (SELECT witness_id FROM contract_witnesses WHERE witness_type = 'PHYSICAL')";
      case GUARANTOR -> alias + ".id IN (SELECT guarantor_id FROM contract_guarantors WHERE guarantor_type = 'PHYSICAL')";
    };
  }

  // ── Mapping helpers ────────────────────────────────────────────────────────

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
