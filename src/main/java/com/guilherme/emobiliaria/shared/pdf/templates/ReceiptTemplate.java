package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.ResourceBundle;

public class ReceiptTemplate
    extends PdfTemplate<ReceiptTemplate.ReceiptParameters, ReceiptTemplate.ReceiptCollections> {

  private static final DateTimeFormatter SHORT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yy");

  private final Receipt receipt;
  private final ResourceBundle bundle;
  private final TemplateFormatter formatter;

  public ReceiptTemplate(Receipt receipt, ResourceBundle bundle) {
    super("receipt");
    this.receipt = receipt;
    this.bundle = bundle;
    this.formatter = new TemplateFormatter(bundle);
  }

  @Override
  public EnumMap<ReceiptParameters, Object> getParameters() {
    Contract contract = receipt.getContract();
    Person tenant = contract.getTenants().getFirst();
    int rent = contract.getRent();
    int discount = receipt.getDiscount();
    int fine = receipt.getFine();
    int totalPaid = rent - discount + fine;

    String formattedTotalPaid = TemplateFormatter.formatCurrency(totalPaid);

    EnumMap<ReceiptParameters, Object> params = new EnumMap<>(ReceiptParameters.class);
    params.put(ReceiptParameters.RECEIPT_TEXT,
        "<html><body style='font-family: Arial;'>" + bundle.getString(
            "pdf.receipt.received_from") + " " + TemplateFormatter.personName(
            tenant) + personIdentifier(tenant) + bundle.getString(
            "pdf.receipt.amount_for") + " " + formattedTotalPaid + " (" + formatter.formatCurrencyInFull(
            totalPaid) + ")" + bundle.getString(
            "pdf.receipt.property_at") + " " + formatAddressForReceipt(
            contract.getProperty().getAddress()) + bundle.getString(
            "pdf.receipt.period") + " " + formatShortDate(
            receipt.getIntervalStart()) + " " + bundle.getString(
            "pdf.receipt.period_to") + " " + formatShortDate(
            receipt.getIntervalEnd()) + "." + "</body></html>");
    params.put(ReceiptParameters.RECEIPT_TITLE, bundle.getString("pdf.receipt.title"));
    params.put(ReceiptParameters.CITY_AND_DATE_TEXT,
        "<html><body style='font-family: Arial; text-align: center;'>" + TemplateFormatter.personCity(
            contract.getLandlord()) + ", " + formatter.formatDateInFull(
            receipt.getDate()) + "." + "</body></html>");
    params.put(ReceiptParameters.OBSERVATIONS,
        receipt.getObservation() != null ? receipt.getObservation() : "");
    params.put(ReceiptParameters.LANDLORD_SIGNING_TEXT, signingText(contract.getLandlord()));
    return params;
  }

  @Override
  public EnumMap<ReceiptCollections, Collection<Object>> getCollections() {
    Contract contract = receipt.getContract();
    int rent = contract.getRent();
    int discount = receipt.getDiscount();
    int fine = receipt.getFine();
    int totalPaid = rent - discount + fine;

    List<Object> rows = new ArrayList<>();
    rows.add(new ValueRowBean(bundle.getString("pdf.receipt.row.rent"),
        TemplateFormatter.formatCurrency(rent)));
    rows.add(new ValueRowBean(bundle.getString("pdf.receipt.row.discount"),
        TemplateFormatter.formatCurrency(discount)));
    rows.add(new ValueRowBean(bundle.getString("pdf.receipt.row.fine"),
        TemplateFormatter.formatCurrency(fine)));
    rows.add(new ValueRowBean(bundle.getString("pdf.receipt.row.total_paid"),
        TemplateFormatter.formatCurrency(totalPaid)));

    EnumMap<ReceiptCollections, Collection<Object>> collections =
        new EnumMap<>(ReceiptCollections.class);
    collections.put(ReceiptCollections.VALUES_TABLE_DATA, rows);
    return collections;
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

  private String personIdentifier(Person person) {
    if (person instanceof PhysicalPerson pp) {
      return bundle.getString("pdf.receipt.cpf_id") + " " + TemplateFormatter.formatCpf(
          pp.getCpf());
    }
    if (person instanceof JuridicalPerson jp) {
      return bundle.getString("pdf.receipt.cnpj_id") + " " + TemplateFormatter.formatCnpj(
          jp.getCnpj());
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  private String formatAddressForReceipt(Address address) {
    String complement = address.getComplement() != null ? ", " + address.getComplement() : "";
    return address.getAddress() + " " + bundle.getString(
        "pdf.receipt.address_number") + " " + address.getNumber() + complement + ", " + address.getNeighborhood() + ", " + address.getCity() + "-" + address.getState()
        .name();
  }

  private String formatShortDate(java.time.LocalDate date) {
    return date.format(SHORT_DATE_FORMATTER);
  }

  public enum ReceiptCollections {
    VALUES_TABLE_DATA
  }


  public enum ReceiptParameters {
    RECEIPT_TEXT, RECEIPT_TITLE, CITY_AND_DATE_TEXT, OBSERVATIONS, LANDLORD_SIGNING_TEXT
  }


  public record ValueRowBean(String label, String value) {
    public String getLabel() {
      return label;
    }

    public String getValue() {
      return value;
    }
  }
}
