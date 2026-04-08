package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
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

  private Address landlordAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Address propertyAddress() {
    return Address.create("32605230", "Av. Dois", "200", null, "Vila das Flores", "Betim",
        BrazilianState.MG);
  }

  private PhysicalPerson landlord() {
    return PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
        "529.982.247-25", "MG-1234567", landlordAddress());
  }

  private PhysicalPerson tenant() {
    return PhysicalPerson.create("Maria Lima", "Brasileira", CivilState.MARRIED, "Advogada",
        "529.982.247-25", "SP-7654321", landlordAddress());
  }

  private Contract buildContract() {
    PaymentAccount account =
        PaymentAccount.create("Banco Inter", "0001", "0518639487", "64514507000146");
    Property property = Property.create("Escritório Centro", "Sala comercial", Purpose.COMMERCIAL,
        "CX 1 INSTALAÇÃO Nº3007687953", "MATRÍCULA Nº103765573", "000920280359002",
        propertyAddress());
    return Contract.create(LocalDate.of(2025, 7, 10), Period.ofMonths(12), 10, 98000, account,
        property, landlord(), List.of(tenant()), List.of(), List.of());
  }

  @Nested
  class GetParameters {

    @Test
    @DisplayName("When contract is provided, should return all 18 Jasper parameters")
    void shouldReturnAllParametersWhenContractIsProvided() {
      ContractTemplate template = new ContractTemplate(buildContract());

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals(18, params.size());
    }

    @Test
    @DisplayName("When contract is provided, should return static section titles")
    void shouldReturnStaticSectionTitlesWhenContractIsProvided() {
      ContractTemplate template = new ContractTemplate(buildContract());

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("CONTRATO DE LOCAÇÃO",
          params.get(ContractTemplate.ContractParameters.CONTRACT_TITLE));
      assertEquals("IMÓVEL OBJETO DESTA LOCAÇÃO:",
          params.get(ContractTemplate.ContractParameters.PROPERTY_SECTION_TITLE));
      assertEquals("PRAZO DESTA LOCAÇÃO:",
          params.get(ContractTemplate.ContractParameters.PERIOD_SECTION_TITLE));
      assertEquals("CLÁUSULAS CONTRATUAIS",
          params.get(ContractTemplate.ContractParameters.CONTRACTUAL_TERMS_SECTION_TITLE));
    }

    @Test
    @DisplayName(
        "When contract has a physical person landlord, should include landlord name in LANDLORD_TEXT")
    void shouldIncludeLandlordNameInLandlordTextWhenLandlordIsPhysicalPerson() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String landlordText =
          (String) template.getParameters().get(ContractTemplate.ContractParameters.LANDLORD_TEXT);

      assertTrue(landlordText.startsWith("<b>LOCADOR: </b>"));
      assertTrue(landlordText.contains("João Silva"));
      assertTrue(landlordText.contains("529.982.247-25"));
    }

    @Test
    @DisplayName("When contract has one tenant, should include tenant name in TENANTS_TEXT")
    void shouldIncludeTenantNameInTenantsTextWhenOneTenant() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String tenantsText =
          (String) template.getParameters().get(ContractTemplate.ContractParameters.TENANTS_TEXT);

      assertTrue(tenantsText.startsWith("<b>LOCATÁRIO(A): </b>"));
      assertTrue(tenantsText.contains("Maria Lima"));
    }

    @Test
    @DisplayName("When contract has rent set, should format rent in CONTRACT_RENT_TEXT")
    void shouldFormatRentInContractRentTextWhenRentIsSet() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String rentText = (String) template.getParameters()
          .get(ContractTemplate.ContractParameters.CONTRACT_RENT_TEXT);

      assertEquals("<b>Valor do aluguel mensal: </b>R$ 980,00 (novecentos e oitenta reais)",
          rentText);
    }

    @Test
    @DisplayName("When contract has 12-month duration, should show period in CONTRACT_PERIOD_TEXT")
    void shouldShowPeriodInContractPeriodTextWhen12MonthDuration() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String periodText = (String) template.getParameters()
          .get(ContractTemplate.ContractParameters.CONTRACT_PERIOD_TEXT);

      assertEquals("<b>Período: </b>12 (doze) meses", periodText);
    }

    @Test
    @DisplayName(
        "When contract starts on 2025-07-10 with 12 months, should set correct start and end dates")
    void shouldSetCorrectStartAndEndDatesWhenContractStartsOnJuly2025() {
      ContractTemplate template = new ContractTemplate(buildContract());

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals("<b>Início: </b>10 de julho de 2025",
          params.get(ContractTemplate.ContractParameters.CONTRACT_START_DATE_TEXT));
      assertEquals("<b>Término: </b>9 de julho de 2026",
          params.get(ContractTemplate.ContractParameters.CONTRACT_END_DATE_TEXT));
    }

    @Test
    @DisplayName(
        "When contract has payment account, should include bank details in CONTRACT_PAYMENT_METHOD_TEXT")
    void shouldIncludeBankDetailsInPaymentMethodTextWhenPaymentAccountIsSet() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String paymentText = (String) template.getParameters()
          .get(ContractTemplate.ContractParameters.CONTRACT_PAYMENT_METHOD_TEXT);

      assertEquals(
          "<b>Dia de pagamento: </b>10 (dez), conta para pagamento: Banco Inter, agência n° 0001, conta corrente n° 0518639487, chave PIX 64514507000146",
          paymentText);
    }

    @Test
    @DisplayName(
        "When contract landlord is in São Paulo, should use landlord city and start date in CITY_AND_DATE_TEXT")
    void shouldUseLandlordCityAndStartDateInCityAndDateTextWhenLandlordIsInSaoPaulo() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String cityAndDateText = (String) template.getParameters()
          .get(ContractTemplate.ContractParameters.CITY_AND_DATE_TEXT);

      assertEquals("São Paulo, 10 de julho de 2025.", cityAndDateText);
    }

    @Test
    @DisplayName(
        "When contract property is commercial, should show Comercial in PROPERTY_PURPOSE_TEXT")
    void shouldShowComercialInPropertyPurposeTextWhenPropertyIsCommercial() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String purposeText = (String) template.getParameters()
          .get(ContractTemplate.ContractParameters.PROPERTY_PURPOSE_TEXT);

      assertEquals("<b>Uso ou finalidade: </b>Comercial", purposeText);
    }

    @Test
    @DisplayName(
        "When contract property has CEMIG and COPASA, should include both in PROPERTY_CEMIG_TEXT")
    void shouldIncludeCemigAndCopasaInPropertyCemigTextWhenBothAreSet() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String cemigText = (String) template.getParameters()
          .get(ContractTemplate.ContractParameters.PROPERTY_CEMIG_TEXT);

      assertEquals("CEMIG: CX 1 INSTALAÇÃO Nº3007687953 - COPASA: MATRÍCULA Nº103765573",
          cemigText);
    }

    @Test
    @DisplayName("When contract property has IPTU, should include it in PROPERTY_IPTU_TEXT")
    void shouldIncludeIptuInPropertyIptuTextWhenIptuIsSet() {
      ContractTemplate template = new ContractTemplate(buildContract());

      String iptuText = (String) template.getParameters()
          .get(ContractTemplate.ContractParameters.PROPERTY_IPTU_TEXT);

      assertEquals("ÍNDICE CADASTRAL (IPTU): 000920280359002", iptuText);
    }
  }

  @Nested
  class GetCollections {

    @Test
    @DisplayName("When contract has one landlord and one tenant, should return two signing entries")
    void shouldReturnTwoSigningEntriesWhenOneLocalordAndOneTenant() {
      ContractTemplate template = new ContractTemplate(buildContract());

      EnumMap<ContractTemplate.ContractCollections, Collection<Object>> collections = template.getCollections();

      assertEquals(1, collections.size());
      assertEquals(2, collections.get(ContractTemplate.ContractCollections.SIGNING_TEXTS).size());
    }

    @Test
    @DisplayName(
        "When contract has one landlord and two tenants, should return three signing entries")
    void shouldReturnThreeSigningEntriesWhenOneLandlordAndTwoTenants() {
      PaymentAccount account = PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
      Property property = Property.create("Casa", "Casa", Purpose.RESIDENTIAL, "111", "222", "333",
          propertyAddress());
      Contract contract =
          Contract.create(LocalDate.of(2025, 1, 1), Period.ofMonths(6), 5, 100000, account,
              property, landlord(), List.of(tenant(), tenant()), List.of(), List.of());
      ContractTemplate template = new ContractTemplate(contract);

      Collection<Object> signingTexts =
          template.getCollections().get(ContractTemplate.ContractCollections.SIGNING_TEXTS);

      assertEquals(3, signingTexts.size());
    }
  }
}
