package com.guilherme.emobiliaria.shared.chart;

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
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.image.BufferedImage;
import java.time.YearMonth;
import java.util.List;

public class ChartGenerator {

  private static final int CHART_WIDTH = 555;
  private static final int CHART_HEIGHT = 280;
  private static final int RENDER_SCALE = 4;

  private static final String[] PT_MONTHS =
      {"Jan", "Fev", "Mar", "Abr", "Mai", "Jun", "Jul", "Ago", "Set", "Out", "Nov", "Dez"};

  private String[] monthLabels(List<YearMonth> months) {
    return months.stream()
        .map(m -> PT_MONTHS[m.getMonthValue() - 1] + "/" + String.format("%02d", m.getYear() % 100))
        .toArray(String[]::new);
  }

  /** Line chart showing total monthly rent earnings over time. */
  public BufferedImage monthlyEarnings(List<YearMonth> months, List<Long> centsList) {
    XYSeries series = new XYSeries("Receita Total");
    for (int i = 0; i < months.size(); i++) {
      series.add(i, centsList.get(i) / 100.0);
    }
    XYSeriesCollection dataset = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(
        null,
        null, null,
        dataset,
        PlotOrientation.VERTICAL,
        false, false, false
    );
    styleTimeSeriesChart(chart, months, "Valor (R$)");
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setSeriesStroke(0, new BasicStroke(2.0f * RENDER_SCALE));
    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, CHART_HEIGHT * RENDER_SCALE);
  }

  /** Three-line chart: actual rent vs IPCA-adjusted vs IGP-M-adjusted initial rent. */
  public BufferedImage rentEvolution(String propertyName,
      List<YearMonth> months,
      List<Long> actualCents,
      List<Long> ipcaCents,
      List<Long> igpmCents) {
    XYSeries actual = new XYSeries("Aluguel real");
    XYSeries ipca = new XYSeries("Correção IPCA");
    XYSeries igpm = new XYSeries("Correção IGP-M");
    for (int i = 0; i < months.size(); i++) {
      actual.add(i, actualCents.get(i) / 100.0);
      ipca.add(i, ipcaCents.get(i) / 100.0);
      igpm.add(i, igpmCents.get(i) / 100.0);
    }
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(actual);
    dataset.addSeries(ipca);
    dataset.addSeries(igpm);
    JFreeChart chart = ChartFactory.createXYLineChart(
        null,
        null, null,
        dataset,
        PlotOrientation.VERTICAL,
        true, false, false
    );
    styleTimeSeriesChart(chart, months, "Valor (R$)");
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setSeriesPaint(1, new Color(0xE53935));
    renderer.setSeriesPaint(2, new Color(0x43A047));
    float s = RENDER_SCALE;
    renderer.setSeriesStroke(0, new BasicStroke(2.0f * s));
    renderer.setSeriesStroke(1, new BasicStroke(1.5f * s, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        10.0f, new float[]{6 * s, 4 * s}, 0.0f));
    renderer.setSeriesStroke(2, new BasicStroke(1.5f * s, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER,
        10.0f, new float[]{3 * s, 3 * s}, 0.0f));
    if (chart.getLegend() != null) {
      chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 10 * RENDER_SCALE));
    }
    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, CHART_HEIGHT * RENDER_SCALE);
  }

  /** Line chart showing occupation percentage per month (0–100%). */
  public BufferedImage overallOccupation(List<YearMonth> months,
      List<Integer> occupiedCount, int totalProperties) {
    XYSeries occupationRate = new XYSeries("Taxa de Ocupação");
    for (int i = 0; i < months.size(); i++) {
      double pct = totalProperties > 0 ? occupiedCount.get(i) * 100.0 / totalProperties : 0.0;
      occupationRate.add(i, pct);
    }
    XYSeriesCollection dataset = new XYSeriesCollection(occupationRate);
    JFreeChart chart = ChartFactory.createXYLineChart(
        null,
        null, null,
        dataset,
        PlotOrientation.VERTICAL,
        false, false, false
    );
    styleTimeSeriesChart(chart, months, "Taxa (%)");
    XYPlot plot = chart.getXYPlot();
    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setRange(0, 110);
    rangeAxis.setTickUnit(new NumberTickUnit(25));
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setSeriesStroke(0, new BasicStroke(2.5f * RENDER_SCALE));
    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, CHART_HEIGHT * RENDER_SCALE);
  }

  /** Bar chart showing occupied (100%) or vacant (0%) per month for a single property. */
  public BufferedImage propertyOccupation(String propertyName,
      List<YearMonth> months, List<Boolean> occupied) {
    int skip = months.size() > 24 ? Math.max(2, months.size() / 12) : 1;
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (int i = 0; i < months.size(); i++) {
      YearMonth ym = months.get(i);
      String label = (i % skip == 0)
          ? PT_MONTHS[ym.getMonthValue() - 1] + "/" + String.format("%02d", ym.getYear() % 100)
          : " ".repeat(i);
      dataset.addValue(occupied.get(i) ? 100 : 0, "Ocupação", label);
    }
    JFreeChart chart = ChartFactory.createBarChart(
        null,
        null, null,
        dataset,
        PlotOrientation.VERTICAL,
        false, false, false
    );
    chart.setBackgroundPaint(Color.WHITE);
    CategoryPlot plot = chart.getCategoryPlot();
    plot.setBackgroundPaint(new Color(0xF5F5F5));
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

    BarRenderer renderer = (BarRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setDrawBarOutline(false);

    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setVisible(true);
    rangeAxis.setLabel("Ocupação (%)");
    rangeAxis.setLabelFont(new Font("Arial", Font.PLAIN, 11 * RENDER_SCALE));
    rangeAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    rangeAxis.setRange(0, 115);
    rangeAxis.setTickUnit(new NumberTickUnit(100));

    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setTickLabelFont(new Font("Arial", Font.PLAIN, 9 * RENDER_SCALE));
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);

    return chart.createBufferedImage(CHART_WIDTH * RENDER_SCALE, 180 * RENDER_SCALE);
  }

  private void styleTimeSeriesChart(JFreeChart chart, List<YearMonth> months, String rangeAxisLabel) {
    chart.setBackgroundPaint(Color.WHITE);
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(new Color(0xF5F5F5));
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY);

    String[] labels = monthLabels(months);
    SymbolAxis domainAxis = new SymbolAxis("Mês/Ano", labels);
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
