package com.guilherme.emobiliaria.contract.infrastructure.repository;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.domain.repository.PaymentAccountRepository;
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

public class JdbcPaymentAccountRepository implements PaymentAccountRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcPaymentAccountRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public PaymentAccount create(PaymentAccount paymentAccount) {
    String sql = "INSERT INTO payment_accounts (bank, bank_branch, account_number, pix_key) VALUES (?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, paymentAccount.getBank());
      stmt.setString(2, paymentAccount.getBankBranch());
      stmt.setString(3, paymentAccount.getAccountNumber());
      stmt.setString(4, paymentAccount.getPixKey());
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        paymentAccount.setId(keys.getLong(1));
      }
      return paymentAccount;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, e);
    }
  }

  @Override
  public PaymentAccount update(PaymentAccount paymentAccount) {
    String sql = "UPDATE payment_accounts SET bank=?, bank_branch=?, account_number=?, pix_key=? WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setString(1, paymentAccount.getBank());
      stmt.setString(2, paymentAccount.getBankBranch());
      stmt.setString(3, paymentAccount.getAccountNumber());
      stmt.setString(4, paymentAccount.getPixKey());
      stmt.setLong(5, paymentAccount.getId());
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, null);
      }
      return paymentAccount;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, e);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM payment_accounts WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, null);
      }
    } catch (SQLException e) {
      if (isConstraintViolation(e)) {
        throw new PersistenceException(ErrorMessage.PaymentAccount.HAS_ASSOCIATED_CONTRACTS, e);
      }
      throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, e);
    }
  }

  private static boolean isConstraintViolation(SQLException e) {
    return e.getSQLState() != null && e.getSQLState().startsWith("23");
  }

  @Override
  public Optional<PaymentAccount> findById(Long id) {
    String sql = "SELECT id, bank, bank_branch, account_number, pix_key FROM payment_accounts WHERE id=?";
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
      throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, e);
    }
  }

  @Override
  public PagedResult<PaymentAccount> findAll(PaginationInput pagination) {
    int limit = pagination.limit() != null ? pagination.limit() : Integer.MAX_VALUE;
    int offset = pagination.offset() != null ? pagination.offset() : 0;
    try (Connection conn = dataSource.getConnection()) {
      long total;
      try (PreparedStatement countStmt = conn.prepareStatement("SELECT COUNT(*) FROM payment_accounts");
          ResultSet countRs = countStmt.executeQuery()) {
        countRs.next();
        total = countRs.getLong(1);
      }
      String sql = "SELECT id, bank, bank_branch, account_number, pix_key FROM payment_accounts LIMIT ? OFFSET ?";
      try (PreparedStatement stmt = conn.prepareStatement(sql)) {
        stmt.setInt(1, limit);
        stmt.setInt(2, offset);
        try (ResultSet rs = stmt.executeQuery()) {
          List<PaymentAccount> items = new ArrayList<>();
          while (rs.next()) {
            items.add(map(rs));
          }
          return new PagedResult<>(items, total);
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PaymentAccount.NOT_FOUND, e);
    }
  }

  private PaymentAccount map(ResultSet rs) throws SQLException {
    return PaymentAccount.restore(
        rs.getLong("id"),
        rs.getString("bank"),
        rs.getString("bank_branch"),
        rs.getString("account_number"),
        rs.getString("pix_key")
    );
  }
}
