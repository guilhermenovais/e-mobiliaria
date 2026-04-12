package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class TemplateFormatterTest {

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo", BrazilianState.SP);
  }

  private PhysicalPerson validPhysicalPerson() {
    return PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
        "529.982.247-25", "MG-1234567", validAddress());
  }

  private JuridicalPerson validJuridicalPerson() {
    return JuridicalPerson.create("Empresa LTDA", "11.222.333/0001-81",
        List.of(validPhysicalPerson()), validAddress());
  }

  @Nested
  class FormatCurrency {

    @ParameterizedTest(name = "{0} centavos should format as {1}")
    @CsvSource({
        "0, 'R$ 0,00'",
        "100, 'R$ 1,00'",
        "150000, 'R$ 1.500,00'",
        "100000, 'R$ 1.000,00'",
        "1000000, 'R$ 10.000,00'"
    })
    @DisplayName("When given centavos, should format as Brazilian currency string")
    void shouldFormatAsBrazilianCurrency(int centavos, String expected) {
      assertEquals(expected, TemplateFormatter.formatCurrency(centavos));
    }
  }

  @Nested
  class FormatCurrencyInFull {

    @ParameterizedTest(name = "{0} centavos should be {1}")
    @CsvSource({
        "0, 'zero reais'",
        "100, 'um real'",
        "200, 'dois reais'",
        "1500, 'quinze reais'",
        "10000, 'cem reais'",
        "150000, 'mil e quinhentos reais'",
        "200000, 'dois mil reais'",
        "100000, 'mil reais'"
    })
    @DisplayName("When given centavos, should return value in full Portuguese words")
    void shouldReturnValueInFullWords(int centavos, String expected) {
      assertEquals(expected, TemplateFormatter.formatCurrencyInFull(centavos));
    }
  }

  @Nested
  class FormatDate {

    @Test
    @DisplayName("When given a date, should format as dd/MM/yyyy")
    void shouldFormatAsDdMmYyyy() {
      assertEquals("05/03/2026", TemplateFormatter.formatDate(LocalDate.of(2026, 3, 5)));
    }
  }

  @Nested
  class FormatDateInFull {

    @ParameterizedTest(name = "Month {0} should be {1}")
    @CsvSource({
        "1, '1 de janeiro de 2026'",
        "2, '1 de fevereiro de 2026'",
        "3, '1 de março de 2026'",
        "4, '1 de abril de 2026'",
        "5, '1 de maio de 2026'",
        "6, '1 de junho de 2026'",
        "7, '1 de julho de 2026'",
        "8, '1 de agosto de 2026'",
        "9, '1 de setembro de 2026'",
        "10, '1 de outubro de 2026'",
        "11, '1 de novembro de 2026'",
        "12, '1 de dezembro de 2026'"
    })
    @DisplayName("When given a date, should format in full Portuguese")
    void shouldFormatInFullPortuguese(int month, String expected) {
      assertEquals(expected, TemplateFormatter.formatDateInFull(LocalDate.of(2026, month, 1)));
    }
  }

  @Nested
  class FormatPeriod {

    @Test
    @DisplayName("When period has only months, should format as X meses")
    void shouldFormatMonthsOnly() {
      assertEquals("12 meses", TemplateFormatter.formatPeriod(Period.ofMonths(12)));
    }

    @Test
    @DisplayName("When period has only one month, should use singular form")
    void shouldUseSingularForOneMonth() {
      assertEquals("1 mês", TemplateFormatter.formatPeriod(Period.ofMonths(1)));
    }

    @Test
    @DisplayName("When period has only years, should format as X anos")
    void shouldFormatYearsOnly() {
      assertEquals("2 anos", TemplateFormatter.formatPeriod(Period.ofYears(2)));
    }

    @Test
    @DisplayName("When period has only one year, should use singular form")
    void shouldUseSingularForOneYear() {
      assertEquals("1 ano", TemplateFormatter.formatPeriod(Period.ofYears(1)));
    }

    @Test
    @DisplayName("When period has years and months, should format both")
    void shouldFormatYearsAndMonths() {
      assertEquals("1 ano e 6 meses", TemplateFormatter.formatPeriod(Period.of(1, 6, 0)));
    }
  }

  @Nested
  class FormatAddress {

    @Test
    @DisplayName("When address has no complement, should format without it")
    void shouldFormatWithoutComplement() {
      Address address = Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo", BrazilianState.SP);

      String result = TemplateFormatter.formatAddress(address);

      assertEquals("Praça da Sé, 1, Sé, São Paulo - SP", result);
    }

    @Test
    @DisplayName("When address has complement, should include it")
    void shouldFormatWithComplement() {
      Address address = Address.create("01001000", "Praça da Sé", "1", "Apto 10", "Sé", "São Paulo", BrazilianState.SP);

      String result = TemplateFormatter.formatAddress(address);

      assertEquals("Praça da Sé, 1, Apto 10, Sé, São Paulo - SP", result);
    }
  }

  @Nested
  class FormatCpf {

    @Test
    @DisplayName("When given CPF digits, should format with mask")
    void shouldFormatWithMask() {
      assertEquals("529.982.247-25", TemplateFormatter.formatCpf("52998224725"));
    }
  }

  @Nested
  class FormatCnpj {

    @Test
    @DisplayName("When given CNPJ digits, should format with mask")
    void shouldFormatWithMask() {
      assertEquals("11.222.333/0001-81", TemplateFormatter.formatCnpj("11222333000181"));
    }
  }

  @Nested
  class CivilStateInPortuguese {

    @ParameterizedTest(name = "{0} should be {1}")
    @CsvSource({
        "SINGLE, solteiro",
        "MARRIED, casado",
        "DIVORCED, divorciado",
        "WIDOWER, viúvo"
    })
    @DisplayName("When given a civil state, should return Portuguese translation")
    void shouldReturnPortugueseTranslation(CivilState civilState, String expected) {
      assertEquals(expected, TemplateFormatter.civilStateInPortuguese(civilState));
    }
  }

  @Nested
  class PersonName {

    @Test
    @DisplayName("When person is a PhysicalPerson, should return their name")
    void shouldReturnPhysicalPersonName() {
      assertEquals("João Silva", TemplateFormatter.personName(validPhysicalPerson()));
    }

    @Test
    @DisplayName("When person is a JuridicalPerson, should return their corporate name")
    void shouldReturnJuridicalPersonCorporateName() {
      assertEquals("Empresa LTDA", TemplateFormatter.personName(validJuridicalPerson()));
    }
  }

  @Nested
  class PersonDescription {

    @Test
    @DisplayName("When person is a PhysicalPerson, should return formatted personal details")
    void shouldReturnPhysicalPersonDescription() {
      PhysicalPerson person = PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE,
          "Engenheiro", "529.982.247-25", "MG-1234567", validAddress());

      String result = TemplateFormatter.personDescription(person);

      assertEquals("Brasileiro, solteiro, Engenheiro, CPF 529.982.247-25, RG MG-1234567", result);
    }

    @Test
    @DisplayName("When person is a JuridicalPerson, should return CNPJ and representative")
    void shouldReturnJuridicalPersonDescription() {
      JuridicalPerson person = validJuridicalPerson();

      String result = TemplateFormatter.personDescription(person);

      assertEquals("CNPJ 11.222.333/0001-81, representada por João Silva", result);
    }
  }

  @Nested
  class PersonCity {

    @Test
    @DisplayName("When person is a PhysicalPerson, should return their city")
    void shouldReturnPhysicalPersonCity() {
      assertEquals("São Paulo", TemplateFormatter.personCity(validPhysicalPerson()));
    }

    @Test
    @DisplayName("When person is a JuridicalPerson, should return their city")
    void shouldReturnJuridicalPersonCity() {
      assertEquals("São Paulo", TemplateFormatter.personCity(validJuridicalPerson()));
    }
  }
}
