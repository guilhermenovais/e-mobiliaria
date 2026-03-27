package com.guilherme.emobiliaria.contract.infrastructure.repository;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.shared.exception.PersistenceException;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcPaymentAccountRepositoryTest {

  private JdbcPaymentAccountRepository repository;

  @BeforeEach
  void setUp() {
    String dbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    config.setPassword("");
    DataSource dataSource = new HikariDataSource(config);
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate();
    repository = new JdbcPaymentAccountRepository(dataSource);
  }

  private PaymentAccount sampleAccount() {
    return PaymentAccount.create("Banco do Brasil", "1234", "56789-0", "chave@pix.com");
  }

  @Nested
  class Create {

    @Test
    void shouldReturnPaymentAccountWithGeneratedId() {
      PaymentAccount account = sampleAccount();
      PaymentAccount created = repository.create(account);
      assertNotNull(created.getId());
      assertTrue(created.getId() > 0);
    }

    @Test
    void shouldPersistAllFields() {
      PaymentAccount created = repository.create(sampleAccount());
      Optional<PaymentAccount> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertEquals("Banco do Brasil", found.get().getBank());
      assertEquals("1234", found.get().getBankBranch());
      assertEquals("56789-0", found.get().getAccountNumber());
      assertEquals("chave@pix.com", found.get().getPixKey());
    }

    @Test
    void shouldPersistNullPixKey() {
      PaymentAccount account = PaymentAccount.create("Banco do Brasil", "1234", "56789-0", null);
      PaymentAccount created = repository.create(account);
      Optional<PaymentAccount> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertNull(found.get().getPixKey());
    }
  }

  @Nested
  class Update {

    @Test
    void shouldUpdateAllFields() {
      PaymentAccount created = repository.create(sampleAccount());
      PaymentAccount updated = PaymentAccount.restore(created.getId(), "Itaú", "9999", "00001-1", "novo@pix.com");
      repository.update(updated);
      Optional<PaymentAccount> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertEquals("Itaú", found.get().getBank());
      assertEquals("9999", found.get().getBankBranch());
      assertEquals("00001-1", found.get().getAccountNumber());
      assertEquals("novo@pix.com", found.get().getPixKey());
    }

    @Test
    void shouldThrowPersistenceExceptionWhenNotFound() {
      PaymentAccount nonExistent = PaymentAccount.restore(999L, "Banco", "001", "00001-1", null);
      assertThrows(PersistenceException.class, () -> repository.update(nonExistent));
    }
  }

  @Nested
  class Delete {

    @Test
    void shouldDeleteWhenExists() {
      PaymentAccount created = repository.create(sampleAccount());
      repository.delete(created.getId());
      assertTrue(repository.findById(created.getId()).isEmpty());
    }

    @Test
    void shouldThrowPersistenceExceptionWhenNotFound() {
      assertThrows(PersistenceException.class, () -> repository.delete(999L));
    }
  }

  @Nested
  class FindById {

    @Test
    void shouldReturnPaymentAccountWhenFound() {
      PaymentAccount created = repository.create(sampleAccount());
      Optional<PaymentAccount> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertEquals(created.getId(), found.get().getId());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
      Optional<PaymentAccount> found = repository.findById(999L);
      assertTrue(found.isEmpty());
    }
  }

  @Nested
  class FindAll {

    @Test
    void shouldReturnEmptyPageWhenNoAccountsExist() {
      PagedResult<PaymentAccount> result = repository.findAll(new PaginationInput(null, null));
      assertEquals(0, result.total());
      assertTrue(result.items().isEmpty());
    }

    @Test
    void shouldReturnAllAccountsWhenNoLimitSpecified() {
      repository.create(sampleAccount());
      repository.create(PaymentAccount.create("Caixa", "0001", "11111-1", null));
      PagedResult<PaymentAccount> result = repository.findAll(new PaginationInput(null, null));
      assertEquals(2, result.total());
      assertEquals(2, result.items().size());
    }

    @Test
    void shouldRespectLimit() {
      repository.create(sampleAccount());
      repository.create(PaymentAccount.create("Caixa", "0001", "11111-1", null));
      repository.create(PaymentAccount.create("Itaú", "0002", "22222-2", null));
      PagedResult<PaymentAccount> result = repository.findAll(new PaginationInput(2, 0));
      assertEquals(3, result.total());
      assertEquals(2, result.items().size());
    }

    @Test
    void shouldRespectOffset() {
      repository.create(sampleAccount());
      repository.create(PaymentAccount.create("Caixa", "0001", "11111-1", null));
      repository.create(PaymentAccount.create("Itaú", "0002", "22222-2", null));
      PagedResult<PaymentAccount> result = repository.findAll(new PaginationInput(10, 2));
      assertEquals(3, result.total());
      assertEquals(1, result.items().size());
    }

    @Test
    void shouldReturnCorrectTotal() {
      repository.create(sampleAccount());
      repository.create(PaymentAccount.create("Caixa", "0001", "11111-1", null));
      PagedResult<PaymentAccount> result = repository.findAll(new PaginationInput(1, 0));
      assertEquals(2, result.total());
      assertEquals(1, result.items().size());
    }
  }
}
