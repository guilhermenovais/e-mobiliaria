package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.entity.Purpose;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.time.LocalDate;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.stream.Collectors;

public class ContractTemplate extends PdfTemplate<ContractTemplate.ContractParameters, ContractTemplate.ContractCollections> {

  public enum ContractParameters {
    LANDLORD_NAME, LANDLORD_DESCRIPTION, PROPERTY_TYPE, PROPERTY_ADDRESS,
    PROPERTY_PURPOSE, PROPERTY_RENT_VALUE, PROPERTY_CEMIG, PROPERTY_IPTU,
    CONTRACT_PERIOD, CONTRACT_START_DATE, CONTRACT_END_DATE, CONTRACT_PAYMENT_METHOD,
    LANDLORD_CITY, CONTRACT_START_DATE_IN_FULL, LANDLORD_SIGNING, TENANT_SIGNING
  }

  public enum ContractCollections {
    TENANTS_LIST
  }

  private final Contract contract;

  public ContractTemplate(Contract contract) {
    super("contract");
    this.contract = contract;
  }

  @Override
  public EnumMap<ContractParameters, Object> getParameters() {
    EnumMap<ContractParameters, Object> params = new EnumMap<>(ContractParameters.class);
    Person landlord = contract.getLandlord();
    Property property = contract.getProperty();
    PaymentAccount account = contract.getPaymentAccount();
    LocalDate startDate = contract.getStartDate();
    LocalDate endDate = startDate.plus(contract.getDuration());

    params.put(ContractParameters.LANDLORD_NAME, TemplateFormatter.personName(landlord));
    params.put(ContractParameters.LANDLORD_DESCRIPTION, TemplateFormatter.personDescription(landlord));
    params.put(ContractParameters.PROPERTY_TYPE, property.getType());
    params.put(ContractParameters.PROPERTY_ADDRESS, TemplateFormatter.formatAddress(property.getAddress()));
    params.put(ContractParameters.PROPERTY_PURPOSE, purposeInPortuguese(property.getPurpose()));
    params.put(ContractParameters.PROPERTY_RENT_VALUE, TemplateFormatter.formatCurrency(property.getRent()));
    params.put(ContractParameters.PROPERTY_CEMIG, property.getCemig());
    params.put(ContractParameters.PROPERTY_IPTU, property.getIptu());
    params.put(ContractParameters.CONTRACT_PERIOD, TemplateFormatter.formatPeriod(contract.getDuration()));
    params.put(ContractParameters.CONTRACT_START_DATE, TemplateFormatter.formatDate(startDate));
    params.put(ContractParameters.CONTRACT_END_DATE, TemplateFormatter.formatDate(endDate));
    params.put(ContractParameters.CONTRACT_PAYMENT_METHOD, formatPaymentMethod(account));
    params.put(ContractParameters.LANDLORD_CITY, TemplateFormatter.personCity(landlord));
    params.put(ContractParameters.CONTRACT_START_DATE_IN_FULL, TemplateFormatter.formatDateInFull(startDate));
    params.put(ContractParameters.LANDLORD_SIGNING, TemplateFormatter.personName(landlord));
    params.put(ContractParameters.TENANT_SIGNING, contract.getTenants().stream()
        .map(TemplateFormatter::personName)
        .collect(Collectors.joining(", ")));
    return params;
  }

  @Override
  public EnumMap<ContractCollections, Collection<Object>> getCollections() {
    EnumMap<ContractCollections, Collection<Object>> collections = new EnumMap<>(ContractCollections.class);
    List<Object> tenantBeans = contract.getTenants().stream()
        .map(t -> (Object) new TenantBean(
            TemplateFormatter.personName(t),
            TemplateFormatter.personDescription(t)))
        .collect(Collectors.toList());
    collections.put(ContractCollections.TENANTS_LIST, tenantBeans);
    return collections;
  }

  private String formatPaymentMethod(PaymentAccount account) {
    StringBuilder sb = new StringBuilder();
    sb.append(account.getBank())
        .append(", Agência ").append(account.getBankBranch())
        .append(", Conta ").append(account.getAccountNumber());
    if (account.getPixKey() != null && !account.getPixKey().isBlank()) {
      sb.append(", PIX: ").append(account.getPixKey());
    }
    return sb.toString();
  }

  private String purposeInPortuguese(Purpose purpose) {
    return switch (purpose) {
      case RESIDENTIAL -> "Residencial";
      case COMMERCIAL -> "Comercial";
    };
  }
}
