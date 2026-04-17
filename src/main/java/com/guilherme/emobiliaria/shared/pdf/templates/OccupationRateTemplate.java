package com.guilherme.emobiliaria.shared.pdf.templates;

import com.guilherme.emobiliaria.shared.pdf.PdfTemplate;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EnumMap;
import java.util.List;

public class OccupationRateTemplate extends
    PdfTemplate<OccupationRateTemplate.OccupationRateParameters, OccupationRateTemplate.OccupationRateCollections> {

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
    super("occupation_rate");
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
    TREND_CHART, VACANCY_VOLUME_CHART, VACANCY_HEATMAP_CHART, GENERATION_DATE, PERIOD, CURRENT_OCCUPATION_PCT, CURRENT_VACANT_UNITS, AVG_VACANCY_RATE, LONGEST_STREAK_MONTHS, LONGEST_STREAK_PROPERTY
  }


  public enum OccupationRateCollections {
    VACANCY_TABLE_ROWS
  }
}
