package com.guilherme.emobiliaria.person.domain.entity;

import com.guilherme.emobiliaria.person.domain.service.CpfValidationService;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class PhysicalPerson extends Person {
  private String name;
  private String nationality;
  private CivilState civilState;
  private String occupation;
  private String cpf;
  private String idCardNumber;
  private Address address;

  private static final CpfValidationService cpfValidationService = new CpfValidationService();

  private PhysicalPerson() {}

  public static PhysicalPerson create(String name, String nationality, CivilState civilState,
      String occupation, String cpf, String idCardNumber, Address address) {
    PhysicalPerson person = new PhysicalPerson();
    person.setName(name);
    person.setNationality(nationality);
    person.setCivilState(civilState);
    person.setOccupation(occupation);
    person.setCpf(cpf);
    person.setIdCardNumber(idCardNumber);
    person.setAddress(address);
    return person;
  }

  public static PhysicalPerson restore(Long id, String name, String nationality,
      CivilState civilState, String occupation, String cpf, String idCardNumber, Address address) {
    PhysicalPerson person = create(name, nationality, civilState, occupation, cpf, idCardNumber,
        address);
    person.setId(id);
    return person;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    if (name == null || name.isBlank()) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.NAME_EMPTY);
    }
    if (name.length() >= 100) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.NAME_TOO_LONG);
    }
    this.name = name;
  }

  public String getNationality() {
    return nationality;
  }

  public void setNationality(String nationality) {
    if (nationality == null || nationality.isBlank()) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.NATIONALITY_EMPTY);
    }
    if (nationality.length() >= 20) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.NATIONALITY_TOO_LONG);
    }
    this.nationality = nationality;
  }

  public CivilState getCivilState() {
    return civilState;
  }

  public void setCivilState(CivilState civilState) {
    if (civilState == null) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.CIVIL_STATE_NULL);
    }
    this.civilState = civilState;
  }

  public String getOccupation() {
    return occupation;
  }

  public void setOccupation(String occupation) {
    if (occupation == null || occupation.isBlank()) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.OCCUPATION_EMPTY);
    }
    if (occupation.length() >= 100) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.OCCUPATION_TOO_LONG);
    }
    this.occupation = occupation;
  }

  public String getCpf() {
    return cpf;
  }

  public void setCpf(String cpf) {
    cpfValidationService.validate(cpf);
    this.cpf = cpf == null ? null : cpf.replaceAll("[^0-9]", "");
  }

  public String getIdCardNumber() {
    return idCardNumber;
  }

  public void setIdCardNumber(String idCardNumber) {
    if (idCardNumber == null || idCardNumber.isBlank()) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.ID_CARD_NUMBER_EMPTY);
    }
    if (idCardNumber.length() >= 20) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.ID_CARD_NUMBER_TOO_LONG);
    }
    this.idCardNumber = idCardNumber;
  }

  public Address getAddress() {
    return address;
  }

  public void setAddress(Address address) {
    if (address == null) {
      throw new BusinessException(ErrorMessage.PhysicalPerson.ADDRESS_NULL);
    }
    this.address = address;
  }
}
