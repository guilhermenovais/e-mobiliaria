package com.guilherme.emobiliaria.receipt.infrastructure.repository;

import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import com.guilherme.emobiliaria.receipt.domain.repository.PaymentProofRepository;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import jakarta.inject.Inject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class JdbcPaymentProofRepository implements PaymentProofRepository {

  private final DataSource dataSource;

  @Inject
  public JdbcPaymentProofRepository(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  @Override
  public PaymentProof create(PaymentProof proof) {
    String sql =
        "INSERT INTO payment_proofs (receipt_id, original_filename, display_name, stored_filename, file_type, attached_at) VALUES (?, ?, ?, ?, ?, ?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setLong(1, proof.getReceiptId());
      stmt.setString(2, proof.getOriginalFileName());
      stmt.setString(3, proof.getDisplayName());
      stmt.setString(4, proof.getStoredFileName());
      stmt.setString(5, proof.getFileType().name());
      stmt.setDate(6, Date.valueOf(proof.getAttachedAt()));
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        proof.setId(keys.getLong(1));
      }
      return proof;
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PaymentProof.NOT_FOUND, e);
    }
  }

  @Override
  public void delete(Long id) {
    String sql = "DELETE FROM payment_proofs WHERE id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, id);
      if (stmt.executeUpdate() == 0) {
        throw new PersistenceException(ErrorMessage.PaymentProof.NOT_FOUND, null);
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PaymentProof.NOT_FOUND, e);
    }
  }

  @Override
  public List<PaymentProof> findAllByReceiptId(Long receiptId) {
    String sql =
        "SELECT id, receipt_id, original_filename, display_name, stored_filename, file_type, attached_at FROM payment_proofs WHERE receipt_id=? ORDER BY attached_at, id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, receiptId);
      try (ResultSet rs = stmt.executeQuery()) {
        List<PaymentProof> proofs = new ArrayList<>();
        while (rs.next()) {
          proofs.add(map(rs));
        }
        return proofs;
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PaymentProof.NOT_FOUND, e);
    }
  }

  @Override
  public void deleteAllByReceiptId(Long receiptId) {
    String sql = "DELETE FROM payment_proofs WHERE receipt_id=?";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      stmt.setLong(1, receiptId);
      stmt.executeUpdate();
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PaymentProof.NOT_FOUND, e);
    }
  }

  @Override
  public Map<Long, Integer> countByReceiptIds(List<Long> receiptIds) {
    Map<Long, Integer> result = new HashMap<>();
    if (receiptIds == null || receiptIds.isEmpty()) {
      return result;
    }
    for (Long id : receiptIds) {
      result.put(id, 0);
    }
    String placeholders = receiptIds.stream().map(id -> "?").collect(Collectors.joining(", "));
    String sql =
        "SELECT receipt_id, COUNT(*) as cnt FROM payment_proofs WHERE receipt_id IN (" + placeholders + ") GROUP BY receipt_id";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql)) {
      for (int i = 0; i < receiptIds.size(); i++) {
        stmt.setLong(i + 1, receiptIds.get(i));
      }
      try (ResultSet rs = stmt.executeQuery()) {
        while (rs.next()) {
          result.put(rs.getLong("receipt_id"), rs.getInt("cnt"));
        }
      }
    } catch (SQLException e) {
      throw new PersistenceException(ErrorMessage.PaymentProof.NOT_FOUND, e);
    }
    return result;
  }

  private PaymentProof map(ResultSet rs) throws SQLException {
    return PaymentProof.restore(rs.getLong("id"), rs.getString("original_filename"),
        rs.getString("display_name"), rs.getString("stored_filename"),
        ProofFileType.valueOf(rs.getString("file_type")), rs.getDate("attached_at").toLocalDate(),
        rs.getLong("receipt_id"));
  }
}
