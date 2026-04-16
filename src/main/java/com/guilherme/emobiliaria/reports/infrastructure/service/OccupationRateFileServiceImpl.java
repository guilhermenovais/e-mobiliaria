package com.guilherme.emobiliaria.reports.infrastructure.service;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.reports.domain.service.OccupationRateFileService;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.OccupationRateTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyChartBean;
import jakarta.inject.Inject;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OccupationRateFileServiceImpl implements OccupationRateFileService {

  private final ContractRepository contractRepository;
  private final PropertyRepository propertyRepository;
  private final PdfGenerationService pdfGenerationService;
  private final ChartGenerator chartGenerator;

  @Inject
  public OccupationRateFileServiceImpl(ContractRepository contractRepository,
      PropertyRepository propertyRepository,
      PdfGenerationService pdfGenerationService,
      ChartGenerator chartGenerator) {
    this.contractRepository = contractRepository;
    this.propertyRepository = propertyRepository;
    this.pdfGenerationService = pdfGenerationService;
    this.chartGenerator = chartGenerator;
  }

  @Override
  public byte[] generate() {
    List<Contract> contracts = contractRepository.findAll();
    List<Property> properties = propertyRepository.findAll();

    if (contracts.isEmpty()) {
      BufferedImage emptyChart = chartGenerator.overallOccupation(List.of(), List.of(),
          properties.size());
      OccupationRateTemplate template = new OccupationRateTemplate(emptyChart, List.of());
      return pdfGenerationService.generatePdf(template);
    }

    YearMonth earliest = contracts.stream()
        .map(c -> YearMonth.from(c.getStartDate()))
        .min(Comparator.naturalOrder())
        .orElseThrow();
    YearMonth current = YearMonth.now();
    List<YearMonth> months = monthRange(earliest, current);
    int totalProperties = properties.size();

    // Overall occupation chart
    List<Integer> occupiedCounts = months.stream()
        .map(m -> occupiedPropertyCount(contracts, m))
        .collect(Collectors.toList());
    BufferedImage overallChart = chartGenerator.overallOccupation(months, occupiedCounts,
        totalProperties);

    // Per-property occupation charts
    Map<Long, List<Contract>> byProperty = contracts.stream()
        .collect(Collectors.groupingBy(c -> c.getProperty().getId()));

    List<PropertyChartBean> propertyCharts = new ArrayList<>();

    for (Property property : properties) {
      List<Contract> propContracts = byProperty.getOrDefault(property.getId(), List.of());
      if (propContracts.isEmpty()) {
        continue;
      }
      YearMonth propEarliest = propContracts.stream()
          .map(c -> YearMonth.from(c.getStartDate()))
          .min(Comparator.naturalOrder())
          .orElseThrow();
      List<YearMonth> propMonths = monthRange(propEarliest, current);
      List<Boolean> occupied = propMonths.stream()
          .map(m -> isPropertyOccupiedInMonth(propContracts, m))
          .collect(Collectors.toList());
      BufferedImage chart = chartGenerator.propertyOccupation(property.getName(), propMonths,
          occupied);
      propertyCharts.add(new PropertyChartBean(property.getName(), chart));
    }

    propertyCharts.sort(Comparator.comparing(PropertyChartBean::propertyName));
    OccupationRateTemplate template = new OccupationRateTemplate(overallChart, propertyCharts);
    return pdfGenerationService.generatePdf(template);
  }

  private List<YearMonth> monthRange(YearMonth start, YearMonth end) {
    List<YearMonth> range = new ArrayList<>();
    YearMonth m = start;
    while (!m.isAfter(end)) {
      range.add(m);
      m = m.plusMonths(1);
    }
    return range;
  }

  private int occupiedPropertyCount(List<Contract> contracts, YearMonth month) {
    return (int) contracts.stream()
        .collect(Collectors.groupingBy(c -> c.getProperty().getId()))
        .values().stream()
        .filter(propContracts -> propContracts.stream().anyMatch(c -> isActiveInMonth(c, month)))
        .count();
  }

  private boolean isPropertyOccupiedInMonth(List<Contract> propContracts, YearMonth month) {
    return propContracts.stream().anyMatch(c -> isActiveInMonth(c, month));
  }

  private boolean isActiveInMonth(Contract contract, YearMonth month) {
    LocalDate firstDay = month.atDay(1);
    LocalDate lastDay = month.atEndOfMonth();
    LocalDate start = contract.getStartDate();
    LocalDate end = start.plus(contract.getDuration());
    return !start.isAfter(lastDay) && !end.isBefore(firstDay);
  }
}
