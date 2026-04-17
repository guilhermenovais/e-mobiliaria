package com.guilherme.emobiliaria.reports.infrastructure.service;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.reports.domain.service.ReportFileService;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.OccupationRateTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyChartBean;
import com.guilherme.emobiliaria.shared.pdf.templates.RentEvolutionTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.VacancyTableRowBean;
import jakarta.inject.Inject;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ReportFileServiceImpl implements ReportFileService {

  private static final String[] PT_MONTHS =
      {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

  private final PdfGenerationService pdfGenerationService;
  private final ChartGenerator chartGenerator;

  @Inject
  public ReportFileServiceImpl(PdfGenerationService pdfGenerationService,
      ChartGenerator chartGenerator) {
    this.pdfGenerationService = pdfGenerationService;
    this.chartGenerator = chartGenerator;
  }

  @Override
  public byte[] generateRentEvolutionPdf(RentEvolutionData data) {
    BufferedImage monthlyEarningsChart =
        chartGenerator.monthlyEarnings(data.months(), data.monthlyTotalCents());

    List<PropertyChartBean> propertyCharts = data.propertyHistories().stream().map(history -> {
      BufferedImage chart = chartGenerator.rentEvolution(history.propertyName(), history.months(),
          history.actualCents(), history.ipcaAdjustedCents(), history.igpmAdjustedCents());
      return new PropertyChartBean(history.propertyName(), chart);
    }).toList();

    return pdfGenerationService.generatePdf(
        new RentEvolutionTemplate(monthlyEarningsChart, propertyCharts, generationDate(),
            periodLabel(data.months())));
  }

  @Override
  public byte[] generateOccupationRatePdf(OccupationRateData data) {
    BufferedImage trendChart =
        chartGenerator.occupancyTrend(data.months(), data.occupiedCounts(), data.totalProperties());
    BufferedImage volumeChart =
        chartGenerator.vacancyVolume(data.months(), data.occupiedCounts(), data.totalProperties());
    BufferedImage heatmapChart =
        chartGenerator.vacancyHeatmap(data.months(), data.propertyHistories());

    List<VacancyTableRowBean> tableRows =
        data.vacancyTableRows().stream().map(VacancyTableRowBean::new).toList();

    int totalProperties = data.totalProperties();
    int lastOccupied =
        data.months().isEmpty() ? 0 : data.occupiedCounts().get(data.occupiedCounts().size() - 1);
    double currentOccPct = totalProperties > 0 ? lastOccupied * 100.0 / totalProperties : 0.0;

    String longestStreakLabel =
        data.longestVacancyStreakMonths() > 0 ? data.longestVacancyStreakMonths() + " meses" : "—";

    return pdfGenerationService.generatePdf(
        new OccupationRateTemplate(trendChart, volumeChart, heatmapChart, generationDate(),
            periodLabel(data.months()), String.format("%.0f%%", currentOccPct),
            String.valueOf(data.currentVacancyCount()),
            String.format("%.1f%%", data.avgVacancyRate()), longestStreakLabel,
            data.longestVacancyStreakPropertyName(), tableRows));
  }

  private String generationDate() {
    return LocalDate.now().format(DATE_FMT);
  }

  private String periodLabel(List<YearMonth> months) {
    if (months.isEmpty())
      return "";
    YearMonth first = months.get(0);
    YearMonth last = months.get(months.size() - 1);
    return monthLabel(first) + " a " + monthLabel(last);
  }

  private String monthLabel(YearMonth ym) {
    return PT_MONTHS[ym.getMonthValue() - 1] + "/" + ym.getYear();
  }
}
