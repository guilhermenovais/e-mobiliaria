package com.guilherme.emobiliaria.shared.pdf.templates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.EnumMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ContractTemplateTest {

  @Nested
  class GetParameters {

    @Test
    @DisplayName("Should return sample values for all string Jasper parameters")
    void shouldReturnSampleParameters() {
      ContractTemplate template = new ContractTemplate(null);

      EnumMap<ContractTemplate.ContractParameters, Object> params = template.getParameters();

      assertEquals(18, params.size());
      assertEquals("CONTRATO DE LOCAÇÃO",
          params.get(ContractTemplate.ContractParameters.CONTRACT_TITLE));
      assertEquals("LOCADORA: Maria Souza, brasileira, casada, empresaria, CPF 111.222.333-44.",
          params.get(ContractTemplate.ContractParameters.LANDLORD_TEXT));
      assertEquals("LOCATARIOS: Joao Silva e Ana Lima, brasileiros, maiores e capazes.",
          params.get(ContractTemplate.ContractParameters.TENANTS_TEXT));
      assertEquals("Belo Horizonte, 1 de janeiro de 2026.",
          params.get(ContractTemplate.ContractParameters.CITY_AND_DATE_TEXT));
    }
  }

  @Nested
  class GetCollections {

    @Test
    @DisplayName("Should return sample text beans for contract title and signing texts")
    void shouldReturnSampleCollections() {
      ContractTemplate template = new ContractTemplate(null);

      EnumMap<ContractTemplate.ContractCollections, Collection<Object>> collections = template.getCollections();

      assertEquals(1, collections.size());
      assertEquals(2, collections.get(ContractTemplate.ContractCollections.SIGNING_TEXTS).size());
    }
  }
}
