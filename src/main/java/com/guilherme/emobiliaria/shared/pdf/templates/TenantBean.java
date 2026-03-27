package com.guilherme.emobiliaria.shared.pdf.templates;

public class TenantBean {
  private final String tenantIdentification;
  private final String tenantDescription;

  public TenantBean(String tenantIdentification, String tenantDescription) {
    this.tenantIdentification = tenantIdentification;
    this.tenantDescription = tenantDescription;
  }

  public String getTenantIdentification() {
    return tenantIdentification;
  }

  public String getTenantDescription() {
    return tenantDescription;
  }
}
