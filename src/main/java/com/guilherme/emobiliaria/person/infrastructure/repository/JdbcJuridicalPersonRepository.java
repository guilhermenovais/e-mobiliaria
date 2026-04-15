package com.guilherme.emobiliaria.person.infrastructure.repository;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PersonFilter;
import com.guilherme.emobiliaria.person.domain.entity.PersonRole;
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

  private static final String ACTIVE_CONTRACTS_CONDITION = """
      jp.id IN (
        SELECT landlord_id FROM contracts
        WHERE landlord_type = 'JURIDICAL'
          AND id = (SELECT MAX(id) FROM contracts c2 WHERE c2.property_id = contracts.property_id)
          AND DATEADD('MONTH', CAST(SUBSTRING(duration, 2, LENGTH(duration) - 2) AS INT), start_date) >= CURRENT_DATE
        UNION
        SELECT tenant_id FROM contract_tenants ct
        JOIN contracts c ON c.id = ct.contract_id
        WHERE ct.tenant_type = 'JURIDICAL'
          AND c.id = (SELECT MAX(id) FROM contracts c2 WHERE c2.property_id = c.property_id)
          AND DATEADD('MONTH', CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT), c.start_date) >= CURRENT_DATE
        UNION
        SELECT witness_id FROM contract_witnesses cw
        JOIN contracts c ON c.id = cw.contract_id
        WHERE cw.witness_type = 'JURIDICAL'
          AND c.id = (SELECT MAX(id) FROM contracts c2 WHERE c2.property_id = c.property_id)
          AND DATEADD('MONTH', CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT), c.start_date) >= CURRENT_DATE
        UNION
        SELECT guarantor_id FROM contract_guarantors cg
        JOIN contracts c ON c.id = cg.contract_id
        WHERE cg.guarantor_type = 'JURIDICAL'
          AND c.id = (SELECT MAX(id) FROM contracts c2 WHERE c2.property_id = c.property_id)
          AND DATEADD('MONTH', CAST(SUBSTRING(c.duration, 2, LENGTH(c.duration) - 2) AS INT), c.start_date) >= CURRENT_DATE
      )""";

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
      if (isConstraintViolation(e)) {
        throw new PersistenceException(ErrorMessage.JuridicalPerson.HAS_ASSOCIATED_RECORDS, e);
      }
      throw new PersistenceException(ErrorMessage.JuridicalPerson.NOT_FOUND, e);
    }
  }

  private static boolean isConstraintViolation(SQLException e) {
    return e.getSQLState() != null && e.getSQLState().startsWith("23");
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
  public PagedResult<JuridicalPerson> findAll(PaginationInput pagination, PersonFilter filter) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    String whereClause = buildFilterWhereClause(filter, "jp");
    try (Connection conn = dataSource.getConnection()) {
      long total;
      String countSql = "SELECT COUNT(*) FROM juridical_persons jp" + whereClause;
      try (PreparedStatement countStmt = conn.prepareStatement(countSql);
          ResultSet countRs = countStmt.executeQuery()) {
        countRs.next();
        total = countRs.getLong(1);
      }
      String sql = "SELECT jp.id, jp.corporate_name, jp.cnpj, jp.address_id FROM juridical_persons jp"
          + whereClause + " LIMIT ? OFFSET ?";
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

  @Override
  public PagedResult<JuridicalPerson> search(String query, PaginationInput pagination, PersonFilter filter) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    String searchTerm = "%" + query + "%";
    String filterClause = buildFilterWhereClause(filter, "jp");
    String searchCondition = filterClause.isEmpty()
        ? " WHERE (jp.corporate_name ILIKE ? OR jp.cnpj ILIKE ? OR pp.name ILIKE ?)"
        : filterClause + " AND (jp.corporate_name ILIKE ? OR jp.cnpj ILIKE ? OR pp.name ILIKE ?)";
    String countSql = """
        SELECT COUNT(DISTINCT jp.id)
        FROM juridical_persons jp
        LEFT JOIN juridical_person_representatives jpr ON jpr.juridical_person_id = jp.id
        LEFT JOIN physical_persons pp ON pp.id = jpr.physical_person_id
        """ + searchCondition;
    String dataSql = """
        SELECT DISTINCT jp.id, jp.corporate_name, jp.cnpj, jp.address_id
        FROM juridical_persons jp
        LEFT JOIN juridical_person_representatives jpr ON jpr.juridical_person_id = jp.id
        LEFT JOIN physical_persons pp ON pp.id = jpr.physical_person_id
        """ + searchCondition + " LIMIT ? OFFSET ?";
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
        countStmt.setString(1, searchTerm);
        countStmt.setString(2, searchTerm);
        countStmt.setString(3, searchTerm);
        try (ResultSet countRs = countStmt.executeQuery()) {
          countRs.next();
          total = countRs.getLong(1);
        }
      }
      try (PreparedStatement stmt = conn.prepareStatement(dataSql)) {
        stmt.setString(1, searchTerm);
        stmt.setString(2, searchTerm);
        stmt.setString(3, searchTerm);
        stmt.setInt(4, limit);
        stmt.setInt(5, offset);
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
      conditions.add(ACTIVE_CONTRACTS_CONDITION.replace("jp.", alias + "."));
    }
    if (conditions.isEmpty()) {
      return "";
    }
    return " WHERE " + String.join(" AND ", conditions);
  }

  private String roleCondition(PersonRole role, String alias) {
    return switch (role) {
      case LANDLORD -> alias + ".id IN (SELECT landlord_id FROM contracts WHERE landlord_type = 'JURIDICAL')";
      case TENANT -> alias + ".id IN (SELECT tenant_id FROM contract_tenants WHERE tenant_type = 'JURIDICAL')";
      case WITNESS -> alias + ".id IN (SELECT witness_id FROM contract_witnesses WHERE witness_type = 'JURIDICAL')";
      case GUARANTOR -> alias + ".id IN (SELECT guarantor_id FROM contract_guarantors WHERE guarantor_type = 'JURIDICAL')";
    };
  }

  // ── Mapping helpers ────────────────────────────────────────────────────────

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
