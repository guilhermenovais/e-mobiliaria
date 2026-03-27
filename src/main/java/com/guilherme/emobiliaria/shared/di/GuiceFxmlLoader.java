package com.guilherme.emobiliaria.shared.di;

import com.google.inject.Injector;
import javafx.fxml.FXMLLoader;

import java.io.IOException;
import java.net.URL;

public class GuiceFxmlLoader {

  private final Injector injector;

  public GuiceFxmlLoader(Injector injector) {
    this.injector = injector;
  }

  public <T> T load(URL fxmlResource) throws IOException {
    FXMLLoader loader = new FXMLLoader(fxmlResource);
    loader.setControllerFactory(injector::getInstance);
    return loader.load();
  }
}
