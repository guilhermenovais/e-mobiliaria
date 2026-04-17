package com.guilherme.emobiliaria.reports.infrastructure.service;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyRentHistory;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.reports.domain.service.ReportFileService;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.OccupationRateTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyChartBean;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyInflationLagRowBean;
import com.guilherme.emobiliaria.shared.pdf.templates.RentEvolutionTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.VacancyTableRowBean;
import jakarta.inject.Inject;

import java.awt.image.BufferedImage;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class ReportFileServiceImpl implements ReportFileService {

  private static final String[] PT_MONTHS =
      {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};
  private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
  private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

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
    PortfolioBenchmarks benchmarks = buildPortfolioBenchmarks(data);
    PortfolioKpis kpis = buildPortfolioKpis(data, benchmarks);

    BufferedImage portfolioInflationChart =
        chartGenerator.portfolioInflationComparison(data.months(), data.monthlyTotalCents(),
            benchmarks.ipcaTotalsCents(), benchmarks.igpmTotalsCents());
    BufferedImage portfolioGapChart =
        chartGenerator.portfolioInflationGap(data.months(), benchmarks.ipcaGapCents(),
            benchmarks.igpmGapCents());

    List<PropertyChartBean> propertyCharts = data.propertyHistories().stream().map(history -> {
      BufferedImage chart = chartGenerator.rentEvolution(history.propertyName(), history.months(),
          history.actualCents(), history.ipcaAdjustedCents(), history.igpmAdjustedCents());
      return new PropertyChartBean(history.propertyName(), chart);
    }).toList();

    List<PropertyInflationLagRowBean> lagRows = buildPropertyLagRows(data);

    return pdfGenerationService.generatePdf(
        new RentEvolutionTemplate(portfolioInflationChart, portfolioGapChart, propertyCharts,
            lagRows, generationDate(), periodLabel(data.months()), kpis.statusHeadline(),
            kpis.gapVsIpca(), kpis.gapVsIgpm(), kpis.nominalGrowth(), kpis.realGrowth()));
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

  private PortfolioBenchmarks buildPortfolioBenchmarks(RentEvolutionData data) {
    List<Long> ipcaTotals = new ArrayList<>();
    List<Long> igpmTotals = new ArrayList<>();
    List<Long> ipcaGaps = new ArrayList<>();
    List<Long> igpmGaps = new ArrayList<>();

    for (int monthIdx = 0; monthIdx < data.months().size(); monthIdx++) {
      YearMonth month = data.months().get(monthIdx);
      long actualTotal =
          monthIdx < data.monthlyTotalCents().size() ? data.monthlyTotalCents().get(monthIdx) : 0L;
      long ipcaTotal = 0L;
      long igpmTotal = 0L;

      for (PropertyRentHistory history : data.propertyHistories()) {
        int idx = history.months().indexOf(month);
        if (idx < 0) {
          continue;
        }
        long currentRent = history.actualCents().get(idx);
        if (currentRent <= 0) {
          continue;
        }
        ipcaTotal += history.ipcaAdjustedCents().get(idx);
        igpmTotal += history.igpmAdjustedCents().get(idx);
      }

      ipcaTotals.add(ipcaTotal);
      igpmTotals.add(igpmTotal);
      ipcaGaps.add(actualTotal - ipcaTotal);
      igpmGaps.add(actualTotal - igpmTotal);
    }

    return new PortfolioBenchmarks(ipcaTotals, igpmTotals, ipcaGaps, igpmGaps);
  }

  private PortfolioKpis buildPortfolioKpis(RentEvolutionData data, PortfolioBenchmarks benchmarks) {
    if (data.monthlyTotalCents().isEmpty()) {
      return new PortfolioKpis("Sem dados no período", "—", "—", "—", "—");
    }

    int lastIdx = data.monthlyTotalCents().size() - 1;
    long currentActual = data.monthlyTotalCents().get(lastIdx);
    long currentIpca = lastIdx < benchmarks.ipcaTotalsCents().size() ?
        benchmarks.ipcaTotalsCents().get(lastIdx) :
        0L;
    long currentIgpm = lastIdx < benchmarks.igpmTotalsCents().size() ?
        benchmarks.igpmTotalsCents().get(lastIdx) :
        0L;

    long gapIpca = currentActual - currentIpca;
    long gapIgpm = currentActual - currentIgpm;
    double gapIpcaPct = percentage(gapIpca, currentIpca);
    double gapIgpmPct = percentage(gapIgpm, currentIgpm);

    long startActual = data.monthlyTotalCents().get(0);
    long startIpca =
        benchmarks.ipcaTotalsCents().isEmpty() ? 0L : benchmarks.ipcaTotalsCents().get(0);
    long startIgpm =
        benchmarks.igpmTotalsCents().isEmpty() ? 0L : benchmarks.igpmTotalsCents().get(0);
    double nominalGrowth = percentage(currentActual - startActual, startActual);
    double inflationGrowthIpca = percentage(currentIpca - startIpca, startIpca);
    double inflationGrowthIgpm = percentage(currentIgpm - startIgpm, startIgpm);
    double realGrowthIpca = realGrowth(nominalGrowth, inflationGrowthIpca);
    double realGrowthIgpm = realGrowth(nominalGrowth, inflationGrowthIgpm);

    return new PortfolioKpis(statusHeadline(gapIpca, gapIgpm), formatGap(gapIpca, gapIpcaPct),
        formatGap(gapIgpm, gapIgpmPct), formatSignedPercent(nominalGrowth),
        "IPCA " + formatSignedPercent(realGrowthIpca) + " | IGP-M " + formatSignedPercent(
            realGrowthIgpm));
  }

  private List<PropertyInflationLagRowBean> buildPropertyLagRows(RentEvolutionData data) {
    if (data.months().isEmpty()) {
      return List.of();
    }

    YearMonth currentMonth = data.months().get(data.months().size() - 1);
    List<PropertyLagMetric> metrics = new ArrayList<>();
    for (PropertyRentHistory history : data.propertyHistories()) {
      int idx = history.months().indexOf(currentMonth);
      if (idx < 0) {
        continue;
      }
      long currentRent = history.actualCents().get(idx);
      if (currentRent <= 0) {
        continue;
      }

      long ipcaRef = history.ipcaAdjustedCents().get(idx);
      long igpmRef = history.igpmAdjustedCents().get(idx);
      long gapIpca = currentRent - ipcaRef;
      long gapIgpm = currentRent - igpmRef;
      double gapIpcaPct = percentage(gapIpca, ipcaRef);
      double gapIgpmPct = percentage(gapIgpm, igpmRef);

      boolean ipcaIsWorst = gapIpca <= gapIgpm;
      long worstGap = ipcaIsWorst ? gapIpca : gapIgpm;
      double worstGapPct = ipcaIsWorst ? gapIpcaPct : gapIgpmPct;
      String worstRef = ipcaIsWorst ? "IPCA" : "IGP-M";
      String status = worstGap < 0 ? "Defasado" : "Acima";

      PropertyInflationLagRowBean row =
          new PropertyInflationLagRowBean(history.propertyName(), formatCurrency(currentRent),
              formatGap(gapIpca, gapIpcaPct), formatGap(gapIgpm, gapIgpmPct),
              worstRef + " " + formatGap(worstGap, worstGapPct), status);
      metrics.add(new PropertyLagMetric(worstGap, history.propertyName(), row));
    }

    metrics.sort(Comparator.comparingLong(PropertyLagMetric::worstGapCents)
        .thenComparing(PropertyLagMetric::propertyName));
    return metrics.stream().map(PropertyLagMetric::row).toList();
  }

  private String statusHeadline(long gapIpca, long gapIgpm) {
    if (gapIpca >= 0 && gapIgpm >= 0) {
      return "Portfólio acima da inflação (IPCA e IGP-M)";
    }
    if (gapIpca < 0 && gapIgpm < 0) {
      return "Portfólio abaixo da inflação (IPCA e IGP-M)";
    }
    if (gapIpca >= 0) {
      return "Portfólio acima do IPCA e abaixo do IGP-M";
    }
    return "Portfólio abaixo do IPCA e acima do IGP-M";
  }

  private String formatGap(long gapCents, double gapPct) {
    return formatSignedCurrency(gapCents) + " (" + formatSignedPercent(gapPct) + ")";
  }

  private String formatCurrency(long cents) {
    NumberFormat nf = NumberFormat.getNumberInstance(PT_BR);
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    return "R$ " + nf.format(cents / 100.0);
  }

  private String formatSignedCurrency(long cents) {
    String sign = cents >= 0 ? "+" : "-";
    NumberFormat nf = NumberFormat.getNumberInstance(PT_BR);
    nf.setMinimumFractionDigits(2);
    nf.setMaximumFractionDigits(2);
    return "R$ " + sign + nf.format(Math.abs(cents) / 100.0);
  }

  private String formatSignedPercent(double value) {
    double normalized = Math.abs(value) < 0.05 ? 0.0 : value;
    NumberFormat nf = NumberFormat.getNumberInstance(PT_BR);
    nf.setMinimumFractionDigits(1);
    nf.setMaximumFractionDigits(1);
    String sign = normalized >= 0 ? "+" : "-";
    return sign + nf.format(Math.abs(normalized)) + "%";
  }

  private double percentage(long numerator, long denominator) {
    if (denominator <= 0) {
      return 0.0;
    }
    return numerator * 100.0 / denominator;
  }

  private double realGrowth(double nominalPct, double inflationPct) {
    double nominalFactor = 1 + nominalPct / 100.0;
    double inflationFactor = 1 + inflationPct / 100.0;
    if (inflationFactor <= 0) {
      return 0.0;
    }
    return (nominalFactor / inflationFactor - 1) * 100.0;
  }

  private record PortfolioBenchmarks(List<Long> ipcaTotalsCents, List<Long> igpmTotalsCents,
                                     List<Long> ipcaGapCents, List<Long> igpmGapCents) {
  }


  private record PortfolioKpis(String statusHeadline, String gapVsIpca, String gapVsIgpm,
                               String nominalGrowth, String realGrowth) {
  }


  private record PropertyLagMetric(long worstGapCents, String propertyName,
                                   PropertyInflationLagRowBean row) {
  }
}
