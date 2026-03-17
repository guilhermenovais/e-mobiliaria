module com.guilherme.emobiliaria {
  requires javafx.controls;
  requires javafx.fxml;

  opens com.guilherme.emobiliaria to javafx.fxml;
  exports com.guilherme.emobiliaria;

  opens com.guilherme.emobiliaria.person.domain.entity;
  opens com.guilherme.emobiliaria.person.domain.service;
  opens com.guilherme.emobiliaria.person.domain.repository;
  exports com.guilherme.emobiliaria.person.domain.entity;
  exports com.guilherme.emobiliaria.person.domain.service;
  exports com.guilherme.emobiliaria.person.domain.repository;
  exports com.guilherme.emobiliaria.shared.exception;
  exports com.guilherme.emobiliaria.shared.persistence;
}
