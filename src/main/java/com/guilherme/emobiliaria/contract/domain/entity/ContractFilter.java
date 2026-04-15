package com.guilherme.emobiliaria.contract.domain.entity;

public record ContractFilter(ContractStatus status) {

  public static final ContractFilter NONE = new ContractFilter(null);
}
