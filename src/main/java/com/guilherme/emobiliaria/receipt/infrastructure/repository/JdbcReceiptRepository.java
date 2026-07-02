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
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
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
    String sql =
        "INSERT INTO receipts (date, payment_due_date, interval_start, interval_end, discount, fine, observation, contract_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setDate(1, Date.valueOf(receipt.getDate()));
      stmt.setDate(2, Date.valueOf(receipt.getPaymentDueDate()));
      stmt.setDate(3, Date.valueOf(receipt.getIntervalStart()));
      stmt.setDate(4, Date.valueOf(receipt.getIntervalEnd()));
      stmt.setInt(5, receipt.getDiscount());
      stmt.setInt(6, receipt.getFine());
      stmt.setString(7, receipt.getObservation());
      stmt.setLong(8, receipt.getContract().getId());
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
    String sql =
        "UPDATE receipts SET date=?, payment_due_date=?, interval_start=?, interval_end=?, discount=?, fine=?, observation=?, contract_id=? WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDate(1, Date.valueOf(receipt.getDate()));
      stmt.setDate(2, Date.valueOf(receipt.getPaymentDueDate()));
      stmt.setDate(3, Date.valueOf(receipt.getIntervalStart()));
      stmt.setDate(4, Date.valueOf(receipt.getIntervalEnd()));
      stmt.setInt(5, receipt.getDiscount());
      stmt.setInt(6, receipt.getFine());
      stmt.setString(7, receipt.getObservation());
      stmt.setLong(8, receipt.getContract().getId());
      stmt.setLong(9, receipt.getId());
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
    String sql =
        "SELECT id, date, payment_due_date, interval_start, interval_end, discount, fine, observation, contract_id FROM receipts WHERE id=?";
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
      try (PreparedStatement countStmt = conn.prepareStatement(
          "SELECT COUNT(*) FROM receipts WHERE contract_id=?")) {
        countStmt.setLong(1, contractId);
        try (ResultSet countRs = countStmt.executeQuery()) {
          countRs.next();
          total = countRs.getLong(1);
        }
      }
      String sql =
          "SELECT id, date, payment_due_date, interval_start, interval_end, discount, fine, observation, contract_id FROM receipts WHERE contract_id=? LIMIT ? OFFSET ?";
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

  @Override
  public PagedResult<Receipt> search(String query, Long contractId, PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    String searchTerm = "%" + query + "%";
    String contractFilter = contractId != null ? "AND contract_id = " + contractId : "";
    String countSql =
        "SELECT COUNT(*) FROM receipts WHERE (FORMATDATETIME(interval_start, 'dd/MM/yyyy') LIKE ? OR FORMATDATETIME(interval_end, 'dd/MM/yyyy') LIKE ?) " + contractFilter;
    String dataSql =
        "SELECT id, date, payment_due_date, interval_start, interval_end, discount, fine, observation, contract_id FROM receipts WHERE (FORMATDATETIME(interval_start, 'dd/MM/yyyy') LIKE ? OR FORMATDATETIME(interval_end, 'dd/MM/yyyy') LIKE ?) " + contractFilter + " LIMIT ? OFFSET ?";
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement(countSql)) {
        countStmt.setString(1, searchTerm);
        countStmt.setString(2, searchTerm);
        try (ResultSet countRs = countStmt.executeQuery()) {
          countRs.next();
          total = countRs.getLong(1);
        }
      }
      try (PreparedStatement stmt = conn.prepareStatement(dataSql)) {
        stmt.setString(1, searchTerm);
        stmt.setString(2, searchTerm);
        stmt.setInt(3, limit);
        stmt.setInt(4, offset);
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

  @Override
  public boolean existsByContractAndPaymentDueDate(Long contractId, LocalDate paymentDueDate,
      Long excludeReceiptId) {
    String sql =
        "SELECT COUNT(*) FROM receipts WHERE contract_id = ? AND payment_due_date = ?" + " AND (? IS NULL OR id != ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      stmt.setDate(2, Date.valueOf(paymentDueDate));
      if (excludeReceiptId != null) {
        stmt.setLong(3, excludeReceiptId);
        stmt.setLong(4, excludeReceiptId);
      } else {
        stmt.setNull(3, java.sql.Types.BIGINT);
        stmt.setNull(4, java.sql.Types.BIGINT);
      }
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        return rs.getLong(1) > 0;
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  @Override
  public List<LocalDate> findAllPaymentDueDatesByContractId(Long contractId) {
    String sql = "SELECT payment_due_date FROM receipts WHERE contract_id = ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      try (ResultSet rs = stmt.executeQuery()) {
        List<LocalDate> result = new ArrayList<>();
        while (rs.next()) {
          result.add(rs.getDate("payment_due_date").toLocalDate());
        }
        return result;
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  @Override
  public List<YearMonth> findAllReceiptMonths() {
    String sql =
        "SELECT DISTINCT FORMATDATETIME(date, 'yyyy-MM') AS ym FROM receipts ORDER BY ym DESC";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        ResultSet rs = stmt.executeQuery()) {
      List<YearMonth> result = new ArrayList<>();
      while (rs.next()) {
        result.add(YearMonth.parse(rs.getString("ym")));
      }
      return result;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  @Override
  public List<Receipt> findAllByMonth(YearMonth month) {
    String sql =
        "SELECT id, date, payment_due_date, interval_start, interval_end, discount, fine, observation, contract_id FROM receipts WHERE date BETWEEN ? AND ?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setDate(1, Date.valueOf(month.atDay(1)));
      stmt.setDate(2, Date.valueOf(month.atEndOfMonth()));
      try (ResultSet rs = stmt.executeQuery()) {
        List<Receipt> items = new ArrayList<>();
        while (rs.next()) {
          items.add(map(rs, conn));
        }
        return items;
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.Receipt.NOT_FOUND, e);
    }
  }

  private Receipt map(ResultSet rs, Connection conn) throws SQLException {
    Contract contract = loadContract(conn, rs.getLong("contract_id"));
    return Receipt.restore(rs.getLong("id"), rs.getDate("date").toLocalDate(),
        rs.getDate("payment_due_date").toLocalDate(), rs.getDate("interval_start").toLocalDate(),
        rs.getDate("interval_end").toLocalDate(), rs.getInt("discount"), rs.getInt("fine"),
        rs.getString("observation"), contract);
  }

  private Contract loadContract(Connection conn, long id) throws SQLException {
    String sql =
        "SELECT id, start_date, duration, payment_day, rent, purpose, payment_account_id, property_id, landlord_id, landlord_type FROM contracts WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        PaymentAccount paymentAccount = loadPaymentAccount(conn, rs.getLong("payment_account_id"));
        Property property = loadProperty(conn, rs.getLong("property_id"));
        Person landlord =
            loadPerson(conn, rs.getLong("landlord_id"), rs.getString("landlord_type"));
        List<Person> tenants = loadTenants(conn, id);
        List<Person> guarantors = loadGuarantors(conn, id);
        List<Person> witnesses = loadWitnesses(conn, id);
        return Contract.restore(rs.getLong("id"), rs.getDate("start_date").toLocalDate(),
            Period.parse(rs.getString("duration")), rs.getInt("payment_day"), rs.getInt("rent"),
            rs.getString("purpose"), paymentAccount, property, landlord, tenants, guarantors,
            witnesses);
      }
    }
  }

  private PaymentAccount loadPaymentAccount(Connection conn, long id) throws SQLException {
    String sql =
        "SELECT id, bank, bank_branch, account_number, pix_key FROM payment_accounts WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        return PaymentAccount.restore(rs.getLong("id"), rs.getString("bank"),
            rs.getString("bank_branch"), rs.getString("account_number"), rs.getString("pix_key"));
      }
    }
  }

  private Property loadProperty(Connection conn, long id) throws SQLException {
    String sql =
        "SELECT id, name, type, cemig, copasa, iptu, address_id FROM properties WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        Address address = loadAddress(conn, rs.getLong("address_id"));
        return Property.restore(rs.getLong("id"), rs.getString("name"), rs.getString("type"),
            rs.getString("cemig"), rs.getString("copasa"), rs.getString("iptu"), address);
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
    String sql =
        "SELECT id, name, nationality, civil_state, occupation, cpf, id_card_number, address_id FROM physical_persons WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        Address address = loadAddress(conn, rs.getLong("address_id"));
        return PhysicalPerson.restore(rs.getLong("id"), rs.getString("name"),
            rs.getString("nationality"), CivilState.valueOf(rs.getString("civil_state")),
            rs.getString("occupation"), rs.getString("cpf"), rs.getString("id_card_number"),
            address);
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
        return JuridicalPerson.restore(rs.getLong("id"), rs.getString("corporate_name"),
            rs.getString("cnpj"), representatives, address);
      }
    }
  }

  private List<PhysicalPerson> loadRepresentatives(Connection conn, long juridicalPersonId)
      throws SQLException {
    String sql =
        "SELECT physical_person_id FROM juridical_person_representatives WHERE juridical_person_id=?";
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
    String sql =
        "SELECT id, cep, address, number, complement, neighborhood, city, state FROM addresses WHERE id=?";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      try (ResultSet rs = stmt.executeQuery()) {
        rs.next();
        return Address.restore(rs.getLong("id"), rs.getString("cep"), rs.getString("address"),
            rs.getString("number"), rs.getString("complement"), rs.getString("neighborhood"),
            rs.getString("city"), BrazilianState.valueOf(rs.getString("state")));
      }
    }
  }

  private List<Person> loadTenants(Connection conn, long contractId) throws SQLException {
    String sql =
        "SELECT tenant_id, tenant_type FROM contract_tenants WHERE contract_id=? ORDER BY id";
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

  private List<Person> loadGuarantors(Connection conn, long contractId) throws SQLException {
    String sql =
        "SELECT guarantor_id, guarantor_type FROM contract_guarantors WHERE contract_id=? ORDER BY id";
    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, contractId);
      try (ResultSet rs = stmt.executeQuery()) {
        List<Person> guarantors = new ArrayList<>();
        while (rs.next()) {
          guarantors.add(
              loadPerson(conn, rs.getLong("guarantor_id"), rs.getString("guarantor_type")));
        }
        return guarantors;
      }
    }
  }

  private List<Person> loadWitnesses(Connection conn, long contractId) throws SQLException {
    String sql =
        "SELECT witness_id, witness_type FROM contract_witnesses WHERE contract_id=? ORDER BY id";
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
}
