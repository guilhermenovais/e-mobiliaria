package com.guilherme.emobiliaria.contract.infrastructure.repository;

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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JdbcContractRepositoryTest {

  private DataSource dataSource;
  private JdbcContractRepository repository;

  @BeforeEach
  void setUp() {
    String dbName = "testdb_" + UUID.randomUUID().toString().replace("-", "");
    HikariConfig config = new HikariConfig();
    config.setJdbcUrl("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1");
    config.setUsername("sa");
    config.setPassword("");
    dataSource = new HikariDataSource(config);
    Flyway.configure()
        .dataSource(dataSource)
        .locations("classpath:db/migration")
        .load()
        .migrate();
    repository = new JdbcContractRepository(dataSource);
  }

  // ─── DB seed helpers ────────────────────────────────────────────────────────

  private long insertAddress() throws SQLException {
    String sql = "INSERT INTO addresses (cep, address, number, complement, neighborhood, city, state) VALUES (?,?,?,?,?,?,?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, "30130010");
      stmt.setString(2, "Rua dos Testes");
      stmt.setString(3, "100");
      stmt.setString(4, null);
      stmt.setString(5, "Centro");
      stmt.setString(6, "Belo Horizonte");
      stmt.setString(7, "MG");
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertPhysicalPerson(long addressId, String cpf) throws SQLException {
    String sql = "INSERT INTO physical_persons (name, nationality, civil_state, occupation, cpf, id_card_number, address_id) VALUES (?,?,?,?,?,?,?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, "João Silva");
      stmt.setString(2, "Brasileiro");
      stmt.setString(3, "SINGLE");
      stmt.setString(4, "Engenheiro");
      stmt.setString(5, cpf);
      stmt.setString(6, "MG1234567");
      stmt.setLong(7, addressId);
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertJuridicalPerson(long representativeId, long addressId, String cnpj) throws SQLException {
    String sql = "INSERT INTO juridical_persons (corporate_name, cnpj, representative_id, address_id) VALUES (?,?,?,?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, "Empresa Teste LTDA");
      stmt.setString(2, cnpj);
      stmt.setLong(3, representativeId);
      stmt.setLong(4, addressId);
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertProperty(long addressId) throws SQLException {
    String sql = "INSERT INTO properties (name, type, purpose, rent, cemig, copasa, iptu, address_id) VALUES (?,?,?,?,?,?,?,?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, "Apartamento 101");
      stmt.setString(2, "Apartamento");
      stmt.setString(3, "RESIDENTIAL");
      stmt.setInt(4, 150000);
      stmt.setString(5, "CEMIG123");
      stmt.setString(6, "COPASA456");
      stmt.setString(7, "IPTU789");
      stmt.setLong(8, addressId);
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  private long insertPaymentAccount() throws SQLException {
    String sql = "INSERT INTO payment_accounts (bank, bank_branch, account_number, pix_key) VALUES (?,?,?,?)";
    try (Connection conn = dataSource.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
      stmt.setString(1, "Banco do Brasil");
      stmt.setString(2, "1234");
      stmt.setString(3, "56789-0");
      stmt.setString(4, null);
      stmt.executeUpdate();
      try (ResultSet keys = stmt.getGeneratedKeys()) {
        keys.next();
        return keys.getLong(1);
      }
    }
  }

  // ─── Entity builders ────────────────────────────────────────────────────────

  private Address buildAddress(long id) {
    return Address.restore(id, "30130010", "Rua dos Testes", "100", null, "Centro", "Belo Horizonte", BrazilianState.MG);
  }

  private PhysicalPerson buildPhysicalPerson(long id, long addressId, String cpf) {
    return PhysicalPerson.restore(id, "João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro", cpf, "MG1234567", buildAddress(addressId));
  }

  private PaymentAccount buildPaymentAccount(long id) {
    return PaymentAccount.restore(id, "Banco do Brasil", "1234", "56789-0", null);
  }

  private Property buildProperty(long id, long addressId) {
    return Property.restore(id, "Apartamento 101", "Apartamento", Purpose.RESIDENTIAL, 150000, "CEMIG123", "COPASA456", "IPTU789", buildAddress(addressId));
  }

  private Contract buildContract(PaymentAccount paymentAccount, Property property, Person landlord, List<Person> tenants) {
    return Contract.create(LocalDate.of(2024, 1, 1), Period.ofMonths(12), 5, paymentAccount, property, landlord, tenants);
  }

  // ─── Tests ─────────────────────────────────────────────────────────────────

  @Nested
  class Create {

    @Test
    void shouldReturnContractWithGeneratedId() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract contract = buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord));
      Contract created = repository.create(contract);

      assertNotNull(created.getId());
      assertTrue(created.getId() > 0);
    }

    @Test
    void shouldPersistAllFields() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract contract = buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord));
      Contract created = repository.create(contract);

      Optional<Contract> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertEquals(LocalDate.of(2024, 1, 1), found.get().getStartDate());
      assertEquals(Period.ofMonths(12), found.get().getDuration());
      assertEquals(5, found.get().getPaymentDay());
    }

    @Test
    void shouldPersistMultipleTenants() throws SQLException {
      long addrId = insertAddress();
      long personId1 = insertPhysicalPerson(addrId, "52998224725");
      long personId2 = insertPhysicalPerson(addrId, "11144477735");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson tenant1 = buildPhysicalPerson(personId1, addrId, "52998224725");
      PhysicalPerson tenant2 = buildPhysicalPerson(personId2, addrId, "11144477735");
      Contract contract = buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), tenant1, List.of(tenant1, tenant2));
      Contract created = repository.create(contract);

      Optional<Contract> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertEquals(2, found.get().getTenants().size());
    }

    @Test
    void shouldReconstructLandlordAsPhysicalPerson() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract created = repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));

      Optional<Contract> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertInstanceOf(PhysicalPerson.class, found.get().getLandlord());
    }

    @Test
    void shouldReconstructLandlordAsJuridicalPerson() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long juridicalId = insertJuridicalPerson(personId, addrId, "11222333000181");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson representative = buildPhysicalPerson(personId, addrId, "52998224725");
      JuridicalPerson landlord = JuridicalPerson.restore(juridicalId, "Empresa Teste LTDA", "11222333000181", representative, buildAddress(addrId));
      PhysicalPerson tenant = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract created = repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(tenant)));

      Optional<Contract> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertInstanceOf(JuridicalPerson.class, found.get().getLandlord());
    }
  }

  @Nested
  class Update {

    @Test
    void shouldUpdateAllFields() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract created = repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));

      long accountId2 = insertPaymentAccount();
      Contract updated = Contract.restore(created.getId(), LocalDate.of(2025, 6, 1), Period.ofYears(1), 10, buildPaymentAccount(accountId2), buildProperty(propertyId, addrId), landlord, List.of(landlord));
      repository.update(updated);

      Optional<Contract> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertEquals(LocalDate.of(2025, 6, 1), found.get().getStartDate());
      assertEquals(Period.ofYears(1), found.get().getDuration());
      assertEquals(10, found.get().getPaymentDay());
    }

    @Test
    void shouldReplaceTenantsOnUpdate() throws SQLException {
      long addrId = insertAddress();
      long personId1 = insertPhysicalPerson(addrId, "52998224725");
      long personId2 = insertPhysicalPerson(addrId, "11144477735");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson tenant1 = buildPhysicalPerson(personId1, addrId, "52998224725");
      PhysicalPerson tenant2 = buildPhysicalPerson(personId2, addrId, "11144477735");
      Contract created = repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), tenant1, List.of(tenant1, tenant2)));

      Contract updated = Contract.restore(created.getId(), created.getStartDate(), created.getDuration(), created.getPaymentDay(), buildPaymentAccount(accountId), buildProperty(propertyId, addrId), tenant1, List.of(tenant2));
      repository.update(updated);

      Optional<Contract> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertEquals(1, found.get().getTenants().size());
    }

    @Test
    void shouldThrowPersistenceExceptionWhenNotFound() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract nonExistent = Contract.restore(999L, LocalDate.of(2024, 1, 1), Period.ofMonths(12), 5, buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord));
      assertThrows(PersistenceException.class, () -> repository.update(nonExistent));
    }
  }

  @Nested
  class Delete {

    @Test
    void shouldDeleteContractWhenExists() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract created = repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));
      repository.delete(created.getId());

      assertTrue(repository.findById(created.getId()).isEmpty());
    }

    @Test
    void shouldCascadeDeleteTenants() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract created = repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));
      long contractId = created.getId();
      repository.delete(contractId);

      try (Connection conn = dataSource.getConnection();
          PreparedStatement stmt = conn.prepareStatement("SELECT COUNT(*) FROM contract_tenants WHERE contract_id=?")) {
        stmt.setLong(1, contractId);
        try (ResultSet rs = stmt.executeQuery()) {
          rs.next();
          assertEquals(0, rs.getInt(1));
        }
      }
    }

    @Test
    void shouldThrowPersistenceExceptionWhenNotFound() {
      assertThrows(PersistenceException.class, () -> repository.delete(999L));
    }
  }

  @Nested
  class FindById {

    @Test
    void shouldReturnContractWithFullGraph() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      Contract created = repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));

      Optional<Contract> found = repository.findById(created.getId());
      assertTrue(found.isPresent());
      assertNotNull(found.get().getPaymentAccount());
      assertNotNull(found.get().getProperty());
      assertNotNull(found.get().getProperty().getAddress());
      assertEquals("30130010", found.get().getProperty().getAddress().getCep());
      assertNotNull(found.get().getLandlord());
      assertNotNull(found.get().getTenants());
      assertEquals(1, found.get().getTenants().size());
    }

    @Test
    void shouldReturnEmptyWhenNotFound() {
      assertTrue(repository.findById(999L).isEmpty());
    }
  }

  @Nested
  class FindAll {

    @Test
    void shouldReturnEmptyPageWhenNoContractsExist() {
      PagedResult<Contract> result = repository.findAll(new PaginationInput(null, null));
      assertEquals(0, result.total());
      assertTrue(result.items().isEmpty());
    }

    @Test
    void shouldReturnAllContractsWhenNoLimitSpecified() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));

      PagedResult<Contract> result = repository.findAll(new PaginationInput(null, null));
      assertEquals(2, result.total());
      assertEquals(2, result.items().size());
    }

    @Test
    void shouldRespectLimitAndOffset() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));

      PagedResult<Contract> result = repository.findAll(new PaginationInput(2, 1));
      assertEquals(3, result.total());
      assertEquals(2, result.items().size());
    }

    @Test
    void shouldReturnCorrectTotal() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));

      PagedResult<Contract> result = repository.findAll(new PaginationInput(1, 0));
      assertEquals(2, result.total());
      assertEquals(1, result.items().size());
    }
  }

  @Nested
  class FindAllByPropertyId {

    @Test
    void shouldReturnOnlyContractsForGivenProperty() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long property1Id = insertProperty(addrId);
      long property2Id = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(property1Id, addrId), landlord, List.of(landlord)));
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(property2Id, addrId), landlord, List.of(landlord)));

      PagedResult<Contract> result = repository.findAllByPropertyId(property1Id, new PaginationInput(null, null));
      assertEquals(1, result.total());
      assertEquals(1, result.items().size());
      assertEquals(property1Id, result.items().get(0).getProperty().getId());
    }

    @Test
    void shouldReturnEmptyWhenPropertyHasNoContracts() throws SQLException {
      long addrId = insertAddress();
      long propertyId = insertProperty(addrId);

      PagedResult<Contract> result = repository.findAllByPropertyId(propertyId, new PaginationInput(null, null));
      assertEquals(0, result.total());
      assertTrue(result.items().isEmpty());
    }

    @Test
    void shouldRespectPagination() throws SQLException {
      long addrId = insertAddress();
      long personId = insertPhysicalPerson(addrId, "52998224725");
      long propertyId = insertProperty(addrId);
      long accountId = insertPaymentAccount();

      PhysicalPerson landlord = buildPhysicalPerson(personId, addrId, "52998224725");
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));
      repository.create(buildContract(buildPaymentAccount(accountId), buildProperty(propertyId, addrId), landlord, List.of(landlord)));

      PagedResult<Contract> result = repository.findAllByPropertyId(propertyId, new PaginationInput(2, 1));
      assertEquals(3, result.total());
      assertEquals(2, result.items().size());
    }
  }
}
