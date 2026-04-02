package com.guilherme.emobiliaria.shared.ui.layout;

import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import com.guilherme.emobiliaria.shared.ui.component.SidebarPane;
import jakarta.inject.Inject;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Locale;
import java.util.ResourceBundle;

public class MainController {

  private static final Logger log = LoggerFactory.getLogger(MainController.class);

  private static final String PHYSICAL_PERSON_LIST_FXML =
      "/com/guilherme/emobiliaria/person/ui/view/physical-person-list-view.fxml";
  private static final String JURIDICAL_PERSON_LIST_FXML =
      "/com/guilherme/emobiliaria/person/ui/view/juridical-person-list-view.fxml";

  private final NavigationService navigationService;
  private final GuiceFxmlLoader fxmlLoader;
  private SidebarPane sidebarPane;

  @Inject
  public MainController(NavigationService navigationService, GuiceFxmlLoader fxmlLoader) {
    this.navigationService = navigationService;
    this.fxmlLoader = fxmlLoader;
  }

  @FXML private StackPane contentPane;
  @FXML private VBox sidebarContainer;
  @FXML private Button backButton;
  @FXML private Button forwardButton;
  @FXML private HBox navBar;

  @FXML
  public void initialize() {
    ResourceBundle bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());

    sidebarPane = new SidebarPane(bundle);
    sidebarPane.setOnPhysicalPeopleAction(() -> navigateToPhysicalPersonList());
    sidebarPane.setOnJuridicalPeopleAction(() -> navigateToJuridicalPersonList());
    sidebarPane.setActiveItem("sidebar.physical_people");
    sidebarContainer.getChildren().add(sidebarPane);

    navigationService.setContentPane(contentPane);
    navigationService.setOnNavigationChanged(this::updateNavButtons);

    backButton.setText(bundle.getString("nav.back.icon"));
    forwardButton.setText(bundle.getString("nav.forward.icon"));
    backButton.getStyleClass().add("nav-icon-button");
    forwardButton.getStyleClass().add("nav-icon-button");

    backButton.setOnAction(e -> navigationService.goBack());
    forwardButton.setOnAction(e -> navigationService.goForward());

    navigateToPhysicalPersonList();
  }

  private void navigateToPhysicalPersonList() {
    if (sidebarPane != null) {
      sidebarPane.setActiveItem("sidebar.physical_people");
    }
    navigationService.navigate(() -> loadPhysicalPersonList());
  }

  private void navigateToJuridicalPersonList() {
    if (sidebarPane != null) {
      sidebarPane.setActiveItem("sidebar.juridical_people");
    }
    navigationService.navigate(() -> loadJuridicalPersonList());
  }

  private Node loadPhysicalPersonList() {
    URL resource = getClass().getResource(PHYSICAL_PERSON_LIST_FXML);
    if (resource == null) {
      log.warn("physical-person-list-view.fxml not found at {}, returning empty pane",
          PHYSICAL_PERSON_LIST_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource);
    } catch (IOException e) {
      log.error("Failed to load physical person list view", e);
      return new StackPane();
    }
  }

  private Node loadJuridicalPersonList() {
    URL resource = getClass().getResource(JURIDICAL_PERSON_LIST_FXML);
    if (resource == null) {
      log.warn("juridical-person-list-view.fxml not found at {}, returning empty pane",
          JURIDICAL_PERSON_LIST_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource);
    } catch (IOException e) {
      log.error("Failed to load juridical person list view", e);
      return new StackPane();
    }
  }

  private void updateNavButtons() {
    backButton.setDisable(!navigationService.canGoBack());
    forwardButton.setDisable(!navigationService.canGoForward());
  }
}
