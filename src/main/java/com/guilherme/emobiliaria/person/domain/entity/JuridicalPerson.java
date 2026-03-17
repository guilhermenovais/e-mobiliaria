package com.guilherme.emobiliaria.person.domain.entity;

import com.guilherme.emobiliaria.person.domain.service.CnpjValidationService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class JuridicalPerson extends Person {
  private String corporateName;
  private String cnpj;
  private PhysicalPerson representative;
  private Address address;

  private static final CnpjValidationService cnpjValidationService = new CnpjValidationService();

  private JuridicalPerson() {}

  public static JuridicalPerson create(String corporateName, String cnpj,
      PhysicalPerson representative, Address address) {
    JuridicalPerson person = new JuridicalPerson();
    person.setCorporateName(corporateName);
    person.setCnpj(cnpj);
    person.setRepresentative(representative);
    person.setAddress(address);
    return person;
  }

  public static JuridicalPerson restore(Long id, String corporateName, String cnpj,
      PhysicalPerson representative, Address address) {
    JuridicalPerson person = create(corporateName, cnpj, representative, address);
    person.setId(id);
    return person;
  }

  public String getCorporateName() {
    return corporateName;
  }

  public void setCorporateName(String corporateName) {
    if (corporateName == null || corporateName.isBlank()) {
      throw new BusinessException(ErrorMessage.JuridicalPerson.CORPORATE_NAME_EMPTY);
    }
    if (corporateName.length() >= 100) {
      throw new BusinessException(ErrorMessage.JuridicalPerson.CORPORATE_NAME_TOO_LONG);
    }
    this.corporateName = corporateName;
  }

  public String getCnpj() {
    return cnpj;
  }

  public void setCnpj(String cnpj) {
    cnpjValidationService.validate(cnpj);
    this.cnpj = cnpj == null ? null : cnpj.replaceAll("[^0-9]", "");
  }

  public PhysicalPerson getRepresentative() {
    return representative;
  }

  public void setRepresentative(PhysicalPerson representative) {
    if (representative == null) {
      throw new BusinessException(ErrorMessage.JuridicalPerson.REPRESENTATIVE_NULL);
    }
    this.representative = representative;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    if (address == null) {
      throw new BusinessException(ErrorMessage.JuridicalPerson.ADDRESS_NULL);
    }
    this.address = address;
  }
}
