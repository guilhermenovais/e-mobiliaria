package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.util.Collection;
import java.util.EnumMap;

public class ReceiptTemplate extends PdfTemplate<ReceiptTemplate.ReceiptParameters, ReceiptTemplate.ReceiptCollections> {

  @Override
  public EnumMap<ReceiptParameters, Object> getParameters() {
    EnumMap<ReceiptParameters, Object> params = new EnumMap<>(ReceiptParameters.class);
    params.put(ReceiptParameters.RECEIPT_TEXT,
        "<html><body style='font-family: Arial;'>" + "Recebemos de João Silva a importância de R$ 1.500,00 referente ao aluguel do mês de março de 2026." + "</body></html>");
    params.put(ReceiptParameters.RECEIPT_TITLE, "RECIBO DE ALUGUEL");
    params.put(ReceiptParameters.VALUES_TABLE_RTF,
        "{\\rtf1\\ansi\\deff0\\fs20" + "\\b Valores\\b0\\par" + "Aluguel: R$ 1.500,00\\par" + "Desconto: R$ 0,00\\par" + "Multa: R$ 0,00\\par" + "Total pago: R$ 1.500,00\\par" + "}");
    params.put(ReceiptParameters.CITY_AND_DATE_TEXT,
        "<html><body style='font-family: Arial; text-align: center;'>" + "São Paulo, 10 de março de 2026." + "</body></html>");
    params.put(ReceiptParameters.OBSERVATIONS,
        "{\\rtf1\\ansi\\deff0\\fs20 Sem observações adicionais.\\par}");
    params.put(ReceiptParameters.LANDLORD_SIGNING_TEXT, "Maria Souza");
    return params;
  }

  public enum ReceiptCollections {}

  private final Receipt receipt;

  public ReceiptTemplate(Receipt receipt) {
    super("receipt");
    this.receipt = receipt;
  }

  public enum ReceiptParameters {
    RECEIPT_TEXT, RECEIPT_TITLE, VALUES_TABLE_RTF, CITY_AND_DATE_TEXT, OBSERVATIONS, LANDLORD_SIGNING_TEXT
  }

  @Override
  public EnumMap<ReceiptCollections, Collection<Object>> getCollections() {
    return new EnumMap<>(ReceiptCollections.class);
  }
}
