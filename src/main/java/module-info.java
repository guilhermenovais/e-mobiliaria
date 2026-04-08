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
  requires com.fasterxml.jackson.databind;
  requires org.slf4j;
  requires ch.qos.logback.classic;
  requires java.net.http;

  opens com.guilherme.emobiliaria to javafx.fxml;
  exports com.guilherme.emobiliaria;

  exports com.guilherme.emobiliaria.config.domain.entity;
  exports com.guilherme.emobiliaria.config.domain.repository;
  opens com.guilherme.emobiliaria.config.di to com.google.guice;
  opens com.guilherme.emobiliaria.config.application.usecase to com.google.guice;
  opens com.guilherme.emobiliaria.config.infrastructure.repository to com.google.guice;
  opens com.guilherme.emobiliaria.config.ui.controller to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.config.ui.component to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.person.ui.component to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.person.ui.controller to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.shared.ui.component to javafx.fxml, com.google.guice;

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

  opens com.guilherme.emobiliaria.shared.ui to com.google.guice;
  opens com.guilherme.emobiliaria.shared.ui.layout to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.shared.di to com.google.guice;
  opens com.guilherme.emobiliaria.person.di to com.google.guice;
  opens com.guilherme.emobiliaria.contract.di to com.google.guice;
  opens com.guilherme.emobiliaria.property.di to com.google.guice;
  opens com.guilherme.emobiliaria.property.ui.controller to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.property.ui.component to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.contract.ui.controller to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.contract.ui.component to javafx.fxml, com.google.guice;
  opens com.guilherme.emobiliaria.receipt.di to com.google.guice;
  opens com.guilherme.emobiliaria.receipt.ui.controller to javafx.fxml, com.google.guice;

  opens com.guilherme.emobiliaria.person.application.usecase to com.google.guice;
  opens com.guilherme.emobiliaria.contract.application.usecase to com.google.guice;
  opens com.guilherme.emobiliaria.property.application.usecase to com.google.guice;
  opens com.guilherme.emobiliaria.receipt.application.usecase to com.google.guice;

  opens com.guilherme.emobiliaria.person.infrastructure.repository to com.google.guice;
  opens com.guilherme.emobiliaria.person.infrastructure.service to com.google.guice;
  opens com.guilherme.emobiliaria.contract.infrastructure.repository to com.google.guice;
  opens com.guilherme.emobiliaria.contract.infrastructure.service to com.google.guice;
  opens com.guilherme.emobiliaria.property.infrastructure.repository to com.google.guice;
  opens com.guilherme.emobiliaria.receipt.infrastructure.repository to com.google.guice;
  opens com.guilherme.emobiliaria.receipt.infrastructure.service to com.google.guice;

  opens db.migration;
  opens reports;
  opens fonts;
  opens fonts.arial;
  opens com.guilherme.emobiliaria.shared.ui.style;
}
