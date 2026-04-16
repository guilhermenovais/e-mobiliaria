package com.guilherme.emobiliaria.reports.infrastructure.service;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.reports.domain.service.RentEvolutionFileService;
import com.guilherme.emobiliaria.shared.chart.ChartGenerator;
import com.guilherme.emobiliaria.shared.chart.InflationIndexes;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.PropertyChartBean;
import com.guilherme.emobiliaria.shared.pdf.templates.RentEvolutionTemplate;
import jakarta.inject.Inject;

import java.awt.image.BufferedImage;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RentEvolutionFileServiceImpl implements RentEvolutionFileService {

  private final ContractRepository contractRepository;
  private final PdfGenerationService pdfGenerationService;
  private final ChartGenerator chartGenerator;

  @Inject
  public RentEvolutionFileServiceImpl(ContractRepository contractRepository,
      PdfGenerationService pdfGenerationService,
      ChartGenerator chartGenerator) {
    this.contractRepository = contractRepository;
    this.pdfGenerationService = pdfGenerationService;
    this.chartGenerator = chartGenerator;
  }

  @Override
  public byte[] generate() {
    List<Contract> contracts = contractRepository.findAll();

    if (contracts.isEmpty()) {
      BufferedImage emptyChart = chartGenerator.monthlyEarnings(List.of(), List.of());
      RentEvolutionTemplate template = new RentEvolutionTemplate(emptyChart, List.of());
      return pdfGenerationService.generatePdf(template);
    }

    YearMonth earliest = contracts.stream()
        .map(c -> YearMonth.from(c.getStartDate()))
        .min(Comparator.naturalOrder())
        .orElseThrow();
    YearMonth current = YearMonth.now();

    List<YearMonth> months = monthRange(earliest, current);

    // Monthly earnings chart
    List<Long> earnings = months.stream()
        .map(m -> totalRentForMonth(contracts, m))
        .collect(Collectors.toList());
    BufferedImage earningsChart = chartGenerator.monthlyEarnings(months, earnings);

    // Per-property rent evolution charts
    Map<Long, List<Contract>> byProperty = contracts.stream()
        .collect(Collectors.groupingBy(c -> c.getProperty().getId()));

    List<PropertyChartBean> propertyCharts = new ArrayList<>();
    for (Map.Entry<Long, List<Contract>> entry : byProperty.entrySet()) {
      List<Contract> propContracts = entry.getValue().stream()
          .sorted(Comparator.comparing(Contract::getStartDate))
          .toList();
      Property property = propContracts.getFirst().getProperty();

      YearMonth propEarliest = YearMonth.from(propContracts.getFirst().getStartDate());
      List<YearMonth> propMonths = monthRange(propEarliest, current);

      long initialRent = initialRentCents(propContracts);
      YearMonth initialMonth = propEarliest;

      List<Long> actualRents = new ArrayList<>();
      List<Long> ipcaRents = new ArrayList<>();
      List<Long> igpmRents = new ArrayList<>();

      for (YearMonth m : propMonths) {
        actualRents.add(actualRentForMonth(propContracts, m));
        ipcaRents.add(indexAdjustedRent(initialRent, initialMonth, m, InflationIndexes.IPCA));
        igpmRents.add(indexAdjustedRent(initialRent, initialMonth, m, InflationIndexes.IGP_M));
      }

      BufferedImage chart = chartGenerator.rentEvolution(
          property.getName(), propMonths, actualRents, ipcaRents, igpmRents);
      propertyCharts.add(new PropertyChartBean(property.getName(), chart));
    }

    propertyCharts.sort(Comparator.comparing(PropertyChartBean::propertyName));
    RentEvolutionTemplate template = new RentEvolutionTemplate(earningsChart, propertyCharts);
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

  private long totalRentForMonth(List<Contract> contracts, YearMonth month) {
    return contracts.stream()
        .filter(c -> isActiveInMonth(c, month))
        .mapToLong(Contract::getRent)
        .sum();
  }

  private long actualRentForMonth(List<Contract> propContracts, YearMonth month) {
    return propContracts.stream()
        .filter(c -> isActiveInMonth(c, month))
        .mapToLong(Contract::getRent)
        .findFirst()
        .orElse(0L);
  }

  private boolean isActiveInMonth(Contract contract, YearMonth month) {
    LocalDate firstDay = month.atDay(1);
    LocalDate lastDay = month.atEndOfMonth();
    LocalDate start = contract.getStartDate();
    LocalDate end = start.plus(contract.getDuration());
    return !start.isAfter(lastDay) && !end.isBefore(firstDay);
  }

  private long initialRentCents(List<Contract> propContracts) {
    return propContracts.getFirst().getRent();
  }

  private long indexAdjustedRent(long initialRent, YearMonth initialMonth, YearMonth targetMonth,
      Map<YearMonth, Double> index) {
    double multiplier = 1.0;
    YearMonth m = initialMonth.plusMonths(1);
    while (!m.isAfter(targetMonth)) {
      double rate = index.getOrDefault(m, 0.0);
      multiplier *= (1.0 + rate);
      m = m.plusMonths(1);
    }
    return Math.round(initialRent * multiplier);
  }
}
