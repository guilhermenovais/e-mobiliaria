package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class OccupationRateTemplate extends
    PdfTemplate<OccupationRateTemplate.OccupationRateParameters, OccupationRateTemplate.OccupationRateCollections> {

  private final ResourceBundle bundle;
  private final BufferedImage trendChart;
  private final BufferedImage vacancyVolumeChart;
  private final BufferedImage vacancyHeatmapChart;
  private final String generationDate;
  private final String period;
  private final String currentOccupationPct;
  private final String currentVacantUnits;
  private final String avgVacancyRate;
  private final String longestStreakMonths;
  private final String longestStreakProperty;
  private final List<VacancyTableRowBean> vacancyTableRows;

  public OccupationRateTemplate(BufferedImage trendChart, BufferedImage vacancyVolumeChart,
      BufferedImage vacancyHeatmapChart, String generationDate, String period,
      String currentOccupationPct, String currentVacantUnits, String avgVacancyRate,
      String longestStreakMonths, String longestStreakProperty,
      List<VacancyTableRowBean> vacancyTableRows) {
    this(ResourceBundle.getBundle("messages", Locale.forLanguageTag("pt-BR")), trendChart,
        vacancyVolumeChart, vacancyHeatmapChart, generationDate, period, currentOccupationPct,
        currentVacantUnits, avgVacancyRate, longestStreakMonths, longestStreakProperty,
        vacancyTableRows);
  }

  public OccupationRateTemplate(ResourceBundle bundle, BufferedImage trendChart,
      BufferedImage vacancyVolumeChart, BufferedImage vacancyHeatmapChart, String generationDate,
      String period, String currentOccupationPct, String currentVacantUnits, String avgVacancyRate,
      String longestStreakMonths, String longestStreakProperty,
      List<VacancyTableRowBean> vacancyTableRows) {
    super("occupation_rate");
    this.bundle = bundle;
    this.trendChart = trendChart;
    this.vacancyVolumeChart = vacancyVolumeChart;
    this.vacancyHeatmapChart = vacancyHeatmapChart;
    this.generationDate = generationDate;
    this.period = period;
    this.currentOccupationPct = currentOccupationPct;
    this.currentVacantUnits = currentVacantUnits;
    this.avgVacancyRate = avgVacancyRate;
    this.longestStreakMonths = longestStreakMonths;
    this.longestStreakProperty = longestStreakProperty;
    this.vacancyTableRows = vacancyTableRows;
  }

  @Override
  public EnumMap<OccupationRateParameters, Object> getParameters() {
    EnumMap<OccupationRateParameters, Object> params =
        new EnumMap<>(OccupationRateParameters.class);
    params.put(OccupationRateParameters.TREND_CHART, trendChart);
    params.put(OccupationRateParameters.VACANCY_VOLUME_CHART, vacancyVolumeChart);
    params.put(OccupationRateParameters.VACANCY_HEATMAP_CHART, vacancyHeatmapChart);
    params.put(OccupationRateParameters.GENERATION_DATE, generationDate);
    params.put(OccupationRateParameters.PERIOD, period);
    params.put(OccupationRateParameters.CURRENT_OCCUPATION_PCT, currentOccupationPct);
    params.put(OccupationRateParameters.CURRENT_VACANT_UNITS, currentVacantUnits);
    params.put(OccupationRateParameters.AVG_VACANCY_RATE, avgVacancyRate);
    params.put(OccupationRateParameters.LONGEST_STREAK_MONTHS, longestStreakMonths);
    params.put(OccupationRateParameters.LONGEST_STREAK_PROPERTY, longestStreakProperty);
    params.put(OccupationRateParameters.LBL_TITLE, bundle.getString("pdf.occupation_rate.title"));
    params.put(OccupationRateParameters.LBL_GENERATED_ON,
        bundle.getString("pdf.occupation_rate.generated_on"));
    params.put(OccupationRateParameters.LBL_PERIOD_PREFIX,
        bundle.getString("pdf.occupation_rate.period_prefix"));
    params.put(OccupationRateParameters.LBL_PAGE_PREFIX,
        bundle.getString("pdf.occupation_rate.page_prefix"));
    params.put(OccupationRateParameters.LBL_KPI_OCCUPATION,
        bundle.getString("pdf.occupation_rate.kpi.occupation"));
    params.put(OccupationRateParameters.LBL_KPI_VACANT_UNITS,
        bundle.getString("pdf.occupation_rate.kpi.vacant_units"));
    params.put(OccupationRateParameters.LBL_KPI_AVG_VACANCY,
        bundle.getString("pdf.occupation_rate.kpi.avg_vacancy"));
    params.put(OccupationRateParameters.LBL_KPI_LONGEST_STREAK,
        bundle.getString("pdf.occupation_rate.kpi.longest_streak"));
    params.put(OccupationRateParameters.LBL_CHART_TREND,
        bundle.getString("pdf.occupation_rate.chart.trend"));
    params.put(OccupationRateParameters.LBL_CHART_VOLUME,
        bundle.getString("pdf.occupation_rate.chart.volume"));
    params.put(OccupationRateParameters.LBL_HEATMAP_TITLE,
        bundle.getString("pdf.occupation_rate.heatmap.title"));
    params.put(OccupationRateParameters.LBL_LEGEND_OCCUPIED,
        bundle.getString("pdf.occupation_rate.legend.occupied"));
    params.put(OccupationRateParameters.LBL_LEGEND_VACANT,
        bundle.getString("pdf.occupation_rate.legend.vacant"));
    params.put(OccupationRateParameters.LBL_SECTION_HIGHEST_VACANCY,
        bundle.getString("pdf.occupation_rate.section.highest_vacancy"));
    params.put(OccupationRateParameters.LBL_COL_PROPERTY,
        bundle.getString("pdf.occupation_rate.col.property"));
    params.put(OccupationRateParameters.LBL_COL_VACANT_MONTHS,
        bundle.getString("pdf.occupation_rate.col.vacant_months"));
    params.put(OccupationRateParameters.LBL_COL_LONGEST_SEQ,
        bundle.getString("pdf.occupation_rate.col.longest_seq"));
    params.put(OccupationRateParameters.LBL_COL_LAST_CONTRACT,
        bundle.getString("pdf.occupation_rate.col.last_contract"));
    params.put(OccupationRateParameters.LBL_COL_STATUS,
        bundle.getString("pdf.occupation_rate.col.status"));
    params.put(OccupationRateParameters.APP_NAME, bundle.getString("pdf.app_name"));
    return params;
  }

  @Override
  public EnumMap<OccupationRateCollections, Collection<Object>> getCollections() {
    EnumMap<OccupationRateCollections, Collection<Object>> collections =
        new EnumMap<>(OccupationRateCollections.class);
    collections.put(OccupationRateCollections.VACANCY_TABLE_ROWS,
        new ArrayList<>(vacancyTableRows));
    return collections;
  }

  public enum OccupationRateParameters {
    TREND_CHART, VACANCY_VOLUME_CHART, VACANCY_HEATMAP_CHART, GENERATION_DATE, PERIOD, CURRENT_OCCUPATION_PCT, CURRENT_VACANT_UNITS, AVG_VACANCY_RATE, LONGEST_STREAK_MONTHS, LONGEST_STREAK_PROPERTY, LBL_TITLE, LBL_GENERATED_ON, LBL_PERIOD_PREFIX, LBL_PAGE_PREFIX, LBL_KPI_OCCUPATION, LBL_KPI_VACANT_UNITS, LBL_KPI_AVG_VACANCY, LBL_KPI_LONGEST_STREAK, LBL_CHART_TREND, LBL_CHART_VOLUME, LBL_HEATMAP_TITLE, LBL_LEGEND_OCCUPIED, LBL_LEGEND_VACANT, LBL_SECTION_HIGHEST_VACANCY, LBL_COL_PROPERTY, LBL_COL_VACANT_MONTHS, LBL_COL_LONGEST_SEQ, LBL_COL_LAST_CONTRACT, LBL_COL_STATUS, APP_NAME
  }


  public enum OccupationRateCollections {
    VACANCY_TABLE_ROWS
  }
}
