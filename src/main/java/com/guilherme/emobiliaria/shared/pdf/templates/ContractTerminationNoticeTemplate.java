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

public class ContractTerminationNoticeTemplate extends
    PdfTemplate<ContractTerminationNoticeTemplate.Parameters, ContractTerminationNoticeTemplate.Collections> {

  private final Contract contract;
  private final LocalDate noticeDate;
  private final ResourceBundle bundle;
  private final TemplateFormatter formatter;

  public ContractTerminationNoticeTemplate(Contract contract, LocalDate noticeDate,
      ResourceBundle bundle) {
    super("contract_termination_notice");
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
    params.put(Parameters.NOTICE_TEXT, buildTerminationNoticeText());
    params.put(Parameters.CITY_AND_DATE_TEXT, cityAndDate);
    return params;
  }

  private String buildTerminationNoticeText() {
    Property property = contract.getProperty();
    LocalDate plannedEndDate = contract.getPlannedEndDate();
    String tenantNames = contract.getTenants().stream().map(TemplateFormatter::personName)
        .collect(Collectors.joining("\n"));

    String propertyAddress = property != null && property.getAddress() != null ?
        formatter.formatAddressForContract(property.getAddress()) :
        "-";

    String terminationBodyKey = hasMultipleLandlords() ?
        "pdf.contract_notice.termination.body.plural" :
        "pdf.contract_notice.termination.body";

    String body = bundle.getString(
        "pdf.contract_notice.termination.title") + " " + tenantNames + ",<br><br>" + bundle.getString(
        terminationBodyKey).formatted(contract.getPurpose(), propertyAddress,
        formatter.formatDateInFull(contract.getStartDate()),
        formatter.formatDateInFull(plannedEndDate));
    String closing = bundle.getString("pdf.contract_notice.termination.closing");

    return body + "<br><br>" + closing;
  }

  private boolean hasMultipleLandlords() {
    try {
      var method = contract.getClass().getMethod("getLandlords");
      Object landlords = method.invoke(contract);
      if (landlords instanceof Collection<?> landlordCollection) {
        return landlordCollection.size() > 1;
      }
      if (landlords instanceof Object[] landlordArray) {
        return landlordArray.length > 1;
      }
    } catch (ReflectiveOperationException ignored) {
      // Fallback to the current single-landlord model.
    }
    return false;
  }

  @Override
  public EnumMap<Collections, Collection<Object>> getCollections() {
    List<Object> signingEntries = new ArrayList<>();
    addSigningEntries(signingEntries, List.of(contract.getLandlord()),
        bundle.getString("pdf.contract.role.landlord"));
    addSigningEntries(signingEntries, contract.getTenants(),
        bundle.getString("pdf.contract.role.tenant"));
    if (!contract.getWitnesses().isEmpty()) {
      addSigningEntries(signingEntries, contract.getWitnesses(),
          bundle.getString("pdf.contract.role.witness"));
    }

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
      return pp.getName() + ", CPF: " + TemplateFormatter.formatCpf(pp.getCpf());
    }
    if (person instanceof JuridicalPerson jp) {
      return jp.getCorporateName() + ", CNPJ: " + TemplateFormatter.formatCnpj(jp.getCnpj());
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  public enum Parameters {
    NOTICE_TEXT, CITY_AND_DATE_TEXT
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
