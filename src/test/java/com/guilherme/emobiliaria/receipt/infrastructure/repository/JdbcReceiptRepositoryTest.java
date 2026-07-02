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
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
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
import java.time.YearMonth;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcReceiptRepositoryTest {

  private JdbcReceiptRepository repository;
  private DataSource dataSource;
  private Contract contract;

  @BeforeEach
  void setUp() {
    String dbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    config.setPassword("");
    dataSource = new HikariDataSource(config);
    Flyway.configure().dataSource(dataSource).locations("classpath:db/migration").load().migrate();
    repository = new JdbcReceiptRepository(dataSource);
    contract = createContract();
  }

  private Contract createContract() {
    JdbcAddressRepository addressRepo = new JdbcAddressRepository(dataSource);
    JdbcPhysicalPersonRepository personRepo = new JdbcPhysicalPersonRepository(dataSource);
    JdbcPropertyRepository propertyRepo = new JdbcPropertyRepository(dataSource);
    JdbcPaymentAccountRepository paymentAccountRepo = new JdbcPaymentAccountRepository(dataSource);
    JdbcContractRepository contractRepo = new JdbcContractRepository(dataSource);

    Address address = addressRepo.create(
        Address.create("01001000", "Rua A", "1", null, "Centro", "São Paulo", BrazilianState.SP));
    PhysicalPerson person = personRepo.create(
        PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
            "529.982.247-25", "MG-1234567", address));
    Property property = propertyRepo.create(
        Property.create("Apto 1", "Apartamento", "1234567890", "0987654321", "IPTU-001", address));
    PaymentAccount paymentAccount = paymentAccountRepo.create(
        PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null));
    return contractRepo.create(
        Contract.create(LocalDate.of(2020, 1, 1), Period.ofYears(10), 10, 150000, "Residencial",
            paymentAccount, property, person, List.of(person), List.of(), List.of()));
  }

  private Receipt createReceipt(LocalDate date) {
    return repository.create(Receipt.create(date, date, date, date, 0, 0, null, contract));
  }

  @Nested
  class FindAllReceiptMonths {

    @Test
    @DisplayName(
        "When receipts exist across months, should return distinct months ordered most-recent-first")
    void shouldReturnDistinctMonthsOrderedMostRecentFirst() {
      createReceipt(LocalDate.of(2026, 5, 10));
      createReceipt(LocalDate.of(2026, 5, 20));
      createReceipt(LocalDate.of(2026, 7, 1));
      createReceipt(LocalDate.of(2026, 6, 15));

      List<YearMonth> months = repository.findAllReceiptMonths();

      assertEquals(List.of(YearMonth.of(2026, 7), YearMonth.of(2026, 6), YearMonth.of(2026, 5)),
          months);
    }

    @Test
    @DisplayName("When no receipts exist, should return empty list")
    void shouldReturnEmptyListWhenNoReceipts() {
      List<YearMonth> months = repository.findAllReceiptMonths();

      assertTrue(months.isEmpty());
    }
  }


  @Nested
  class FindAllByMonth {

    @Test
    @DisplayName(
        "When receipts fall within the month including boundary days, should return exactly those receipts")
    void shouldReturnReceiptsWithinMonthIncludingBoundaries() {
      createReceipt(LocalDate.of(2026, 6, 1));
      createReceipt(LocalDate.of(2026, 6, 15));
      createReceipt(LocalDate.of(2026, 6, 30));
      createReceipt(LocalDate.of(2026, 5, 31));
      createReceipt(LocalDate.of(2026, 7, 1));

      List<Receipt> result = repository.findAllByMonth(YearMonth.of(2026, 6));

      assertEquals(3, result.size());
      assertTrue(
          result.stream().allMatch(r -> YearMonth.from(r.getDate()).equals(YearMonth.of(2026, 6))));
    }

    @Test
    @DisplayName("When no receipts fall within the month, should return empty list")
    void shouldReturnEmptyListWhenNoneMatch() {
      createReceipt(LocalDate.of(2026, 5, 10));

      List<Receipt> result = repository.findAllByMonth(YearMonth.of(2026, 6));

      assertTrue(result.isEmpty());
    }
  }
}
