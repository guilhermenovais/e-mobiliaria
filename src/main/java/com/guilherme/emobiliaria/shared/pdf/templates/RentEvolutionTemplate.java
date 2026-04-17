package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public class RentEvolutionTemplate
    extends PdfTemplate<RentEvolutionTemplate.RentEvolutionParameters, RentEvolutionTemplate.RentEvolutionCollections> {

  private final BufferedImage portfolioInflationChart;
  private final BufferedImage portfolioGapChart;
  private final List<PropertyChartBean> propertyCharts;
  private final List<PropertyInflationLagRowBean> propertyInflationLagRows;
  private final String generationDate;
  private final String period;
  private final String kpiStatusHeadline;
  private final String kpiGapVsIpca;
  private final String kpiGapVsIgpm;
  private final String kpiNominalGrowth;
  private final String kpiRealGrowth;

  public RentEvolutionTemplate(BufferedImage portfolioInflationChart,
      BufferedImage portfolioGapChart, List<PropertyChartBean> propertyCharts,
      List<PropertyInflationLagRowBean> propertyInflationLagRows, String generationDate,
      String period, String kpiStatusHeadline, String kpiGapVsIpca, String kpiGapVsIgpm,
      String kpiNominalGrowth, String kpiRealGrowth) {
    super("rent_evolution");
    this.portfolioInflationChart = portfolioInflationChart;
    this.portfolioGapChart = portfolioGapChart;
    this.propertyCharts = propertyCharts;
    this.propertyInflationLagRows = propertyInflationLagRows;
    this.generationDate = generationDate;
    this.period = period;
    this.kpiStatusHeadline = kpiStatusHeadline;
    this.kpiGapVsIpca = kpiGapVsIpca;
    this.kpiGapVsIgpm = kpiGapVsIgpm;
    this.kpiNominalGrowth = kpiNominalGrowth;
    this.kpiRealGrowth = kpiRealGrowth;
  }

  @Override
  public EnumMap<RentEvolutionParameters, Object> getParameters() {
    EnumMap<RentEvolutionParameters, Object> params = new EnumMap<>(RentEvolutionParameters.class);
    params.put(RentEvolutionParameters.PORTFOLIO_INFLATION_CHART, portfolioInflationChart);
    params.put(RentEvolutionParameters.PORTFOLIO_GAP_CHART, portfolioGapChart);
    params.put(RentEvolutionParameters.GENERATION_DATE, generationDate);
    params.put(RentEvolutionParameters.PERIOD, period);
    params.put(RentEvolutionParameters.KPI_STATUS_HEADLINE, kpiStatusHeadline);
    params.put(RentEvolutionParameters.KPI_GAP_VS_IPCA, kpiGapVsIpca);
    params.put(RentEvolutionParameters.KPI_GAP_VS_IGPM, kpiGapVsIgpm);
    params.put(RentEvolutionParameters.KPI_NOMINAL_GROWTH, kpiNominalGrowth);
    params.put(RentEvolutionParameters.KPI_REAL_GROWTH, kpiRealGrowth);
    return params;
  }

  @Override
  public EnumMap<RentEvolutionCollections, Collection<Object>> getCollections() {
    EnumMap<RentEvolutionCollections, Collection<Object>> collections =
        new EnumMap<>(RentEvolutionCollections.class);
    collections.put(RentEvolutionCollections.PROPERTY_RENT_CHARTS,
        new ArrayList<>(propertyCharts));
    collections.put(RentEvolutionCollections.PROPERTY_INFLATION_LAG_ROWS,
        new ArrayList<>(propertyInflationLagRows));
    return collections;
  }

  public enum RentEvolutionParameters {
    PORTFOLIO_INFLATION_CHART, PORTFOLIO_GAP_CHART, GENERATION_DATE, PERIOD, KPI_STATUS_HEADLINE, KPI_GAP_VS_IPCA, KPI_GAP_VS_IGPM, KPI_NOMINAL_GROWTH, KPI_REAL_GROWTH
  }

  public enum RentEvolutionCollections {
    PROPERTY_RENT_CHARTS, PROPERTY_INFLATION_LAG_ROWS
  }
}
