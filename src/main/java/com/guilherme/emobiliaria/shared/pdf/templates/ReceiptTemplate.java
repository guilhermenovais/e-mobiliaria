package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.time.format.DateTimeFormatter;
import java.util.Collection;
import java.util.EnumMap;

public class ReceiptTemplate extends PdfTemplate<ReceiptTemplate.ReceiptParameters, ReceiptTemplate.ReceiptCollections> {

  private static final DateTimeFormatter SHORT_DATE_FORMATTER =
      DateTimeFormatter.ofPattern("dd/MM/yy");
  private static final String RECEIPT_TITLE = "RECIBO DE ALUGUEL";
  private static final int VALUES_TABLE_WIDTH = 60;

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
    params.put(ReceiptParameters.VALUES_TABLE_RTF,
        buildValuesTable(rent, discount, fine, totalPaid));
    params.put(ReceiptParameters.CITY_AND_DATE_TEXT,
        "<html><body style='font-family: Arial; text-align: center;'>" + TemplateFormatter.personCity(
            contract.getLandlord()) + ", " + TemplateFormatter.formatDateInFull(
            receipt.getDate()) + "." + "</body></html>");
    params.put(ReceiptParameters.OBSERVATIONS, "");
    params.put(ReceiptParameters.LANDLORD_SIGNING_TEXT, signingText(contract.getLandlord()));
    return params;
  }

  public enum ReceiptCollections {}

  public enum ReceiptParameters {
    RECEIPT_TEXT, RECEIPT_TITLE, VALUES_TABLE_RTF, CITY_AND_DATE_TEXT, OBSERVATIONS, LANDLORD_SIGNING_TEXT
  }

  @Override
  public EnumMap<ReceiptCollections, Collection<Object>> getCollections() {
    return new EnumMap<>(ReceiptCollections.class);
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
    return address.getAddress() + " nº " + address.getNumber() + ", " + address.getNeighborhood() + ", " + address.getCity() + "-" + address.getState()
        .name();
  }

  private String formatShortDate(java.time.LocalDate date) {
    return date.format(SHORT_DATE_FORMATTER);
  }

  private String buildValuesTable(int rent, int discount, int fine, int totalPaid) {
    return String.join("\n",
        buildDottedRow("aluguel do período", TemplateFormatter.formatCurrency(rent)),
        buildDottedRow("desconto", TemplateFormatter.formatCurrency(discount)),
        buildDottedRow("multa", TemplateFormatter.formatCurrency(fine)),
        buildDottedRow("valor pago", TemplateFormatter.formatCurrency(totalPaid)));
  }

  private String buildDottedRow(String label, String value) {
    int targetWidthInDots = VALUES_TABLE_WIDTH * 2;
    int labelWidthInDots = textWidthInDots(label);
    int valueWidthInDots = valueTextWidthInDots(value);
    int dots = Math.max(0, targetWidthInDots - labelWidthInDots - valueWidthInDots - 1);
    return label + ".".repeat(dots) + " " + value;
  }

  private int textWidthInDots(String text) {
    int width = 0;
    for (int i = 0; i < text.length(); i++) {
      width += text.charAt(i) == ' ' ? 1 : 2;
    }
    return width;
  }

  private int valueTextWidthInDots(String text) {
    int width = 0;
    for (int i = 0; i < text.length(); i++) {
      char ch = text.charAt(i);
      width += (ch == ' ' || ch == '.' || ch == ',') ? 1 : 2;
    }
    return width;
  }
}
