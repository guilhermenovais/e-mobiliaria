package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
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
import java.util.ResourceBundle;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ContractRescissionTemplate extends
    PdfTemplate<ContractRescissionTemplate.Parameters, ContractRescissionTemplate.Collections> {

  private final Contract contract;
  private final LocalDate noticeDate;
  private final ResourceBundle bundle;
  private final TemplateFormatter formatter;

  public ContractRescissionTemplate(Contract contract, LocalDate noticeDate,
      ResourceBundle bundle) {
    super("contract_rescission");
    this.contract = contract;
    this.noticeDate = noticeDate;
    this.bundle = bundle;
    this.formatter = new TemplateFormatter(bundle);
  }

  @Override
  public EnumMap<Parameters, Object> getParameters() {
    EnumMap<Parameters, Object> params = new EnumMap<>(Parameters.class);
    String cityAndDate =
        TemplateFormatter.personCity(contract.getLandlord()) + ", " + formatter.formatDateInFull(
            noticeDate) + ".";
    params.put(Parameters.NOTICE_TITLE, bundle.getString("pdf.contract_notice.rescission.title"));
    params.put(Parameters.NOTICE_TEXT, buildRescissionText());
    params.put(Parameters.CITY_AND_DATE_TEXT, cityAndDate);
    return params;
  }

  private String buildRescissionText() {
    Property property = contract.getProperty();
    LocalDate plannedEndDate = contract.getPlannedEndDate();

    String landlordDesc =
        removeTrailingPeriod(formatter.formatPersonForContract(contract.getLandlord()));
    String tenantDescs = contract.getTenants().stream().map(formatter::formatPersonForContract)
        .map(this::removeTrailingPeriod).collect(Collectors.joining(" e "));
    String propertyAddress = property != null && property.getAddress() != null ?
        formatter.formatAddressForContract(property.getAddress()) :
        "-";
    String city = TemplateFormatter.personCity(contract.getLandlord());

    String opening = bundle.getString("pdf.contract_notice.rescission.opening")
        .formatted(landlordDesc, tenantDescs);
    String clause1 = bundle.getString("pdf.contract_notice.rescission.clause1")
        .formatted(formatter.formatDateInFull(contract.getStartDate()), contract.getPurpose(),
            propertyAddress, formatter.formatPeriodForContract(contract.getDuration()),
            formatter.formatDateInFull(contract.getStartDate()),
            formatter.formatDateInFull(plannedEndDate));
    String clause2 = bundle.getString("pdf.contract_notice.rescission.clause2");
    String clause3 = bundle.getString("pdf.contract_notice.rescission.clause3");
    String clause4 = bundle.getString("pdf.contract_notice.rescission.clause4");
    String clause5 = bundle.getString("pdf.contract_notice.rescission.clause5").formatted(city);
    String closing = bundle.getString("pdf.contract_notice.rescission.closing");

    return opening + "<br><br>" + clause1 + "<br><br>" + clause2 + "<br><br>" + clause3 + "<br><br>" + clause4 + "<br><br>" + clause5 + "<br><br>" + closing;
  }

  private String removeTrailingPeriod(String text) {
    if (text == null) {
      return null;
    }
    return text.endsWith(".") ? text.substring(0, text.length() - 1) : text;
  }

  @Override
  public EnumMap<Collections, Collection<Object>> getCollections() {
    List<Object> signingEntries = new ArrayList<>();
    addSigningEntries(signingEntries, List.of(contract.getLandlord()),
        bundle.getString("pdf.contract.role.landlord"));
    addSigningEntries(signingEntries, contract.getTenants(),
        bundle.getString("pdf.contract.role.tenant"));
    addSigningEntries(signingEntries, contract.getWitnesses(),
        bundle.getString("pdf.contract.role.witness"));

    EnumMap<Collections, Collection<Object>> collections = new EnumMap<>(Collections.class);
    collections.put(Collections.SIGNING_TEXTS, signingEntries);
    return collections;
  }

  private void addSigningEntries(List<Object> signingEntries, List<Person> people,
      String roleLabel) {
    boolean shouldShowOrdinal = people.size() > 1;
    IntStream.range(0, people.size()).forEach(i -> {
      String indexedRoleLabel = shouldShowOrdinal ? roleLabel + " " + (i + 1) : roleLabel;
      signingEntries.add(new TextBean(bold(indexedRoleLabel + ": ") + signingText(people.get(i))));
    });
  }

  private String signingText(Person person) {
    if (person instanceof PhysicalPerson pp) {
      return pp.getName() + ", CPF " + TemplateFormatter.formatCpf(pp.getCpf());
    }
    if (person instanceof JuridicalPerson jp) {
      return jp.getCorporateName() + ", CNPJ " + TemplateFormatter.formatCnpj(jp.getCnpj());
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  public enum Parameters {
    NOTICE_TITLE, NOTICE_TEXT, CITY_AND_DATE_TEXT
  }


  public enum Collections {
    SIGNING_TEXTS
  }


  public record TextBean(String text) {
    public String getText() {
      return text;
    }
  }
}
