package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public class RentEvolutionTemplate
    extends PdfTemplate<RentEvolutionTemplate.RentEvolutionParameters, RentEvolutionTemplate.RentEvolutionCollections> {

  private final BufferedImage monthlyEarningsChart;
  private final List<PropertyChartBean> propertyCharts;

  public RentEvolutionTemplate(BufferedImage monthlyEarningsChart,
      List<PropertyChartBean> propertyCharts) {
    super("rent_evolution");
    this.monthlyEarningsChart = monthlyEarningsChart;
    this.propertyCharts = propertyCharts;
  }

  @Override
  public EnumMap<RentEvolutionParameters, Object> getParameters() {
    EnumMap<RentEvolutionParameters, Object> params = new EnumMap<>(RentEvolutionParameters.class);
    params.put(RentEvolutionParameters.MONTHLY_EARNINGS_CHART, monthlyEarningsChart);
    return params;
  }

  @Override
  public EnumMap<RentEvolutionCollections, Collection<Object>> getCollections() {
    EnumMap<RentEvolutionCollections, Collection<Object>> collections =
        new EnumMap<>(RentEvolutionCollections.class);
    collections.put(RentEvolutionCollections.PROPERTY_RENT_CHARTS,
        new ArrayList<>(propertyCharts));
    return collections;
  }

  public enum RentEvolutionParameters {
    MONTHLY_EARNINGS_CHART
  }

  public enum RentEvolutionCollections {
    PROPERTY_RENT_CHARTS
  }
}
