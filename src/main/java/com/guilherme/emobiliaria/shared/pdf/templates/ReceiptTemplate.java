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

public class ReceiptTemplate extends PdfTemplate<ReceiptTemplate.ReceiptParameters, ReceiptTemplate.ReceiptCollections> {

  private static final DateTimeFormatter SHORT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yy");
  private static final String RECEIPT_TITLE = "RECIBO DE ALUGUEL";

  private final Receipt receipt;

  public ReceiptTemplate(Receipt receipt) {
    super("receipt");
    this.receipt = receipt;
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
        "<html><body style='font-family: Arial;'>" + "Recebemos de " + TemplateFormatter.personName(
            tenant) + ", " + personIdentifier(
            tenant) + " a quantia de " + formattedTotalPaid + " (" + TemplateFormatter.formatCurrencyInFull(
            totalPaid) + ") referente a aluguel de imóvel sito à " + formatAddressForReceipt(
            contract.getProperty().getAddress()) + ", no período de " + formatShortDate(
            receipt.getIntervalStart()) + " a " + formatShortDate(
            receipt.getIntervalEnd()) + "." + "</body></html>");
    params.put(ReceiptParameters.RECEIPT_TITLE, RECEIPT_TITLE);
    params.put(ReceiptParameters.CITY_AND_DATE_TEXT,
        "<html><body style='font-family: Arial; text-align: center;'>" + TemplateFormatter.personCity(
            contract.getLandlord()) + ", " + TemplateFormatter.formatDateInFull(
            receipt.getDate()) + "." + "</body></html>");
    params.put(ReceiptParameters.OBSERVATIONS, receipt.getObservation() != null ? receipt.getObservation() : "");
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
    rows.add(new ValueRowBean("aluguel do período", TemplateFormatter.formatCurrency(rent)));
    rows.add(new ValueRowBean("desconto", TemplateFormatter.formatCurrency(discount)));
    rows.add(new ValueRowBean("multa", TemplateFormatter.formatCurrency(fine)));
    rows.add(new ValueRowBean("valor pago", TemplateFormatter.formatCurrency(totalPaid)));

    EnumMap<ReceiptCollections, Collection<Object>> collections =
        new EnumMap<>(ReceiptCollections.class);
    collections.put(ReceiptCollections.VALUES_TABLE_DATA, rows);
    return collections;
  }

  public enum ReceiptCollections {
    VALUES_TABLE_DATA
  }

  public enum ReceiptParameters {
    RECEIPT_TEXT, RECEIPT_TITLE, CITY_AND_DATE_TEXT, OBSERVATIONS, LANDLORD_SIGNING_TEXT
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
      return "CPF sob o n° " + TemplateFormatter.formatCpf(pp.getCpf());
    }
    if (person instanceof JuridicalPerson jp) {
      return "CNPJ sob o n° " + TemplateFormatter.formatCnpj(jp.getCnpj());
    }
    throw new IllegalArgumentException("Unknown person type: " + person.getClass());
  }

  private String formatAddressForReceipt(Address address) {
    String complement = address.getComplement() != null ? ", " + address.getComplement() : "";
    return address.getAddress() + " nº " + address.getNumber() + complement + ", " + address.getNeighborhood() + ", " + address.getCity() + "-" + address.getState().name();
  }

  private String formatShortDate(java.time.LocalDate date) {
    return date.format(SHORT_DATE_FORMATTER);
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
