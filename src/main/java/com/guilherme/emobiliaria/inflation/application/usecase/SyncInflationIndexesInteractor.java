package com.guilherme.emobiliaria.inflation.application.usecase;

import com.guilherme.emobiliaria.inflation.domain.entity.IndexType;
import com.guilherme.emobiliaria.inflation.domain.repository.InflationIndexRepository;
import com.guilherme.emobiliaria.inflation.infrastructure.service.IpeadataInflationFetchService;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class SyncInflationIndexesInteractor {

  private static final Logger log = LoggerFactory.getLogger(SyncInflationIndexesInteractor.class);

  private final InflationIndexRepository repository;
  private final IpeadataInflationFetchService fetchService;

  @Inject
  public SyncInflationIndexesInteractor(InflationIndexRepository repository,
      IpeadataInflationFetchService fetchService) {
    this.repository = repository;
    this.fetchService = fetchService;
  }

  public void execute() {
    for (IndexType type : IndexType.values()) {
      Optional<YearMonth> latestInDb = repository.findLatestMonth(type);
      Map<YearMonth, Double> fetched = fetchService.fetch(type);
      if (fetched.isEmpty()) continue;

      Map<YearMonth, Double> newEntries = fetched.entrySet().stream()
          .filter(e -> latestInDb.isEmpty() || e.getKey().isAfter(latestInDb.get()))
          .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

      if (!newEntries.isEmpty()) {
        repository.saveAll(type, newEntries);
        log.info("Saved {} new {} entries (up to {})", newEntries.size(), type,
            newEntries.keySet().stream().max(YearMonth::compareTo).orElseThrow());
      } else {
        log.debug("{} inflation indexes are up to date", type);
      }
    }
  }
}
