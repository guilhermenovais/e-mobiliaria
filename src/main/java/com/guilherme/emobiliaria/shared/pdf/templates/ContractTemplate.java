package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public class ContractTemplate
    extends PdfTemplate<ContractTemplate.ContractParameters, ContractTemplate.ContractCollections> {

  @SuppressWarnings("unused")
  private final Contract contract;

  @Override
  public EnumMap<ContractParameters, Object> getParameters() {
    EnumMap<ContractParameters, Object> params = new EnumMap<>(ContractParameters.class);
    params.put(ContractParameters.CONTRACT_TITLE, "CONTRATO DE LOCAÇÃO");
    params.put(ContractParameters.LANDLORD_TEXT,
        "LOCADORA: Maria Souza, brasileira, casada, empresaria, CPF 111.222.333-44.");
    params.put(ContractParameters.TENANTS_TEXT,
        "LOCATARIOS: Joao Silva e Ana Lima, brasileiros, maiores e capazes.");
    params.put(ContractParameters.PROPERTY_SECTION_TITLE, "1) DO IMOVEL");
    params.put(ContractParameters.PROPERTY_TYPE_TEXT, "Tipo: Apartamento");
    params.put(ContractParameters.PROPERTY_ADDRESS_TEXT,
        "Endereco: Rua das Flores, 100, Centro, Belo Horizonte - MG");
    params.put(ContractParameters.PROPERTY_PURPOSE_TEXT, "Finalidade: Residencial");
    params.put(ContractParameters.CONTRACT_RENT_TEXT, "Aluguel mensal: R$ 1.500,00");
    params.put(ContractParameters.PROPERTY_CEMIG_TEXT, "CEMIG: 123456789");
    params.put(ContractParameters.PROPERTY_IPTU_TEXT, "IPTU: 2026-000123");
    params.put(ContractParameters.PERIOD_SECTION_TITLE, "2) DO PRAZO E PAGAMENTO");
    params.put(ContractParameters.CONTRACT_PERIOD_TEXT, "Prazo: 12 meses");
    params.put(ContractParameters.CONTRACT_START_DATE_TEXT, "Inicio: 01/01/2026");
    params.put(ContractParameters.CONTRACT_END_DATE_TEXT, "Termino: 01/01/2027");
    params.put(ContractParameters.CONTRACT_PAYMENT_METHOD_TEXT,
        "Pagamento via Banco do Brasil, Agencia 1234-5, Conta 12345-6, PIX: pix@exemplo.com");
    params.put(ContractParameters.CONTRACTUAL_TERMS_SECTION_TITLE, "3) DAS CONDICOES GERAIS");
    params.put(ContractParameters.CONTRACTUAL_TERMS_TEXT,
        "O(a) LOCATARIO(a) se compromete a manter o imovel em bom estado de conservacao e uso.");
    params.put(ContractParameters.CITY_AND_DATE_TEXT, "Belo Horizonte, 1 de janeiro de 2026.");
    return params;
  }

  @Override
  public EnumMap<ContractCollections, Collection<Object>> getCollections() {
    EnumMap<ContractCollections, Collection<Object>> collections =
        new EnumMap<>(ContractCollections.class);
    collections.put(ContractCollections.SIGNING_TEXTS,
        List.of(new TextBean("Maria Souza - LOCADORA"),
            new TextBean("Joao Silva e Ana Lima - LOCATARIOS")));
    return collections;
  }

  public ContractTemplate(Contract contract) {
    super("contract");
    this.contract = contract;
  }

  public enum ContractParameters {
    CONTRACT_TITLE, LANDLORD_TEXT, TENANTS_TEXT, PROPERTY_SECTION_TITLE, PROPERTY_TYPE_TEXT, PROPERTY_ADDRESS_TEXT, PROPERTY_PURPOSE_TEXT, CONTRACT_RENT_TEXT, PROPERTY_CEMIG_TEXT, PROPERTY_IPTU_TEXT, PERIOD_SECTION_TITLE, CONTRACT_PERIOD_TEXT, CONTRACT_START_DATE_TEXT, CONTRACT_END_DATE_TEXT, CONTRACT_PAYMENT_METHOD_TEXT, CONTRACTUAL_TERMS_SECTION_TITLE, CONTRACTUAL_TERMS_TEXT, CITY_AND_DATE_TEXT
  }

  public enum ContractCollections {
    SIGNING_TEXTS
  }


  public record TextBean(String text) {
    public String getText() {
      return text;
    }
  }
}
