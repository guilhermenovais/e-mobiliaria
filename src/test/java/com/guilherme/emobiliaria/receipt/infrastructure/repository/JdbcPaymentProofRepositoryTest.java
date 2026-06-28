package com.guilherme.emobiliaria.receipt.infrastructure.repository;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.infrastructure.repository.JdbcContractRepository;
import com.guilherme.emobiliaria.contract.infrastructure.repository.JdbcPaymentAccountRepository;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.infrastructure.repository.JdbcAddressRepository;
import com.guilherme.emobiliaria.person.infrastructure.repository.JdbcPhysicalPersonRepository;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.infrastructure.repository.JdbcPropertyRepository;
import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcPaymentProofRepositoryTest {

  private JdbcPaymentProofRepository repository;
  private DataSource dataSource;

  @BeforeEach
  void setUp() {
    String dbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    config.setPassword("");
    dataSource = new HikariDataSource(config);
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    repository = new JdbcPaymentProofRepository(dataSource);
  }

  private Long createReceipt() {
    JdbcAddressRepository addressRepo = new JdbcAddressRepository(dataSource);
    JdbcPhysicalPersonRepository personRepo = new JdbcPhysicalPersonRepository(dataSource);
    JdbcPropertyRepository propertyRepo = new JdbcPropertyRepository(dataSource);
    JdbcPaymentAccountRepository paymentAccountRepo = new JdbcPaymentAccountRepository(dataSource);
    JdbcContractRepository contractRepo = new JdbcContractRepository(dataSource);
    JdbcReceiptRepository receiptRepo = new JdbcReceiptRepository(dataSource);

    Address address = addressRepo.create(
        Address.create("01001000", "Rua A", "1", null, "Centro", "São Paulo", BrazilianState.SP));
    PhysicalPerson person = personRepo.create(
        PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
            "529.982.247-25", "MG-1234567", address));
    Property property = propertyRepo.create(
        Property.create("Apto 1", "Apartamento", "1234567890", "0987654321", "IPTU-001", address));
    PaymentAccount paymentAccount = paymentAccountRepo.create(
        PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null));
    Contract contract = contractRepo.create(
        Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, 150000, "Residencial",
            paymentAccount, property, person, List.of(person), List.of(), List.of()));
    Receipt receipt = receiptRepo.create(
        Receipt.create(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 15),
            LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 0, 0, null, contract));
    return receipt.getId();
  }

  private PaymentProof validProof(Long receiptId) {
    return PaymentProof.create("comprovante.pdf", "Comprovante", "stored-file.pdf",
        ProofFileType.PDF,
        LocalDate.of(2026, 6, 1), receiptId);
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When valid, should persist and return proof with generated id")
    void shouldPersistAndReturnWithId() {
      Long receiptId = createReceipt();
      PaymentProof proof = validProof(receiptId);

      PaymentProof created = repository.create(proof);

      assertNotNull(created.getId());
      assertEquals("comprovante.pdf", created.getOriginalFileName());
      assertEquals("stored-file.pdf", created.getStoredFileName());
      assertEquals(ProofFileType.PDF, created.getFileType());
      assertEquals(receiptId, created.getReceiptId());
    }
  }


  @Nested
  class FindAllByReceiptId {

    @Test
    @DisplayName("When proofs exist for receipt, should return them")
    void shouldReturnProofsForReceipt() {
      Long receiptId = createReceipt();
      repository.create(PaymentProof.create("a.pdf", "a.pdf", "stored-a.pdf", ProofFileType.PDF,
          LocalDate.of(2026, 6, 1), receiptId));
      repository.create(PaymentProof.create("b.jpg", "b.jpg", "stored-b.jpg", ProofFileType.IMAGE,
          LocalDate.of(2026, 6, 2), receiptId));

      List<PaymentProof> proofs = repository.findAllByReceiptId(receiptId);

      assertEquals(2, proofs.size());
    }

    @Test
    @DisplayName("When no proofs exist for receipt, should return empty list")
    void shouldReturnEmptyWhenNoProofs() {
      Long receiptId = createReceipt();

      List<PaymentProof> proofs = repository.findAllByReceiptId(receiptId);

      assertTrue(proofs.isEmpty());
    }
  }


  @Nested
  class Delete {

    @Test
    @DisplayName("When proof exists, should delete it")
    void shouldDeleteExistingProof() {
      Long receiptId = createReceipt();
      PaymentProof proof = repository.create(validProof(receiptId));

      repository.delete(proof.getId());

      assertTrue(repository.findAllByReceiptId(receiptId).isEmpty());
    }

    @Test
    @DisplayName("When proof does not exist, should throw PersistenceException")
    void shouldThrowWhenProofNotFound() {
      assertThrows(PersistenceException.class, () -> repository.delete(999L));
    }
  }


  @Nested
  class DeleteAllByReceiptId {

    @Test
    @DisplayName("When multiple proofs exist, should delete all for receipt")
    void shouldDeleteAllProofsForReceipt() {
      Long receiptId = createReceipt();
      repository.create(PaymentProof.create("a.pdf", "a.pdf", "stored-a.pdf", ProofFileType.PDF,
          LocalDate.of(2026, 6, 1), receiptId));
      repository.create(PaymentProof.create("b.jpg", "b.jpg", "stored-b.jpg", ProofFileType.IMAGE,
          LocalDate.of(2026, 6, 2), receiptId));

      repository.deleteAllByReceiptId(receiptId);

      assertTrue(repository.findAllByReceiptId(receiptId).isEmpty());
    }
  }


  @Nested
  class CountByReceiptIds {

    @Test
    @DisplayName("When proofs exist, should return correct counts per receipt")
    void shouldReturnCorrectCountsPerReceipt() {
      Long receiptId1 = createReceipt();
      Long receiptId2 = createReceipt();
      repository.create(PaymentProof.create("a.pdf", "a.pdf", "stored-a.pdf", ProofFileType.PDF,
          LocalDate.of(2026, 6, 1), receiptId1));
      repository.create(PaymentProof.create("b.jpg", "b.jpg", "stored-b.jpg", ProofFileType.IMAGE,
          LocalDate.of(2026, 6, 2), receiptId1));

      Map<Long, Integer> counts = repository.countByReceiptIds(List.of(receiptId1, receiptId2));

      assertEquals(2, counts.get(receiptId1));
      assertEquals(0, counts.get(receiptId2));
    }

    @Test
    @DisplayName("When empty list provided, should return empty map")
    void shouldReturnEmptyMapWhenNoIds() {
      Map<Long, Integer> counts = repository.countByReceiptIds(List.of());

      assertTrue(counts.isEmpty());
    }
  }
}
