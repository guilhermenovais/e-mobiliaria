package com.guilherme.emobiliaria.property.domain.entity;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class Property {
  private Long id;
  private String name;
  private String type;
  private Purpose purpose;
  private String cemig;
  private String copasa;
  private String iptu;
  private Address address;

  private Property() {
  }

  public static Property create(String name, String type, Purpose purpose, String cemig,
      String copasa, String iptu, Address address) {
    Property property = new Property();
    property.setName(name);
    property.setType(type);
    property.setPurpose(purpose);
    property.setCemig(cemig);
    property.setCopasa(copasa);
    property.setIptu(iptu);
    property.setAddress(address);
    return property;
  }

  public static Property restore(Long id, String name, String type, Purpose purpose,
      String cemig, String copasa, String iptu, Address address) {
    Property property = create(name, type, purpose, cemig, copasa, iptu, address);
    property.setId(id);
    return property;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.isBlank()) {
      throw new BusinessException(ErrorMessage.Property.NAME_EMPTY);
    }
    this.name = name;
  }

  public String getType() {
    return type;
  }

  public void setType(String type) {
    if (type == null || type.isBlank()) {
      throw new BusinessException(ErrorMessage.Property.TYPE_EMPTY);
    }
    this.type = type;
  }

  public Purpose getPurpose() {
    return purpose;
  }

  public void setPurpose(Purpose purpose) {
    if (purpose == null) {
      throw new BusinessException(ErrorMessage.Property.PURPOSE_NULL);
    }
    this.purpose = purpose;
  }

  public String getCemig() {
    return cemig;
  }

  public void setCemig(String cemig) {
    if (cemig == null || cemig.isBlank()) {
      throw new BusinessException(ErrorMessage.Property.CEMIG_EMPTY);
    }
    this.cemig = cemig;
  }

  public String getCopasa() {
    return copasa;
  }

  public void setCopasa(String copasa) {
    if (copasa == null || copasa.isBlank()) {
      throw new BusinessException(ErrorMessage.Property.COPASA_EMPTY);
    }
    this.copasa = copasa;
  }

  public String getIptu() {
    return iptu;
  }

  public void setIptu(String iptu) {
    if (iptu == null || iptu.isBlank()) {
      throw new BusinessException(ErrorMessage.Property.IPTU_EMPTY);
    }
    this.iptu = iptu;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    if (address == null) {
      throw new BusinessException(ErrorMessage.Property.ADDRESS_NULL);
    }
    this.address = address;
  }
}
