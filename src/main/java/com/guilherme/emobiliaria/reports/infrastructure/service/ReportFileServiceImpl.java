package com.guilherme.emobiliaria.reports.infrastructure.service;

import com.guilherme.emobiliaria.reports.domain.entity.OccupationRateData;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyOccupationHistory;
import com.guilherme.emobiliaria.reports.domain.entity.PropertyRentHistory;
import com.guilherme.emobiliaria.reports.domain.entity.RentEvolutionData;
import com.guilherme.emobiliaria.reports.domain.service.ReportFileService;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.OccupationRateTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyChartBean;
import com.guilherme.emobiliaria.shared.pdf.templates.RentEvolutionTemplate;
import jakarta.inject.Inject;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
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
    BufferedImage monthlyEarningsChart = chartGenerator.monthlyEarnings(
        data.months(), data.monthlyTotalCents());

    List<PropertyChartBean> propertyCharts = new ArrayList<>();
    for (PropertyRentHistory history : data.propertyHistories()) {
      BufferedImage chart = chartGenerator.rentEvolution(
          history.propertyName(),
          history.months(),
          history.actualCents(),
          history.ipcaAdjustedCents(),
          history.igpmAdjustedCents());
      propertyCharts.add(new PropertyChartBean(history.propertyName(), chart));
    }

    return pdfGenerationService.generatePdf(
        new RentEvolutionTemplate(monthlyEarningsChart, propertyCharts,
            generationDate(), periodLabel(data.months())));
  }

  @Override
  public byte[] generateOccupationRatePdf(OccupationRateData data) {
    BufferedImage overallChart = chartGenerator.overallOccupation(
        data.months(), data.occupiedCounts(), data.totalProperties());

    List<PropertyChartBean> propertyCharts = new ArrayList<>();
    for (PropertyOccupationHistory history : data.propertyHistories()) {
      BufferedImage chart = chartGenerator.propertyOccupation(
          history.propertyName(), history.months(), history.occupied());
      propertyCharts.add(new PropertyChartBean(history.propertyName(), chart));
    }

    return pdfGenerationService.generatePdf(
        new OccupationRateTemplate(overallChart, propertyCharts,
            generationDate(), periodLabel(data.months())));
  }

  private String generationDate() {
    return LocalDate.now().format(DATE_FMT);
  }

  private String periodLabel(List<YearMonth> months) {
    if (months.isEmpty()) return "";
    YearMonth first = months.get(0);
    YearMonth last = months.get(months.size() - 1);
    return monthLabel(first) + " a " + monthLabel(last);
  }

  private String monthLabel(YearMonth ym) {
    return PT_MONTHS[ym.getMonthValue() - 1] + "/" + ym.getYear();
  }
}
