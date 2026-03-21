package com.guilherme.emobiliaria.contract.application.input;

public record PersonReference(Long id, PersonType type) {
  public enum PersonType {
    PHYSICAL, JURIDICAL
  }
}
