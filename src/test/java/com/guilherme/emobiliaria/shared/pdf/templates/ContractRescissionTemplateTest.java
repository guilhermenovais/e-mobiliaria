package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractRescissionTemplateTest {

  private ResourceBundle bundle() {
    return ResourceBundle.getBundle("messages", Locale.forLanguageTag("pt-BR"));
  }

  private Address landlordAddress() {
    return Address.create("31515252", "Rua Doutor Alvaro Camargos", "1790B", null,
        "Sao Joao Batista (Venda Nova)", "Belo Horizonte", BrazilianState.MG);
  }

  private Address representativeAddress() {
    return Address.create("31515251", "Rua Um", "1790B", "Bloco B Apto 1606",
        "Alterosa Segunda Secao", "Belo Horizonte", BrazilianState.ES);
  }

  private Address tenantAddress() {
    return Address.create("31515252", "Rua Doutor Alvaro Camargos", "1231", "Casa",
        "Sao Joao Batista (Venda Nova)", "Belo Horizonte", BrazilianState.MG);
  }

  private Contract buildContract() {
    PhysicalPerson representative =
        PhysicalPerson.create("Representante Locadora de Souza", "Brasileiro", CivilState.SINGLE,
            "Desenvolvedor de Software", "101.970.506-03", "mg18935795", representativeAddress());
    JuridicalPerson landlord =
        JuridicalPerson.create("Locadora LTDA", "82.512.084/0001-07", List.of(representative),
            landlordAddress());
    Person tenant =
        PhysicalPerson.create("Locatario da Silva", "Brasileiro", CivilState.SINGLE, "Pedreiro",
            "564.238.240-37", "mg18231321", tenantAddress());

    PaymentAccount account = PaymentAccount.create("Banco Inter", "0001", "0518639487", null);
    Property property = Property.create("Casa 1", "Casa", "111", "222", "333", tenantAddress());

    return Contract.create(LocalDate.of(2025, 1, 1), Period.ofMonths(12), 10, 150000, "Residencial",
        account, property, landlord, List.of(tenant), List.of(), List.of());
  }

  @Test
  @DisplayName("Should not leave trailing periods after CEP in rescission opening text")
  void shouldNotLeaveTrailingPeriodsAfterCepInOpeningText() {
    ContractRescissionTemplate template =
        new ContractRescissionTemplate(buildContract(), LocalDate.of(2025, 6, 1), bundle());

    String noticeText =
        (String) template.getParameters().get(ContractRescissionTemplate.Parameters.NOTICE_TEXT);

    assertFalse(noticeText.contains(". e "));
    assertFalse(noticeText.contains("., doravante"));
    assertTrue(noticeText.contains("CEP 31515-251 e"));
    assertTrue(noticeText.contains("CEP 31515-252, doravante"));
  }
}

