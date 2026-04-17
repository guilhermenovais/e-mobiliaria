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
  private final String generationDate;
  private final String period;

  public RentEvolutionTemplate(BufferedImage monthlyEarningsChart,
      List<PropertyChartBean> propertyCharts, String generationDate, String period) {
    super("rent_evolution");
    this.monthlyEarningsChart = monthlyEarningsChart;
    this.propertyCharts = propertyCharts;
    this.generationDate = generationDate;
    this.period = period;
  }

  @Override
  public EnumMap<RentEvolutionParameters, Object> getParameters() {
    EnumMap<RentEvolutionParameters, Object> params = new EnumMap<>(RentEvolutionParameters.class);
    params.put(RentEvolutionParameters.MONTHLY_EARNINGS_CHART, monthlyEarningsChart);
    params.put(RentEvolutionParameters.GENERATION_DATE, generationDate);
    params.put(RentEvolutionParameters.PERIOD, period);
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
    MONTHLY_EARNINGS_CHART, GENERATION_DATE, PERIOD
  }

  public enum RentEvolutionCollections {
    PROPERTY_RENT_CHARTS
  }
}
