package com.guilherme.emobiliaria.inflation.domain.repository;

import com.guilherme.emobiliaria.inflation.domain.entity.IndexType;

import java.time.YearMonth;
import java.util.Map;
import java.util.Optional;

public interface InflationIndexRepository {
  Map<YearMonth, Double> findAll(IndexType type);
  Optional<YearMonth> findLatestMonth(IndexType type);
  void saveAll(IndexType type, Map<YearMonth, Double> rates);
}
