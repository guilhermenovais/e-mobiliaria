package com.guilherme.emobiliaria.shared.chart;

import com.guilherme.emobiliaria.reports.domain.entity.PropertyOccupationHistory;
import jakarta.inject.Inject;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.axis.SymbolAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.LookupPaintScale;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.time.YearMonth;
import java.util.List;
import java.util.ResourceBundle;

public class ChartGenerator {

  private static final int CHART_WIDTH = 555;
  private static final int CHART_HEIGHT = 280;
  private static final int RENDER_SCALE = 4;

  private final ResourceBundle bundle;

  @Inject
  public ChartGenerator(ResourceBundle bundle) {
    this.bundle = bundle;
  }

  private String[] monthLabels(List<YearMonth> months) {
    String[] abbreviated = bundle.getString("chart.months").split(",");
    return months.stream().map(
            m -> abbreviated[m.getMonthValue() - 1] + "/" + String.format("%02d", m.getYear() % 100))
        .toArray(String[]::new);
  }

  /** Line chart showing total monthly rent earnings over time. */
  public BufferedImage monthlyEarnings(List<YearMonth> months, List<Long> centsList) {
    XYSeries series = new XYSeries(bundle.getString("chart.monthly_earnings.series"));
    for (int i = 0; i < months.size(); i++) {
      series.add(i, centsList.get(i) / 100.0);
    }
    XYSeriesCollection dataset = new XYSeriesCollection(series);
    JFreeChart chart =
        ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, false,
            false, false);
    styleTimeSeriesChart(chart, months, bundle.getString("chart.axis.value_reais"));
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setSeriesStroke(0, new BasicStroke(2.0f * RENDER_SCALE));
    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, CHART_HEIGHT * RENDER_SCALE);
  }

  /** Single-line chart: portfolio actual total rent over time. */
  public BufferedImage portfolioInflationComparison(List<YearMonth> months, List<Long> actualCents,
      List<Long> ipcaCents, List<Long> igpmCents) {
    XYSeries actual = new XYSeries(bundle.getString("chart.portfolio.series.actual"));
    for (int i = 0; i < months.size(); i++) {
      actual.add(i, actualCents.get(i) / 100.0);
    }
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(actual);

    JFreeChart chart =
        ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, false,
            false, false);
    styleTimeSeriesChart(chart, months, bundle.getString("chart.axis.value_reais"));
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x1E88E5));
    float s = RENDER_SCALE;
    renderer.setSeriesStroke(0, new BasicStroke(2.5f * s));
    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, CHART_HEIGHT * RENDER_SCALE);
  }

  /** Two-line chart: portfolio gap in reais versus IPCA and IGP-M. */
  public BufferedImage portfolioInflationGap(List<YearMonth> months, List<Long> gapVsIpcaCents,
      List<Long> gapVsIgpmCents) {
    XYSeries gapIpca = new XYSeries(bundle.getString("chart.gap.series.ipca"));
    XYSeries gapIgpm = new XYSeries(bundle.getString("chart.gap.series.igpm"));
    XYSeries baseline = new XYSeries(bundle.getString("chart.gap.series.zero"));
    for (int i = 0; i < months.size(); i++) {
      gapIpca.add(i, gapVsIpcaCents.get(i) / 100.0);
      gapIgpm.add(i, gapVsIgpmCents.get(i) / 100.0);
      baseline.add(i, 0.0);
    }
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(gapIpca);
    dataset.addSeries(gapIgpm);
    dataset.addSeries(baseline);

    JFreeChart chart =
        ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true,
            false, false);
    styleTimeSeriesChart(chart, months, bundle.getString("chart.axis.difference_reais"));
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0xE53935));
    renderer.setSeriesPaint(1, new Color(0x43A047));
    renderer.setSeriesPaint(2, new Color(0x757575));
    float s = RENDER_SCALE;
    renderer.setSeriesStroke(0, new BasicStroke(2.0f * s));
    renderer.setSeriesStroke(1, new BasicStroke(2.0f * s));
    renderer.setSeriesStroke(2,
        new BasicStroke(1.4f * s, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
            new float[] {4 * s, 4 * s}, 0.0f));
    if (chart.getLegend() != null) {
      chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    }
    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, CHART_HEIGHT * RENDER_SCALE);
  }

  /** Three-line chart: actual rent vs IPCA-adjusted vs IGP-M-adjusted initial rent. */
  public BufferedImage rentEvolution(String propertyName, List<YearMonth> months,
      List<Long> actualCents, List<Long> ipcaCents, List<Long> igpmCents) {
    XYSeries actual = new XYSeries(bundle.getString("chart.rent_evolution.series.actual"));
    XYSeries ipca = new XYSeries(bundle.getString("chart.rent_evolution.series.ipca"));
    XYSeries igpm = new XYSeries(bundle.getString("chart.rent_evolution.series.igpm"));
    for (int i = 0; i < months.size(); i++) {
      actual.add(i, actualCents.get(i) / 100.0);
      ipca.add(i, ipcaCents.get(i) / 100.0);
      igpm.add(i, igpmCents.get(i) / 100.0);
    }
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(actual);
    dataset.addSeries(ipca);
    dataset.addSeries(igpm);
    JFreeChart chart =
        ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true,
            false, false);
    styleTimeSeriesChart(chart, months, bundle.getString("chart.axis.value_reais"));
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setSeriesPaint(1, new Color(0xE53935));
    renderer.setSeriesPaint(2, new Color(0x43A047));
    float s = RENDER_SCALE;
    renderer.setSeriesStroke(0, new BasicStroke(2.0f * s));
    renderer.setSeriesStroke(1,
        new BasicStroke(1.5f * s, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
            new float[] {6 * s, 4 * s}, 0.0f));
    renderer.setSeriesStroke(2,
        new BasicStroke(1.5f * s, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
            new float[] {3 * s, 3 * s}, 0.0f));
    if (chart.getLegend() != null) {
      chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 10 * RENDER_SCALE));
    }
    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, CHART_HEIGHT * RENDER_SCALE);
  }

  /**
   * Dual-line chart: occupation rate and vacancy rate (%), plus a 3-month moving average for
   * vacancy. Y-axis is strictly 0–100.
   */
  public BufferedImage occupancyTrend(List<YearMonth> months, List<Integer> occupiedCounts,
      int totalProperties) {
    XYSeries occupationSeries =
        new XYSeries(bundle.getString("chart.occupancy_trend.series.occupation"));
    XYSeries vacancySeries = new XYSeries(bundle.getString("chart.occupancy_trend.series.vacancy"));
    XYSeries maSeries = new XYSeries(bundle.getString("chart.occupancy_trend.series.ma"));

    double[] vacancyPct = new double[months.size()];
    for (int i = 0; i < months.size(); i++) {
      double occ = totalProperties > 0 ? occupiedCounts.get(i) * 100.0 / totalProperties : 0.0;
      double vac = 100.0 - occ;
      vacancyPct[i] = vac;
      occupationSeries.add(i, occ);
      vacancySeries.add(i, vac);
    }
    for (int i = 0; i < months.size(); i++) {
      int start = Math.max(0, i - 2);
      double sum = 0;
      for (int j = start; j <= i; j++)
        sum += vacancyPct[j];
      maSeries.add(i, sum / (i - start + 1));
    }

    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(occupationSeries);
    dataset.addSeries(vacancySeries);
    dataset.addSeries(maSeries);

    JFreeChart chart =
        ChartFactory.createXYLineChart(null, null, null, dataset, PlotOrientation.VERTICAL, true,
            false, false);
    styleTimeSeriesChart(chart, months, bundle.getString("chart.axis.rate_pct"));

    XYPlot plot = chart.getXYPlot();
    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setRange(0, 100);
    rangeAxis.setTickUnit(new NumberTickUnit(25));

    float s = RENDER_SCALE;
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setSeriesStroke(0, new BasicStroke(2.5f * s));
    renderer.setSeriesPaint(1, new Color(0xEF6C00));
    renderer.setSeriesStroke(1, new BasicStroke(2.0f * s));
    renderer.setSeriesPaint(2, new Color(0xE53935));
    renderer.setSeriesStroke(2,
        new BasicStroke(1.5f * s, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f,
            new float[] {6 * s, 4 * s}, 0.0f));

    if (chart.getLegend() != null) {
      chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    }
    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, CHART_HEIGHT * RENDER_SCALE);
  }

  /** Column chart showing vacant property count per month. */
  public BufferedImage vacancyVolume(List<YearMonth> months, List<Integer> occupiedCounts,
      int totalProperties) {
    String[] abbreviated = bundle.getString("chart.months").split(",");
    int skip = months.size() > 24 ? Math.max(2, months.size() / 12) : 1;
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    String seriesName = bundle.getString("chart.vacancy_volume.series");
    for (int i = 0; i < months.size(); i++) {
      YearMonth ym = months.get(i);
      String label = (i % skip == 0) ?
          abbreviated[ym.getMonthValue() - 1] + "/" + String.format("%02d", ym.getYear() % 100) :
          " ".repeat(i);
      dataset.addValue(totalProperties - occupiedCounts.get(i), seriesName, label);
    }
    JFreeChart chart =
        ChartFactory.createBarChart(null, null, null, dataset, PlotOrientation.VERTICAL, false,
            false, false);
    chart.setBackgroundPaint(Color.WHITE);
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(new Color(0xF5F5F5));
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0xEF6C00));
    renderer.setDrawBarOutline(false);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setLabel(bundle.getString("chart.vacancy_volume.axis"));
    rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 11 * RENDER_SCALE));
    rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    rangeAxis.setRange(0, Math.max(1, totalProperties));
    rangeAxis.setTickUnit(new NumberTickUnit(Math.max(1, totalProperties / 5)));

    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, 200 * RENDER_SCALE);
  }

  /** Heatmap with properties on the Y-axis and months on the X-axis. Blue=occupied, red=vacant. */
  public BufferedImage vacancyHeatmap(List<YearMonth> months,
      List<PropertyOccupationHistory> histories) {
    int numMonths = months.size();
    int numProperties = histories.size();

    double[][] data = new double[3][numMonths * numProperties];
    int k = 0;
    for (int p = 0; p < numProperties; p++) {
      List<Boolean> occupied = histories.get(p).occupied();
      for (int m = 0; m < numMonths; m++) {
        data[0][k] = m;
        data[1][k] = p;
        data[2][k] = occupied.get(m) ? 1.0 : 0.0;
        k++;
      }
    }
    DefaultXYZDataset dataset = new DefaultXYZDataset();
    dataset.addSeries("status", data);

    LookupPaintScale paintScale = new LookupPaintScale(0.0, 1.5, new Color(0xBDBDBD));
    paintScale.add(0.0, new Color(0xEF5350));
    paintScale.add(1.0, new Color(0x42A5F5));

    XYBlockRenderer renderer = new XYBlockRenderer();
    renderer.setPaintScale(paintScale);
    renderer.setBlockWidth(1.0);
    renderer.setBlockHeight(1.0);

    String[] abbreviated = bundle.getString("chart.months").split(",");
    int skipX = numMonths > 24 ? Math.max(2, numMonths / 12) : 1;
    String[] xLabels = new String[numMonths];
    for (int i = 0; i < numMonths; i++) {
      YearMonth ym = months.get(i);
      xLabels[i] = (i % skipX == 0) ?
          abbreviated[ym.getMonthValue() - 1] + "/" + String.format("%02d", ym.getYear() % 100) :
          "";
    }

    String[] yLabels =
        histories.stream().map(PropertyOccupationHistory::propertyName).toArray(String[]::new);

    SymbolAxis xAxis = new SymbolAxis("", xLabels);
    xAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    xAxis.setGridBandsVisible(false);

    SymbolAxis yAxis = new SymbolAxis("", yLabels);
    yAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    yAxis.setGridBandsVisible(false);
    yAxis.setInverted(true);

    XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
    plot.setBackgroundPaint(Color.WHITE);
    plot.setDomainGridlinesVisible(false);
    plot.setRangeGridlinesVisible(false);

    JFreeChart chart = new JFreeChart(null, JFreeChart.DEFAULT_TITLE_FONT, plot, false);
    chart.setBackgroundPaint(Color.WHITE);

    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, 240 * RENDER_SCALE);
  }

  /** Bar chart showing occupied (100%) or vacant (0%) per month for a single property. */
  public BufferedImage propertyOccupation(String propertyName, List<YearMonth> months,
      List<Boolean> occupied) {
    String[] abbreviated = bundle.getString("chart.months").split(",");
    int skip = months.size() > 24 ? Math.max(2, months.size() / 12) : 1;
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    String seriesName = bundle.getString("chart.property_occupation.series");
    for (int i = 0; i < months.size(); i++) {
      YearMonth ym = months.get(i);
      String label = (i % skip == 0) ?
          abbreviated[ym.getMonthValue() - 1] + "/" + String.format("%02d", ym.getYear() % 100) :
          " ".repeat(i);
      dataset.addValue(occupied.get(i) ? 100 : 0, seriesName, label);
    }
    JFreeChart chart =
        ChartFactory.createBarChart(null, null, null, dataset, PlotOrientation.VERTICAL, false,
            false, false);
    chart.setBackgroundPaint(Color.WHITE);
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(new Color(0xF5F5F5));
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

    BarRenderer barRenderer = (BarRenderer) plot.getRenderer();
    barRenderer.setSeriesPaint(0, new Color(0x2196F3));
    barRenderer.setDrawBarOutline(false);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setVisible(true);
    rangeAxis.setLabel(bundle.getString("chart.property_occupation.axis"));
    rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 11 * RENDER_SCALE));
    rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    rangeAxis.setRange(0, 115);
    rangeAxis.setTickUnit(new NumberTickUnit(100));

    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, 180 * RENDER_SCALE);
  }

  private void styleTimeSeriesChart(JFreeChart chart, List<YearMonth> months,
      String rangeAxisLabel) {
    chart.setBackgroundPaint(Color.WHITE);
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(new Color(0xF5F5F5));
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

    String[] labels = monthLabels(months);
    SymbolAxis domainAxis = new SymbolAxis(bundle.getString("chart.axis.month_year"), labels);
    domainAxis.setLabelFont(new Font("Arial", Font.PLAIN, 11 * RENDER_SCALE));
    domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    domainAxis.setGridBandsVisible(false);
    plot.setDomainAxis(domainAxis);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setLabel(rangeAxisLabel);
    rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 11 * RENDER_SCALE));
    rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
  }
}
