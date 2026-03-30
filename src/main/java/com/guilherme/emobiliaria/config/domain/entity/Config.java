package com.guilherme.emobiliaria.config.domain.entity;

import com.guilherme.emobiliaria.person.domain.entity.Person;

public class Config {

  private Long id;
  private Person defaultLandlord;

  private Config() {
  }

  public static Config restore(Long id, Person defaultLandlord) {
    Config config = new Config();
    config.setId(id);
    config.setDefaultLandlord(defaultLandlord);
    return config;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public Person getDefaultLandlord() {
    return defaultLandlord;
  }

  public void setDefaultLandlord(Person defaultLandlord) {
    this.defaultLandlord = defaultLandlord;
  }
}
