package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public class OccupationRateTemplate
    extends PdfTemplate<OccupationRateTemplate.OccupationRateParameters, OccupationRateTemplate.OccupationRateCollections> {

  private final BufferedImage overallOccupationChart;
  private final List<PropertyChartBean> propertyCharts;

  public OccupationRateTemplate(BufferedImage overallOccupationChart,
      List<PropertyChartBean> propertyCharts) {
    super("occupation_rate");
    this.overallOccupationChart = overallOccupationChart;
    this.propertyCharts = propertyCharts;
  }

  @Override
  public EnumMap<OccupationRateParameters, Object> getParameters() {
    EnumMap<OccupationRateParameters, Object> params = new EnumMap<>(OccupationRateParameters.class);
    params.put(OccupationRateParameters.OVERALL_OCCUPATION_CHART, overallOccupationChart);
    return params;
  }

  @Override
  public EnumMap<OccupationRateCollections, Collection<Object>> getCollections() {
    EnumMap<OccupationRateCollections, Collection<Object>> collections =
        new EnumMap<>(OccupationRateCollections.class);
    collections.put(OccupationRateCollections.PROPERTY_OCCUPATION_CHARTS,
        new ArrayList<>(propertyCharts));
    return collections;
  }

  public enum OccupationRateParameters {
    OVERALL_OCCUPATION_CHART
  }

  public enum OccupationRateCollections {
    PROPERTY_OCCUPATION_CHARTS
  }
}
