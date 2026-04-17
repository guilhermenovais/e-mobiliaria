package com.guilherme.emobiliaria.inflation.infrastructure.service;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guilherme.emobiliaria.inflation.domain.entity.IndexType;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IpeadataInflationFetchService {

  private static final Logger log = LoggerFactory.getLogger(IpeadataInflationFetchService.class);

  private static final String IPCA_URL =
      "http://www.ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='PRECOS12_IPCAG12')";
  private static final String IGP_M_URL =
      "http://www.ipeadata.gov.br/api/odata4/ValoresSerie(SERCODIGO='IGP12_IGPMG12')";

  private final HttpClient httpClient;
  private final ObjectMapper objectMapper;

  @Inject
  public IpeadataInflationFetchService() {
    this.httpClient = HttpClient.newBuilder()
        .connectTimeout(Duration.ofSeconds(15))
        .followRedirects(HttpClient.Redirect.ALWAYS)
        .build();
    this.objectMapper = new ObjectMapper()
        .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
  }

  public Map<YearMonth, Double> fetch(IndexType type) {
    String url = type == IndexType.IPCA ? IPCA_URL : IGP_M_URL;
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(URI.create(url))
          .timeout(Duration.ofSeconds(30))
          .GET()
          .build();
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      IpeadataResponse body = objectMapper.readValue(response.body(), IpeadataResponse.class);
      Map<YearMonth, Double> rates = new HashMap<>();
      for (IpeadataEntry entry : body.value()) {
        if (entry.valValor() == null) continue;
        YearMonth ym = YearMonth.from(OffsetDateTime.parse(entry.valData()));
        rates.put(ym, entry.valValor() / 100.0);
      }
      return rates;
    } catch (Exception e) {
      log.warn("Failed to fetch {} inflation indexes from IPEADATA: {}", type, e.getMessage());
      return Map.of();
    }
  }

  record IpeadataResponse(@JsonProperty("value") List<IpeadataEntry> value) {}

  record IpeadataEntry(
      @JsonProperty("VALDATA") String valData,
      @JsonProperty("VALVALOR") Double valValor) {}
}
