package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.util.Collection;
import java.util.EnumMap;
import java.util.stream.Collectors;

public class ReceiptTemplate extends PdfTemplate<ReceiptTemplate.ReceiptParameters, ReceiptTemplate.ReceiptCollections> {

  public enum ReceiptParameters {
    PAYER_IDENTIFICATION, PAYMENT_VALUE_IN_FULL, PERIOD, DISCOUNT,
    FINE, PAYED_VALUE, LANDLORD_CITY, RECEIPT_DATE, LANDLORD_IDENTIFICATION
  }

  public enum ReceiptCollections {}

  private final Receipt receipt;

  public ReceiptTemplate(Receipt receipt) {
    super("receipt");
    this.receipt = receipt;
  }

  @Override
  public EnumMap<ReceiptParameters, Object> getParameters() {
    EnumMap<ReceiptParameters, Object> params = new EnumMap<>(ReceiptParameters.class);
    Contract contract = receipt.getContract();
    int rent = contract.getProperty().getRent();
    int payedValue = rent - receipt.getDiscount() + receipt.getFine();

    params.put(ReceiptParameters.PAYER_IDENTIFICATION, contract.getTenants().stream()
        .map(TemplateFormatter::personName)
        .collect(Collectors.joining(", ")));
    params.put(ReceiptParameters.PAYMENT_VALUE_IN_FULL, TemplateFormatter.formatCurrencyInFull(rent));
    params.put(ReceiptParameters.PERIOD, TemplateFormatter.formatDate(receipt.getIntervalStart())
        + " a " + TemplateFormatter.formatDate(receipt.getIntervalEnd()));
    params.put(ReceiptParameters.DISCOUNT, TemplateFormatter.formatCurrency(receipt.getDiscount()));
    params.put(ReceiptParameters.FINE, TemplateFormatter.formatCurrency(receipt.getFine()));
    params.put(ReceiptParameters.PAYED_VALUE, TemplateFormatter.formatCurrency(payedValue));
    params.put(ReceiptParameters.LANDLORD_CITY, TemplateFormatter.personCity(contract.getLandlord()));
    params.put(ReceiptParameters.RECEIPT_DATE, TemplateFormatter.formatDate(receipt.getDate()));
    params.put(ReceiptParameters.LANDLORD_IDENTIFICATION,
        TemplateFormatter.personName(contract.getLandlord()) + ", "
            + TemplateFormatter.personDescription(contract.getLandlord()));
    return params;
  }

  @Override
  public EnumMap<ReceiptCollections, Collection<Object>> getCollections() {
    return new EnumMap<>(ReceiptCollections.class);
  }
}
