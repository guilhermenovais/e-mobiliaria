package com.guilherme.emobiliaria;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.guilherme.emobiliaria.config.application.usecase.GetConfigInteractor;
import com.guilherme.emobiliaria.shared.di.AppModule;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class EMobiliariaApplication extends Application {

  private static final Logger log = LoggerFactory.getLogger(EMobiliariaApplication.class);

  private Injector injector;

  @Override
  public void init() {
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, throwable) -> log.error("Uncaught exception in thread {}", thread.getName(),
            throwable));
    injector = Guice.createInjector(new AppModule());
  }

  @Override
  public void start(Stage stage) throws IOException {
    GuiceFxmlLoader fxmlLoader = new GuiceFxmlLoader(injector);
    ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    var configOutput = injector.getInstance(GetConfigInteractor.class).execute();
    boolean needsSetup = configOutput.config().getDefaultLandlord() == null;

    if (needsSetup) {
      Scene scene = new Scene(fxmlLoader.load(
          EMobiliariaApplication.class.getResource(
              "/com/guilherme/emobiliaria/config/ui/view/initial-setup-view.fxml")));
      stage.setTitle(bundle.getString("setup.step.type.title"));
      stage.setScene(scene);
      stage.setResizable(false);
      stage.setWidth(1440);
      stage.setHeight(900);
    } else {
      // TODO: load main view
      stage.setTitle("e-Mobiliária");
      stage.setWidth(1440);
      stage.setHeight(900);
    }

    stage.show();
  }
}
