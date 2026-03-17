package com.guilherme.emobiliaria.person.domain.entity;

public abstract class Person {
  private Long id;

  protected Person() {}

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }
}
