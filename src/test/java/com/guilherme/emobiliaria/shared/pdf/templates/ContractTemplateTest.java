package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractTemplateTest {

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo", BrazilianState.SP);
  }

  private PhysicalPerson validLandlord() {
    return PhysicalPerson.create("Maria Souza", "Brasileira", CivilState.MARRIED,
        "Empresária", "529.982.247-25", "RG-9876543", validAddress());
  }

  private PhysicalPerson validTenant() {
    return PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE,
        "Engenheiro", "529.982.247-25", "MG-1234567", validAddress());
  }

  private Property validProperty() {
    return Property.create("Apto Centro", "Apartamento", Purpose.RESIDENTIAL, 150000,
        "CEMIG-001", "COPASA-001", "IPTU-001", validAddress());
  }

  private PaymentAccount validPaymentAccount() {
    return PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
  }

  private Contract validContract(PhysicalPerson landlord, PhysicalPerson tenant) {
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10,
        validPaymentAccount(), validProperty(), landlord, List.of(tenant));
  }

  @Nested
  class GetParameters {

    @Test
    @DisplayName("When landlord is a PhysicalPerson, should return landlord name")
    void shouldReturnLandlordName() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Maria Souza", params.get(ContractTemplate.ContractParameters.LANDLORD_NAME));
    }

    @Test
    @DisplayName("When landlord is a PhysicalPerson, should return formatted landlord description")
    void shouldReturnLandlordDescription() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Brasileira, casado, Empresária, CPF 529.982.247-25, RG RG-9876543",
          params.get(ContractTemplate.ContractParameters.LANDLORD_DESCRIPTION));
    }

    @Test
    @DisplayName("When given a contract, should return property type")
    void shouldReturnPropertyType() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Apartamento", params.get(ContractTemplate.ContractParameters.PROPERTY_TYPE));
    }

    @Test
    @DisplayName("When given a contract, should return formatted property address")
    void shouldReturnPropertyAddress() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Praça da Sé, 1, Sé, São Paulo - SP",
          params.get(ContractTemplate.ContractParameters.PROPERTY_ADDRESS));
    }

    @Test
    @DisplayName("When property purpose is RESIDENTIAL, should return Residencial")
    void shouldReturnResidentialPurpose() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Residencial", params.get(ContractTemplate.ContractParameters.PROPERTY_PURPOSE));
    }

    @Test
    @DisplayName("When given a contract, should return formatted property rent value")
    void shouldReturnPropertyRentValue() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("R$ 1.500,00", params.get(ContractTemplate.ContractParameters.PROPERTY_RENT_VALUE));
    }

    @Test
    @DisplayName("When given a contract, should return property CEMIG")
    void shouldReturnPropertyCemig() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("CEMIG-001", params.get(ContractTemplate.ContractParameters.PROPERTY_CEMIG));
    }

    @Test
    @DisplayName("When given a contract, should return property IPTU")
    void shouldReturnPropertyIptu() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("IPTU-001", params.get(ContractTemplate.ContractParameters.PROPERTY_IPTU));
    }

    @Test
    @DisplayName("When given a contract with 12 months duration, should return formatted period")
    void shouldReturnContractPeriod() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("12 meses", params.get(ContractTemplate.ContractParameters.CONTRACT_PERIOD));
    }

    @Test
    @DisplayName("When given a contract, should return formatted start date")
    void shouldReturnContractStartDate() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("01/01/2026", params.get(ContractTemplate.ContractParameters.CONTRACT_START_DATE));
    }

    @Test
    @DisplayName("When given a contract with 12 months duration, should return formatted end date")
    void shouldReturnContractEndDate() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("01/01/2027", params.get(ContractTemplate.ContractParameters.CONTRACT_END_DATE));
    }

    @Test
    @DisplayName("When payment account has no PIX key, should return formatted payment method without PIX")
    void shouldReturnPaymentMethodWithoutPix() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Banco do Brasil, Agência 1234-5, Conta 12345-6",
          params.get(ContractTemplate.ContractParameters.CONTRACT_PAYMENT_METHOD));
    }

    @Test
    @DisplayName("When payment account has a PIX key, should include PIX in payment method")
    void shouldReturnPaymentMethodWithPix() {
      PaymentAccount accountWithPix = PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", "pix@key.com");
      Contract contract = Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10,
          accountWithPix, validProperty(), validLandlord(), List.of(validTenant()));
      ContractTemplate template = new ContractTemplate(contract);

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Banco do Brasil, Agência 1234-5, Conta 12345-6, PIX: pix@key.com",
          params.get(ContractTemplate.ContractParameters.CONTRACT_PAYMENT_METHOD));
    }

    @Test
    @DisplayName("When landlord is a PhysicalPerson, should return landlord city")
    void shouldReturnLandlordCity() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("São Paulo", params.get(ContractTemplate.ContractParameters.LANDLORD_CITY));
    }

    @Test
    @DisplayName("When given a contract, should return start date in full Portuguese")
    void shouldReturnStartDateInFull() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("1 de janeiro de 2026",
          params.get(ContractTemplate.ContractParameters.CONTRACT_START_DATE_IN_FULL));
    }

    @Test
    @DisplayName("When landlord is a PhysicalPerson, should return landlord signing name")
    void shouldReturnLandlordSigning() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Maria Souza", params.get(ContractTemplate.ContractParameters.LANDLORD_SIGNING));
    }

    @Test
    @DisplayName("When contract has one tenant, should return tenant name as signing")
    void shouldReturnTenantSigning() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("João Silva", params.get(ContractTemplate.ContractParameters.TENANT_SIGNING));
    }

    @Test
    @DisplayName("When contract has multiple tenants, should return names joined by comma")
    void shouldReturnMultipleTenantSignings() {
      PhysicalPerson tenant2 = PhysicalPerson.create("Ana Lima", "Brasileira", CivilState.MARRIED,
          "Médica", "529.982.247-25", "SP-9999999", validAddress());
      Contract contract = Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10,
          validPaymentAccount(), validProperty(), validLandlord(), List.of(validTenant(), tenant2));
      ContractTemplate template = new ContractTemplate(contract);

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("João Silva, Ana Lima", params.get(ContractTemplate.ContractParameters.TENANT_SIGNING));
    }

    @Test
    @DisplayName("When landlord is a JuridicalPerson, should return corporate name")
    void shouldReturnJuridicalLandlordName() {
      JuridicalPerson juridicalLandlord = JuridicalPerson.create("Empresa LTDA", "11.222.333/0001-81",
          validLandlord(), validAddress());
      Contract contract = Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10,
          validPaymentAccount(), validProperty(), juridicalLandlord, List.of(validTenant()));
      ContractTemplate template = new ContractTemplate(contract);

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("Empresa LTDA", params.get(ContractTemplate.ContractParameters.LANDLORD_NAME));
    }
  }

  @Nested
  class GetCollections {

    @Test
    @DisplayName("When given a contract, should return tenant beans in TENANTS_LIST collection")
    void shouldReturnTenantBeans() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractCollections, Collection<Object>> collections = template.getCollections();

      Collection<Object> tenantBeans = collections.get(ContractTemplate.ContractCollections.TENANTS_LIST);
      assertEquals(1, tenantBeans.size());
      TenantBean bean = (TenantBean) tenantBeans.iterator().next();
      assertEquals("João Silva", bean.getTenantIdentification());
    }

    @Test
    @DisplayName("When contract has multiple tenants, should return all tenant beans")
    void shouldReturnAllTenantBeans() {
      PhysicalPerson tenant2 = PhysicalPerson.create("Ana Lima", "Brasileira", CivilState.MARRIED,
          "Médica", "529.982.247-25", "SP-9999999", validAddress());
      Contract contract = Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10,
          validPaymentAccount(), validProperty(), validLandlord(), List.of(validTenant(), tenant2));
      ContractTemplate template = new ContractTemplate(contract);

      EnumMap<ContractTemplate.ContractCollections, Collection<Object>> collections = template.getCollections();

      Collection<Object> tenantBeans = collections.get(ContractTemplate.ContractCollections.TENANTS_LIST);
      assertEquals(2, tenantBeans.size());
    }

    @Test
    @DisplayName("When given a contract, tenant bean should contain correct description")
    void shouldReturnTenantBeanWithCorrectDescription() {
      ContractTemplate template = new ContractTemplate(validContract(validLandlord(), validTenant()));

      EnumMap<ContractTemplate.ContractCollections, Collection<Object>> collections = template.getCollections();

      TenantBean bean = (TenantBean) collections.get(ContractTemplate.ContractCollections.TENANTS_LIST).iterator().next();
      assertTrue(bean.getTenantDescription().contains("529.982.247-25"));
    }
  }
}
