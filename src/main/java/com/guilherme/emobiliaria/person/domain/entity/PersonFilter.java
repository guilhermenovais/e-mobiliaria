package com.guilherme.emobiliaria.person.domain.entity;

public record PersonFilter(PersonRole role, boolean activeContractsOnly) {

  public static final PersonFilter NONE = new PersonFilter(null, false);
}
