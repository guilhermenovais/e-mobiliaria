package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.ResourceBundle;

public class RentEvolutionTemplate extends
    PdfTemplate<RentEvolutionTemplate.RentEvolutionParameters, RentEvolutionTemplate.RentEvolutionCollections> {

  private final ResourceBundle bundle;
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

  public RentEvolutionTemplate(ResourceBundle bundle, BufferedImage portfolioInflationChart,
      BufferedImage portfolioGapChart, List<PropertyChartBean> propertyCharts,
      List<PropertyInflationLagRowBean> propertyInflationLagRows, String generationDate,
      String period, String kpiStatusHeadline, String kpiGapVsIpca, String kpiGapVsIgpm,
      String kpiNominalGrowth, String kpiRealGrowth) {
    super("rent_evolution");
    this.bundle = bundle;
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
    params.put(RentEvolutionParameters.LBL_TITLE, bundle.getString("pdf.rent_evolution.title"));
    params.put(RentEvolutionParameters.LBL_GENERATED_ON,
        bundle.getString("pdf.rent_evolution.generated_on"));
    params.put(RentEvolutionParameters.LBL_PERIOD_PREFIX,
        bundle.getString("pdf.rent_evolution.period_prefix"));
    params.put(RentEvolutionParameters.LBL_PAGE_PREFIX,
        bundle.getString("pdf.rent_evolution.page_prefix"));
    params.put(RentEvolutionParameters.LBL_KPI_GAP_IPCA,
        bundle.getString("pdf.rent_evolution.kpi.gap_ipca"));
    params.put(RentEvolutionParameters.LBL_KPI_GAP_IGPM,
        bundle.getString("pdf.rent_evolution.kpi.gap_igpm"));
    params.put(RentEvolutionParameters.LBL_KPI_NOMINAL_GROWTH,
        bundle.getString("pdf.rent_evolution.kpi.nominal_growth"));
    params.put(RentEvolutionParameters.LBL_KPI_REAL_GROWTH,
        bundle.getString("pdf.rent_evolution.kpi.real_growth"));
    params.put(RentEvolutionParameters.LBL_CHART_PORTFOLIO,
        bundle.getString("pdf.rent_evolution.chart.portfolio"));
    params.put(RentEvolutionParameters.LBL_CHART_GAP,
        bundle.getString("pdf.rent_evolution.chart.gap"));
    params.put(RentEvolutionParameters.LBL_SECTION_RANKING,
        bundle.getString("pdf.rent_evolution.section.ranking"));
    params.put(RentEvolutionParameters.LBL_SECTION_RANKING_SUBTITLE,
        bundle.getString("pdf.rent_evolution.section.ranking_subtitle"));
    params.put(RentEvolutionParameters.LBL_COL_PROPERTY,
        bundle.getString("pdf.rent_evolution.col.property"));
    params.put(RentEvolutionParameters.LBL_COL_CURRENT_RENT,
        bundle.getString("pdf.rent_evolution.col.current_rent"));
    params.put(RentEvolutionParameters.LBL_COL_GAP_IPCA,
        bundle.getString("pdf.rent_evolution.col.gap_ipca"));
    params.put(RentEvolutionParameters.LBL_COL_GAP_IGPM,
        bundle.getString("pdf.rent_evolution.col.gap_igpm"));
    params.put(RentEvolutionParameters.LBL_COL_WORST_GAP,
        bundle.getString("pdf.rent_evolution.col.worst_gap"));
    params.put(RentEvolutionParameters.LBL_SECTION_PER_PROPERTY,
        bundle.getString("pdf.rent_evolution.section.per_property"));
    params.put(RentEvolutionParameters.APP_NAME, bundle.getString("pdf.app_name"));
    return params;
  }

  @Override
  public EnumMap<RentEvolutionCollections, Collection<Object>> getCollections() {
    EnumMap<RentEvolutionCollections, Collection<Object>> collections =
        new EnumMap<>(RentEvolutionCollections.class);
    collections.put(RentEvolutionCollections.PROPERTY_RENT_CHARTS, new ArrayList<>(propertyCharts));
    collections.put(RentEvolutionCollections.PROPERTY_INFLATION_LAG_ROWS,
        new ArrayList<>(propertyInflationLagRows));
    return collections;
  }

  public enum RentEvolutionParameters {
    PORTFOLIO_INFLATION_CHART, PORTFOLIO_GAP_CHART, GENERATION_DATE, PERIOD, KPI_STATUS_HEADLINE, KPI_GAP_VS_IPCA, KPI_GAP_VS_IGPM, KPI_NOMINAL_GROWTH, KPI_REAL_GROWTH, LBL_TITLE, LBL_GENERATED_ON, LBL_PERIOD_PREFIX, LBL_PAGE_PREFIX, LBL_KPI_GAP_IPCA, LBL_KPI_GAP_IGPM, LBL_KPI_NOMINAL_GROWTH, LBL_KPI_REAL_GROWTH, LBL_CHART_PORTFOLIO, LBL_CHART_GAP, LBL_SECTION_RANKING, LBL_SECTION_RANKING_SUBTITLE, LBL_COL_PROPERTY, LBL_COL_CURRENT_RENT, LBL_COL_GAP_IPCA, LBL_COL_GAP_IGPM, LBL_COL_WORST_GAP, LBL_SECTION_PER_PROPERTY, APP_NAME
  }


  public enum RentEvolutionCollections {
    PROPERTY_RENT_CHARTS, PROPERTY_INFLATION_LAG_ROWS
  }
}
