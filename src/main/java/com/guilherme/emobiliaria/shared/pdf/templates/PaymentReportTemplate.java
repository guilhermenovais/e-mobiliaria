package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.util.Collection;
import java.util.EnumMap;

public class PaymentReportTemplate
    extends PdfTemplate<PaymentReportTemplate.Param, PaymentReportTemplate.Coll> {

  private final String monthLabel;
  private final String generationDate;
  private final String appName;

  public PaymentReportTemplate(String monthLabel, String generationDate, String appName) {
    super("payment_report");
    this.monthLabel = monthLabel;
    this.generationDate = generationDate;
    this.appName = appName;
  }

  @Override
  public EnumMap<Param, Object> getParameters() {
    EnumMap<Param, Object> params = new EnumMap<>(Param.class);
    params.put(Param.MONTH_LABEL, monthLabel);
    params.put(Param.GENERATION_DATE, generationDate);
    params.put(Param.APP_NAME, appName);
    return params;
  }

  @Override
  public EnumMap<Coll, Collection<Object>> getCollections() {
    return new EnumMap<>(Coll.class);
  }

  public enum Param {
    MONTH_LABEL, GENERATION_DATE, APP_NAME
  }


  public enum Coll {
    _UNUSED
  }
}
