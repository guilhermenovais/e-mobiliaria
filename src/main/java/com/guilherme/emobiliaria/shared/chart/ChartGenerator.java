package com.guilherme.emobiliaria.shared.chart;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.axis.NumberAxis;
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
import java.awt.image.BufferedImage;
import java.time.YearMonth;
import java.util.List;

public class ChartGenerator {

  private static final int CHART_WIDTH = 555;
  private static final int CHART_HEIGHT = 280;

  /** Line chart showing total monthly rent earnings over time. */
  public BufferedImage monthlyEarnings(List<YearMonth> months, List<Long> centsList) {
    XYSeries series = new XYSeries("Receita Total");
    for (int i = 0; i < months.size(); i++) {
      series.add(i, centsList.get(i) / 100.0);
    }
    XYSeriesCollection dataset = new XYSeriesCollection(series);
    JFreeChart chart = ChartFactory.createXYLineChart(
        "Receita Mensal Total",
        null, "R$",
        dataset,
        PlotOrientation.VERTICAL,
        false, false, false
    );
    styleTimeSeriesChart(chart, months);
    return chart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
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
        propertyName,
        null, "R$",
        dataset,
        PlotOrientation.VERTICAL,
        true, false, false
    );
    styleTimeSeriesChart(chart, months);
    XYPlot plot = chart.getXYPlot();
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setSeriesPaint(1, new Color(0xE53935));
    renderer.setSeriesPaint(2, new Color(0x43A047));
    renderer.setSeriesStroke(0, new BasicStroke(2.0f));
    renderer.setSeriesStroke(1, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{6, 4}, 0.0f));
    renderer.setSeriesStroke(2, new BasicStroke(1.5f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{3, 3}, 0.0f));
    return chart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
  }

  /** Line chart showing number of occupied properties per month vs total. */
  public BufferedImage overallOccupation(List<YearMonth> months,
      List<Integer> occupiedCount, int totalProperties) {
    XYSeries occupied = new XYSeries("Imóveis ocupados");
    XYSeries total = new XYSeries("Total de imóveis");
    for (int i = 0; i < months.size(); i++) {
      occupied.add(i, occupiedCount.get(i));
      total.add(i, totalProperties);
    }
    XYSeriesCollection dataset = new XYSeriesCollection();
    dataset.addSeries(occupied);
    dataset.addSeries(total);
    JFreeChart chart = ChartFactory.createXYLineChart(
        "Ocupação Geral",
        null, "Imóveis",
        dataset,
        PlotOrientation.VERTICAL,
        true, false, false
    );
    styleTimeSeriesChart(chart, months);
    XYPlot plot = chart.getXYPlot();
    NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
    rangeAxis.setLowerBound(0);
    rangeAxis.setUpperBound(totalProperties + 1);
    XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) plot.getRenderer();
    renderer.setSeriesPaint(0, new Color(0x2196F3));
    renderer.setSeriesPaint(1, new Color(0xBDBDBD));
    renderer.setSeriesStroke(0, new BasicStroke(2.5f));
    renderer.setSeriesStroke(1, new BasicStroke(1.0f, BasicStroke.CAP_BUTT, BasicStroke.JOIN_MITER, 10.0f, new float[]{5, 5}, 0.0f));
    return chart.createBufferedImage(CHART_WIDTH, CHART_HEIGHT);
  }

  /** Bar chart showing occupied (1) or vacant (0) per month for a single property. */
  public BufferedImage propertyOccupation(String propertyName,
      List<YearMonth> months, List<Boolean> occupied) {
    DefaultCategoryDataset dataset = new DefaultCategoryDataset();
    for (int i = 0; i < months.size(); i++) {
      String label = months.get(i).getMonth().getValue() + "/" + (months.get(i).getYear() % 100);
      dataset.addValue(occupied.get(i) ? 1 : 0, "Ocupação", label);
    }
    JFreeChart chart = ChartFactory.createBarChart(
        propertyName,
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
    rangeAxis.setVisible(false);
    CategoryAxis domainAxis = plot.getDomainAxis();
    domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
    if (months.size() > 24) {
      domainAxis.setTickLabelsVisible(false);
    }
    return chart.createBufferedImage(CHART_WIDTH, 180);
  }

  private void styleTimeSeriesChart(JFreeChart chart, List<YearMonth> months) {
    chart.setBackgroundPaint(Color.WHITE);
    XYPlot plot = chart.getXYPlot();
    plot.setBackgroundPaint(new Color(0xF5F5F5));
    plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
    plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
    NumberAxis domainAxis = new NumberAxis();
    domainAxis.setVisible(false);
    plot.setDomainAxis(domainAxis);
    if (!months.isEmpty()) {
      // Add month/year labels via custom renderer tick override is complex;
      // use domain axis range only — labels omitted for cleanliness at scale
    }
  }
}
