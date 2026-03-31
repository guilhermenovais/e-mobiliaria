package com.guilherme.emobiliaria.person.domain.entity;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class Address {
  private Long id;
  private String cep;
  private String address;
  private String number;
  private String complement;
  private String neighborhood;
  private String city;
  private BrazilianState state;

  private Address() {}

  public static Address create(String cep, String address, String number, String complement,
      String neighborhood, String city, BrazilianState state) {
    Address a = new Address();
    a.setCep(cep);
    a.setAddress(address);
    a.setNumber(number);
    a.setComplement(complement);
    a.setNeighborhood(neighborhood);
    a.setCity(city);
    a.setState(state);
    return a;
  }

  public static Address restore(Long id, String cep, String address, String number,
      String complement, String neighborhood, String city, BrazilianState state) {
    Address a = create(cep, address, number, complement, neighborhood, city, state);
    a.setId(id);
    return a;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getCep() {
    return cep;
  }

  public void setCep(String cep) {
    if (cep == null || cep.isBlank()) {
      throw new BusinessException(ErrorMessage.Address.CEP_REQUIRED);
    }
    String normalized = cep.replaceAll("-", "");
    if (!normalized.matches("\\d{8}")) {
      throw new BusinessException(ErrorMessage.Address.CEP_INVALID);
    }
    this.cep = normalized;
  }

  public String getAddress() {
    return address;
  }

  public void setAddress(String address) {
    if (address == null || address.isBlank()) {
      throw new BusinessException(ErrorMessage.Address.STREET_REQUIRED);
    }
    if (address.length() < 3 || address.length() > 150) {
      throw new BusinessException(ErrorMessage.Address.STREET_INVALID_LENGTH);
    }
    if (!address.matches("[\\p{L}0-9 .,\\-/']+")) {
      throw new BusinessException(ErrorMessage.Address.STREET_INVALID_CHARACTERS);
    }
    this.address = address;
  }

  public String getNumber() {
    return number;
  }

  public void setNumber(String number) {
    if (number == null || number.isBlank()) {
      throw new BusinessException(ErrorMessage.Address.NUMBER_REQUIRED);
    }
    if (number.length() > 10) {
      throw new BusinessException(ErrorMessage.Address.NUMBER_TOO_LONG);
    }
    if (!number.matches("(?i)(S/?N|[0-9A-Z]+([\\-/][0-9A-Z]+)*)")) {
      throw new BusinessException(ErrorMessage.Address.NUMBER_INVALID);
    }
    this.number = number;
  }

  public String getComplement() {
    return complement;
  }

  public void setComplement(String complement) {
    if (complement != null) {
      if (complement.isBlank()) {
        throw new BusinessException(ErrorMessage.Address.COMPLEMENT_WHITESPACE_ONLY);
      }
      if (complement.length() > 100) {
        throw new BusinessException(ErrorMessage.Address.COMPLEMENT_TOO_LONG);
      }
      if (!complement.matches("[\\p{L}0-9 .,\\-/#]+")) {
        throw new BusinessException(ErrorMessage.Address.COMPLEMENT_INVALID_CHARACTERS);
      }
    }
    this.complement = complement;
  }

  public String getNeighborhood() {
    return neighborhood;
  }

  public void setNeighborhood(String neighborhood) {
    if (neighborhood == null || neighborhood.isBlank()) {
      throw new BusinessException(ErrorMessage.Address.NEIGHBORHOOD_REQUIRED);
    }
    if (neighborhood.length() < 2 || neighborhood.length() > 100) {
      throw new BusinessException(ErrorMessage.Address.NEIGHBORHOOD_INVALID_LENGTH);
    }
    if (!neighborhood.matches("[\\p{L} ()\\-']+")) {
      throw new BusinessException(ErrorMessage.Address.NEIGHBORHOOD_INVALID_CHARACTERS);
    }
    this.neighborhood = neighborhood;
  }

  public String getCity() {
    return city;
  }

  public void setCity(String city) {
    if (city == null || city.isBlank()) {
      throw new BusinessException(ErrorMessage.Address.CITY_REQUIRED);
    }
    if (city.length() < 2 || city.length() > 100) {
      throw new BusinessException(ErrorMessage.Address.CITY_INVALID_LENGTH);
    }
    if (!city.matches("[\\p{L} \\-']+")) {
      throw new BusinessException(ErrorMessage.Address.CITY_INVALID_CHARACTERS);
    }
    this.city = city;
  }

  public BrazilianState getState() {
    return state;
  }

  public void setState(BrazilianState state) {
    if (state == null) {
      throw new BusinessException(ErrorMessage.Address.STATE_REQUIRED);
    }
    this.state = state;
  }
}
