package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContractTemplate
    extends PdfTemplate<ContractTemplate.ContractParameters, ContractTemplate.ContractCollections> {

  private final Contract contract;
  private final ResourceBundle bundle;
  private final TemplateFormatter formatter;

  public ContractTemplate(Contract contract) {
    this(contract, ResourceBundle.getBundle("messages", Locale.forLanguageTag("pt-BR")));
  }

  public ContractTemplate(Contract contract, ResourceBundle bundle) {
    super("contract");
    this.contract = contract;
    this.bundle = bundle;
    this.formatter = new TemplateFormatter(bundle);
  }

  @Override
  public EnumMap<ContractParameters, Object> getParameters() {
    Property property = contract.getProperty();
    PaymentAccount account = contract.getPaymentAccount();
    LocalDate endDate = contract.getStartDate().plus(contract.getDuration()).minusDays(1);

    String tenantLabel = bundle.getString("pdf.contract.role.tenant");
    String guarantorLabel = bundle.getString("pdf.contract.role.guarantor");

    String tenantsText = buildOrdinalPartiesText(contract.getTenants(), tenantLabel,
        formatter::formatPersonForContract);
    String guarantorsText = buildOrdinalPartiesText(contract.getGuarantors(), guarantorLabel,
        formatter::formatPersonForContract);
    String allPartiesText = tenantsText + (guarantorsText.isEmpty() ? "" : "<br>" + guarantorsText);

    String paymentMethodText = bold(bundle.getString(
        "pdf.contract.payment_day_label") + " ") + contract.getPaymentDay() + " (" + formatter.numberInWords(
        contract.getPaymentDay()) + "), " + bundle.getString(
        "pdf.contract.payment_account") + " " + account.getBank() + bundle.getString(
        "pdf.contract.bank_branch") + " " + account.getBankBranch() + bundle.getString(
        "pdf.contract.account_number") + " " + account.getAccountNumber() + (account.getPixKey() != null ?
        bundle.getString("pdf.contract.pix_key") + " " + account.getPixKey() :
        "");

    EnumMap<ContractParameters, Object> params = new EnumMap<>(ContractParameters.class);
    params.put(ContractParameters.CONTRACT_TITLE, bundle.getString("pdf.contract.title"));
    params.put(ContractParameters.LANDLORD_TEXT, bold(
        bundle.getString("pdf.contract.landlord_label") + " ") + formatter.formatPersonForContract(
        contract.getLandlord()));
    params.put(ContractParameters.TENANTS_TEXT, allPartiesText);
    params.put(ContractParameters.PROPERTY_SECTION_TITLE,
        bundle.getString("pdf.contract.property_section"));
    params.put(ContractParameters.PROPERTY_TYPE_TEXT,
        bold(bundle.getString("pdf.contract.type_label") + " ") + property.getType());
    params.put(ContractParameters.PROPERTY_ADDRESS_TEXT, bold(
        bundle.getString("pdf.contract.address_label") + " ") + formatter.formatAddressForContract(
        property.getAddress()));
    params.put(ContractParameters.PROPERTY_PURPOSE_TEXT,
        bold(bundle.getString("pdf.contract.purpose_label") + " ") + contract.getPurpose());
    params.put(ContractParameters.CONTRACT_RENT_TEXT,
        bold(bundle.getString("pdf.contract.rent_label") + " ") + TemplateFormatter.formatCurrency(
            contract.getRent()) + " (" + formatter.formatCurrencyInFull(contract.getRent()) + ")");
    params.put(ContractParameters.PROPERTY_CEMIG_TEXT,
        "CEMIG: " + property.getCemig() + " - COPASA: " + property.getCopasa());
    params.put(ContractParameters.PROPERTY_IPTU_TEXT,
        bundle.getString("pdf.contract.iptu_label") + " " + property.getIptu());
    params.put(ContractParameters.PERIOD_SECTION_TITLE,
        bundle.getString("pdf.contract.period_section"));
    params.put(ContractParameters.CONTRACT_PERIOD_TEXT, bold(
        bundle.getString("pdf.contract.period_label") + " ") + formatter.formatPeriodForContract(
        contract.getDuration()));
    params.put(ContractParameters.CONTRACT_START_DATE_TEXT,
        bold(bundle.getString("pdf.contract.start_label") + " ") + formatter.formatDateInFull(
            contract.getStartDate()));
    params.put(ContractParameters.CONTRACT_END_DATE_TEXT,
        bold(bundle.getString("pdf.contract.end_label") + " ") + formatter.formatDateInFull(
            endDate));
    params.put(ContractParameters.CONTRACT_PAYMENT_METHOD_TEXT, paymentMethodText);
    params.put(ContractParameters.CONTRACTUAL_TERMS_SECTION_TITLE,
        bundle.getString("pdf.contract.terms_section"));
    params.put(ContractParameters.CONTRACTUAL_TERMS_TEXT,
        bundle.getString("pdf.contract.terms_text"));
    params.put(ContractParameters.CITY_AND_DATE_TEXT,
        TemplateFormatter.personCity(contract.getLandlord()) + ", " + formatter.formatDateInFull(
            contract.getStartDate()) + ".");
    return params;
  }

  @Override
  public EnumMap<ContractCollections, Collection<Object>> getCollections() {
    List<Object> signingEntries = new ArrayList<>();
    addSigningEntries(signingEntries, List.of(contract.getLandlord()),
        bundle.getString("pdf.contract.role.landlord"));
    addSigningEntries(signingEntries, contract.getTenants(),
        bundle.getString("pdf.contract.role.tenant"));
    addSigningEntries(signingEntries, contract.getGuarantors(),
        bundle.getString("pdf.contract.role.guarantor"));
    addSigningEntries(signingEntries, contract.getWitnesses(),
        bundle.getString("pdf.contract.role.witness"));

    EnumMap<ContractCollections, Collection<Object>> collections =
        new EnumMap<>(ContractCollections.class);
    collections.put(ContractCollections.SIGNING_TEXTS, signingEntries);
    return collections;
  }

  private void addSigningEntries(List<Object> signingEntries, List<Person> people,
      String roleLabel) {
    boolean shouldShowOrdinal = people.size() > 1;
    IntStream.range(0, people.size()).forEach(i -> {
      String indexedRoleLabel = shouldShowOrdinal ? roleLabel + " " + (i + 1) : roleLabel;
      String text = bold(indexedRoleLabel + ": " + signingText(people.get(i)));
      signingEntries.add(new TextBean(text));
    });
  }

  private String signingText(Person person) {
    if (person instanceof PhysicalPerson pp) {
      return pp.getName() + ", CPF: " + TemplateFormatter.formatCpf(pp.getCpf());
    }
    if (person instanceof JuridicalPerson jp) {
      return jp.getCorporateName() + ", CNPJ: " + TemplateFormatter.formatCnpj(jp.getCnpj());
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  private <T> String buildOrdinalPartiesText(List<T> parties, String label,
      Function<T, String> formatter) {
    boolean shouldShowOrdinal = parties.size() > 1;
    return IntStream.range(0, parties.size()).mapToObj(i -> {
      String indexedLabel = shouldShowOrdinal ? label + " " + (i + 1) : label;
      return bold(indexedLabel + ": ") + formatter.apply(parties.get(i));
    }).collect(Collectors.joining("<br>"));
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
