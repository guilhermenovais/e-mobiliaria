package com.guilherme.emobiliaria.person.infrastructure.service;

import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchResult;
import com.guilherme.emobiliaria.person.domain.service.AddressSearchService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ViaCepAddressSearchService implements AddressSearchService {

  private static final Logger log = LoggerFactory.getLogger(ViaCepAddressSearchService.class);

  private final HttpClient httpClient = HttpClient.newHttpClient();

  @Override
  public Optional<AddressSearchResult> search(String cep) {
    HttpRequest request = HttpRequest.newBuilder()
        .uri(URI.create("https://viacep.com.br/ws/" + cep + "/json/"))
        .GET()
        .build();
    try {
      HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
      if (response.statusCode() != 200) {
        return Optional.empty();
      }
      String body = response.body();
      if (body.contains("\"erro\"")) {
        return Optional.empty();
      }
      String uf = extractJsonValue(body, "uf");
      String city = extractJsonValue(body, "localidade");
      String neighborhood = extractJsonValue(body, "bairro");
      String address = extractJsonValue(body, "logradouro");
      if (uf == null || city == null) {
        return Optional.empty();
      }
      BrazilianState state = BrazilianState.valueOf(uf);
      return Optional.of(new AddressSearchResult(state, city, neighborhood, address));
    } catch (IOException | InterruptedException e) {
      log.warn("Failed to fetch address for CEP {}: {}", cep, e.getMessage());
      return Optional.empty();
    }
  }

  private String extractJsonValue(String json, String key) {
    Pattern pattern = Pattern.compile("\"" + key + "\"\\s*:\\s*\"([^\"]*?)\"");
    Matcher matcher = pattern.matcher(json);
    return matcher.find() ? matcher.group(1) : null;
  }
}
