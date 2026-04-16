package com.guilherme.emobiliaria.shared.pdf.templates;

import java.awt.image.BufferedImage;

public record PropertyChartBean(String propertyName, BufferedImage chart) {

  public String getPropertyName() {
    return propertyName;
  }

  public BufferedImage getChart() {
    return chart;
  }
}
