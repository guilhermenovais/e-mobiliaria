package com.guilherme.emobiliaria;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.guilherme.emobiliaria.config.application.usecase.GetConfigInteractor;
import com.guilherme.emobiliaria.inflation.application.usecase.SyncInflationIndexesInteractor;
import com.guilherme.emobiliaria.shared.di.AppModule;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.persistence.AppDataPaths;
import com.guilherme.emobiliaria.shared.update.UpdateProgressWindow;
import com.guilherme.emobiliaria.shared.update.UpdateService;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;

public class EMobiliariaApplication extends Application {

  static {
    System.setProperty("java.locale.providers", "CLDR,COMPAT");
    AppDataPaths.initializeSystemProperties();
  }

  private static final Logger log = LoggerFactory.getLogger(EMobiliariaApplication.class);

  private Injector injector;

  @Override
  public void init() {
    Thread.setDefaultUncaughtExceptionHandler(
        (thread, throwable) -> log.error("Uncaught exception in thread {}", thread.getName(),
            throwable));
    injector = Guice.createInjector(new AppModule());

    ResourceBundle updateBundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());
    UpdateProgressWindow updateWindow = new UpdateProgressWindow(updateBundle);
    Thread updateThread = new Thread(() -> new UpdateService(updateWindow).checkAndUpdate());
    updateThread.setDaemon(true);
    updateThread.setName("update-checker");
    updateThread.start();

    SyncInflationIndexesInteractor syncInflation =
        injector.getInstance(SyncInflationIndexesInteractor.class);
    Thread inflationThread = new Thread(syncInflation::execute);
    inflationThread.setDaemon(true);
    inflationThread.setName("inflation-sync");
    inflationThread.start();
  }

  @Override
  public void start(Stage stage) throws IOException {
    GuiceFxmlLoader fxmlLoader = injector.getInstance(GuiceFxmlLoader.class);
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
    } else {
      Scene scene = new Scene(fxmlLoader.load(
          EMobiliariaApplication.class.getResource(
              "/com/guilherme/emobiliaria/shared/ui/layout/view/main-view.fxml")));
      stage.setTitle("e-Mobiliária");
      stage.setScene(scene);
    }

    stage.setMaximized(true);
    stage.show();
  }
}
