package com.guilherme.emobiliaria.receipt.infrastructure.repository;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
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

public class JdbcReceiptRepository implements ReceiptRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcReceiptRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public Receipt create(Receipt receipt) {
    String sql = "INSERT INTO receipts (date, interval_start, interval_end, discount, fine, contract_id) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setDate(1, Date.valueOf(receipt.getDate()));
      stmt.setDate(2, Date.valueOf(receipt.getIntervalStart()));
      stmt.setDate(3, Date.valueOf(receipt.getIntervalEnd()));
      stmt.setInt(4, receipt.getDiscount());
      stmt.setInt(5, receipt.getFine());
      stmt.setLong(6, receipt.getContract().getId());
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        receipt.setId(keys.getLong(1));
      }
      return receipt;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  @Override
  public Receipt update(Receipt receipt) {
    String sql = "UPDATE receipts SET date=?, interval_start=?, interval_end=?, discount=?, fine=?, contract_id=? WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDate(1, Date.valueOf(receipt.getDate()));
      stmt.setDate(2, Date.valueOf(receipt.getIntervalStart()));
      stmt.setDate(3, Date.valueOf(receipt.getIntervalEnd()));
      stmt.setInt(4, receipt.getDiscount());
      stmt.setInt(5, receipt.getFine());
      stmt.setLong(6, receipt.getContract().getId());
      stmt.setLong(7, receipt.getId());
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, null);
      }
      return receipt;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM receipts WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, null);
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  @Override
  public Optional<Receipt> findById(Long id) {
    String sql = "SELECT id, date, interval_start, interval_end, discount, fine, contract_id FROM receipts WHERE id=?";
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
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<Receipt> findAllByContractId(Long contractId, PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM receipts WHERE contract_id=?")) {
        countStmt.setLong(1, contractId);
        try (ResultSet countRs = countStmt.executeQuery()) {
          countRs.next();
          total = countRs.getLong(1);
        }
      }
      String sql = "SELECT id, date, interval_start, interval_end, discount, fine, contract_id FROM receipts WHERE contract_id=? LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setLong(1, contractId);
        stmt.setInt(2, limit);
        stmt.setInt(3, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<Receipt> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs, conn));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  private Receipt map(ResultSet rs, Connection conn) throws SQLException {
    Contract contract = loadContract(conn, rs.getLong("contract_id"));
    return Receipt.restore(
        rs.getLong("id"),
        rs.getDate("date").toLocalDate(),
        rs.getDate("interval_start").toLocalDate(),
        rs.getDate("interval_end").toLocalDate(),
        rs.getInt("discount"),
        rs.getInt("fine"),
        contract
    );
  }

  private Contract loadContract(Connection conn, long id) throws SQLException {
    String sql = "SELECT id, start_date, duration, payment_day, rent, payment_account_id, property_id, landlord_id, landlord_type FROM contracts WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        PaymentAccount paymentAccount = loadPaymentAccount(conn, rs.getLong("payment_account_id"));
        Property property = loadProperty(conn, rs.getLong("property_id"));
        Person landlord = loadPerson(conn, rs.getLong("landlord_id"), rs.getString("landlord_type"));
        List<Person> tenants = loadTenants(conn, id);
        return Contract.restore(
            rs.getLong("id"),
            rs.getDate("start_date").toLocalDate(),
            Period.parse(rs.getString("duration")),
            rs.getInt("payment_day"),
            rs.getInt("rent"),
            paymentAccount,
            property,
            landlord,
            tenants
        );
      }
    }
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
    String sql = "SELECT id, name, type, purpose, cemig, copasa, iptu, address_id FROM properties WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        Address address = loadAddress(conn, rs.getLong("address_id"));
        return Property.restore(
            rs.getLong("id"),
            rs.getString("name"),
            rs.getString("type"),
            Purpose.valueOf(rs.getString("purpose")),
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
    String sql = "SELECT id, corporate_name, cnpj, representative_id, address_id FROM juridical_persons WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        PhysicalPerson representative = loadPhysicalPerson(conn, rs.getLong("representative_id"));
        Address address = loadAddress(conn, rs.getLong("address_id"));
        return JuridicalPerson.restore(
            rs.getLong("id"),
            rs.getString("corporate_name"),
            rs.getString("cnpj"),
            representative,
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
}
