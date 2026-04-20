package com.guilherme.emobiliaria.shared.pdf.templates;

public class PropertyInflationLagRowBean {

  private final String propertyName;
  private final String currentRent;
  private final String gapVsIpca;
  private final String gapVsIgpm;
  private final String worstGap;
  private final String status;
  private final boolean isLagging;

  public PropertyInflationLagRowBean(String propertyName, String currentRent, String gapVsIpca,
      String gapVsIgpm, String worstGap, String status, boolean isLagging) {
    this.propertyName = propertyName;
    this.currentRent = currentRent;
    this.gapVsIpca = gapVsIpca;
    this.gapVsIgpm = gapVsIgpm;
    this.worstGap = worstGap;
    this.status = status;
    this.isLagging = isLagging;
  }

  public String getProperty_name() {
    return propertyName;
  }

  public String getCurrent_rent() {
    return currentRent;
  }

  public String getGap_vs_ipca() {
    return gapVsIpca;
  }

  public String getGap_vs_igpm() {
    return gapVsIgpm;
  }

  public String getWorst_gap() {
    return worstGap;
  }

  public String getStatus() {
    return status;
  }

  public boolean getIs_lagging() {
    return isLagging;
  }
}
