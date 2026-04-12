package com.guilherme.emobiliaria.contract.infrastructure.repository;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Period;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JdbcContractRepository implements ContractRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcContractRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Contract create(Contract contract) {
    String sql = "INSERT INTO contracts (start_date, duration, payment_day, rent, purpose, payment_account_id, property_id, landlord_id, landlord_type) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setDate(1, Date.valueOf(contract.getStartDate()));
      stmt.setString(2, contract.getDuration().toString());
      stmt.setInt(3, contract.getPaymentDay());
      stmt.setInt(4, contract.getRent());
      stmt.setString(5, contract.getPurpose());
      stmt.setLong(6, contract.getPaymentAccount().getId());
      stmt.setLong(7, contract.getProperty().getId());
      stmt.setLong(8, contract.getLandlord().getId());
      stmt.setString(9, personType(contract.getLandlord()));
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        contract.setId(keys.getLong(1));
      }
      insertTenants(conn, contract.getId(), contract.getTenants());
      insertGuarantors(conn, contract.getId(), contract.getGuarantors());
      insertWitnesses(conn, contract.getId(), contract.getWitnesses());
      return contract;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, e);
    }
  }

  @Override
  public Contract update(Contract contract) {
    String sql = "UPDATE contracts SET start_date=?, duration=?, payment_day=?, rent=?, purpose=?, payment_account_id=?, property_id=?, landlord_id=?, landlord_type=? WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDate(1, Date.valueOf(contract.getStartDate()));
      stmt.setString(2, contract.getDuration().toString());
      stmt.setInt(3, contract.getPaymentDay());
      stmt.setInt(4, contract.getRent());
      stmt.setString(5, contract.getPurpose());
      stmt.setLong(6, contract.getPaymentAccount().getId());
      stmt.setLong(7, contract.getProperty().getId());
      stmt.setLong(8, contract.getLandlord().getId());
      stmt.setString(9, personType(contract.getLandlord()));
      stmt.setLong(10, contract.getId());
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, null);
      }
      deleteTenants(conn, contract.getId());
      insertTenants(conn, contract.getId(), contract.getTenants());
      deleteGuarantors(conn, contract.getId());
      insertGuarantors(conn, contract.getId(), contract.getGuarantors());
      deleteWitnesses(conn, contract.getId());
      insertWitnesses(conn, contract.getId(), contract.getWitnesses());
      return contract;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, e);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM contracts WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, null);
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, e);
    }
  }

  @Override
  public Optional<Contract> findById(Long id) {
    String sql = "SELECT id, start_date, duration, payment_day, rent, purpose, payment_account_id, property_id, landlord_id, landlord_type FROM contracts WHERE id=?";
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
      throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<Contract> findAll(PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM contracts");
          ResultSet countRs = countStmt.executeQuery()) {
        countRs.next();
        total = countRs.getLong(1);
      }
      String sql = "SELECT id, start_date, duration, payment_day, rent, purpose, payment_account_id, property_id, landlord_id, landlord_type FROM contracts LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, limit);
        stmt.setInt(2, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<Contract> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs, conn));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<Contract> findAllByPropertyId(Long propertyId, PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM contracts WHERE property_id=?")) {
        countStmt.setLong(1, propertyId);
        try (ResultSet countRs = countStmt.executeQuery()) {
          countRs.next();
          total = countRs.getLong(1);
        }
      }
      String sql = "SELECT id, start_date, duration, payment_day, rent, purpose, payment_account_id, property_id, landlord_id, landlord_type FROM contracts WHERE property_id=? LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setLong(1, propertyId);
        stmt.setInt(2, limit);
        stmt.setInt(3, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<Contract> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs, conn));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Contract.NOT_FOUND, e);
    }
  }

  private Contract map(ResultSet rs, Connection conn) throws SQLException {
    long id = rs.getLong("id");
    Period duration = Period.parse(rs.getString("duration"));
    PaymentAccount paymentAccount = loadPaymentAccount(conn, rs.getLong("payment_account_id"));
    Property property = loadProperty(conn, rs.getLong("property_id"));
    Person landlord = loadPerson(conn, rs.getLong("landlord_id"), rs.getString("landlord_type"));
    List<Person> tenants = loadTenants(conn, id);
    List<Person> guarantors = loadGuarantors(conn, id);
    List<Person> witnesses = loadWitnesses(conn, id);
    return Contract.restore(
        id,
        rs.getDate("start_date").toLocalDate(),
        duration,
        rs.getInt("payment_day"),
        rs.getInt("rent"),
        rs.getString("purpose"),
        paymentAccount,
        property,
        landlord,
        tenants,
        guarantors,
        witnesses
    );
  }

  private PaymentAccount loadPaymentAccount(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, bank, bank_branch, account_number, pix_key FROM payment_accounts WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        return PaymentAccount.restore(
            rs.getLong("id"),
            rs.getString("bank"),
            rs.getString("bank_branch"),
            rs.getString("account_number"),
            rs.getString("pix_key")
        );
      }
    }
  }

  private Property loadProperty(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, name, type, cemig, copasa, iptu, address_id FROM properties WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        Address address = loadAddress(conn, rs.getLong("address_id"));
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
  }

  private Person loadPerson(Connection conn, long id, String type) throws SQLException {
    if ("PHYSICAL".equals(type)) {
      return loadPhysicalPerson(conn, id);
    }
    return loadJuridicalPerson(conn, id);
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

  private JuridicalPerson loadJuridicalPerson(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, corporate_name, cnpj, address_id FROM juridical_persons WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        List<PhysicalPerson> representatives = loadRepresentatives(conn, rs.getLong("id"));
        Address address = loadAddress(conn, rs.getLong("address_id"));
        return JuridicalPerson.restore(
            rs.getLong("id"),
            rs.getString("corporate_name"),
            rs.getString("cnpj"),
            representatives,
            address
        );
      }
    }
  }

  private List<PhysicalPerson> loadRepresentatives(Connection conn, long juridicalPersonId) throws SQLException {
    String sql = "SELECT physical_person_id FROM juridical_person_representatives WHERE juridical_person_id=?";
    List<PhysicalPerson> representatives = new ArrayList<>();
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, juridicalPersonId);
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          representatives.add(loadPhysicalPerson(conn, rs.getLong("physical_person_id")));
        }
      }
    }
    return representatives;
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

  private List<Person> loadTenants(Connection conn, long contractId) throws SQLException {
    String sql = "SELECT tenant_id, tenant_type FROM contract_tenants WHERE contract_id=? ORDER BY id";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      try (ResultSet rs = stmt.executeQuery()) {
        List<Person> tenants = new ArrayList<>();
        while (rs.next()) {
          tenants.add(loadPerson(conn, rs.getLong("tenant_id"), rs.getString("tenant_type")));
        }
        return tenants;
      }
    }
  }

  private void insertTenants(Connection conn, long contractId, List<Person> tenants) throws SQLException {
    String sql = "INSERT INTO contract_tenants (contract_id, tenant_id, tenant_type) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (Person tenant : tenants) {
        stmt.setLong(1, contractId);
        stmt.setLong(2, tenant.getId());
        stmt.setString(3, personType(tenant));
        stmt.executeUpdate();
      }
    }
  }

  private void deleteTenants(Connection conn, long contractId) throws SQLException {
    String sql = "DELETE FROM contract_tenants WHERE contract_id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      stmt.executeUpdate();
    }
  }

  private List<Person> loadGuarantors(Connection conn, long contractId) throws SQLException {
    String sql = "SELECT guarantor_id, guarantor_type FROM contract_guarantors WHERE contract_id=? ORDER BY id";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      try (ResultSet rs = stmt.executeQuery()) {
        List<Person> guarantors = new ArrayList<>();
        while (rs.next()) {
          guarantors.add(loadPerson(conn, rs.getLong("guarantor_id"), rs.getString("guarantor_type")));
        }
        return guarantors;
      }
    }
  }

  private void insertGuarantors(Connection conn, long contractId, List<Person> guarantors) throws SQLException {
    String sql = "INSERT INTO contract_guarantors (contract_id, guarantor_id, guarantor_type) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (Person guarantor : guarantors) {
        stmt.setLong(1, contractId);
        stmt.setLong(2, guarantor.getId());
        stmt.setString(3, personType(guarantor));
        stmt.executeUpdate();
      }
    }
  }

  private void deleteGuarantors(Connection conn, long contractId) throws SQLException {
    String sql = "DELETE FROM contract_guarantors WHERE contract_id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      stmt.executeUpdate();
    }
  }

  private List<Person> loadWitnesses(Connection conn, long contractId) throws SQLException {
    String sql = "SELECT witness_id, witness_type FROM contract_witnesses WHERE contract_id=? ORDER BY id";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      try (ResultSet rs = stmt.executeQuery()) {
        List<Person> witnesses = new ArrayList<>();
        while (rs.next()) {
          witnesses.add(loadPerson(conn, rs.getLong("witness_id"), rs.getString("witness_type")));
        }
        return witnesses;
      }
    }
  }

  private void insertWitnesses(Connection conn, long contractId, List<Person> witnesses) throws SQLException {
    String sql = "INSERT INTO contract_witnesses (contract_id, witness_id, witness_type) VALUES (?, ?, ?)";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (Person witness : witnesses) {
        stmt.setLong(1, contractId);
        stmt.setLong(2, witness.getId());
        stmt.setString(3, personType(witness));
        stmt.executeUpdate();
      }
    }
  }

  private void deleteWitnesses(Connection conn, long contractId) throws SQLException {
    String sql = "DELETE FROM contract_witnesses WHERE contract_id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      stmt.executeUpdate();
    }
  }

  private String personType(Person person) {
    return person instanceof PhysicalPerson ? "PHYSICAL" : "JURIDICAL";
  }
}
