module com.guilherme.emobiliaria {
  requires javafx.controls;
  requires javafx.fxml;
  requires net.sf.jasperreports.core;
  requires java.sql;
  requires com.google.guice;
  requires jakarta.inject;
  requires com.zaxxer.hikari;
  requires com.h2database;
  requires flyway.core;
  requires org.slf4j;
  requires java.net.http;

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

  opens com.guilherme.emobiliaria.shared.pdf;
  opens com.guilherme.emobiliaria.shared.pdf.templates;

  opens com.guilherme.emobiliaria.shared.di to com.google.guice;
  opens com.guilherme.emobiliaria.person.di to com.google.guice;
  opens com.guilherme.emobiliaria.contract.di to com.google.guice;
  opens com.guilherme.emobiliaria.property.di to com.google.guice;
  opens com.guilherme.emobiliaria.receipt.di to com.google.guice;

  opens com.guilherme.emobiliaria.person.application.usecase to com.google.guice;
  opens com.guilherme.emobiliaria.contract.application.usecase to com.google.guice;
  opens com.guilherme.emobiliaria.property.application.usecase to com.google.guice;
  opens com.guilherme.emobiliaria.receipt.application.usecase to com.google.guice;

  opens com.guilherme.emobiliaria.person.infrastructure.repository to com.google.guice;
  opens com.guilherme.emobiliaria.person.infrastructure.service to com.google.guice;
  opens com.guilherme.emobiliaria.contract.infrastructure.repository to com.google.guice;
  opens com.guilherme.emobiliaria.contract.infrastructure.service to com.google.guice;
  opens com.guilherme.emobiliaria.property.infrastructure.repository to com.google.guice;
}
