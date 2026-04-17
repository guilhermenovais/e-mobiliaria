package com.guilherme.emobiliaria.shared.pdf.templates;

import java.awt.image.BufferedImage;

public record PropertyChartBean(String propertyName, BufferedImage chart) {

  public String getPropertyName() {
    return propertyName;
  }

  // Required by JasperReports/BeanUtils: template field is named "property_name" (snake_case)
  public String getProperty_name() {
    return propertyName;
  }

  public BufferedImage getChart() {
    return chart;
  }
}
