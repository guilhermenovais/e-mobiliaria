package com.guilherme.emobiliaria;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.guilherme.emobiliaria.shared.di.AppModule;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class EMobiliariaApplication extends Application {

  private Injector injector;

  @Override
  public void init() {
    injector = Guice.createInjector(new AppModule());
  }

  @Override
  public void start(Stage stage) throws IOException {
    GuiceFxmlLoader fxmlLoader = new GuiceFxmlLoader(injector);
    Scene scene =
        new Scene(fxmlLoader.load(EMobiliariaApplication.class.getResource("hello-view.fxml")), 320,
            240);
    stage.setTitle("Hello!");
    stage.setScene(scene);
    stage.show();
  }
}
