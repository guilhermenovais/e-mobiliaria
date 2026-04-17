package com.guilherme.emobiliaria.config.ui.controller;

import com.guilherme.emobiliaria.config.application.input.SetConfigInput;
import com.guilherme.emobiliaria.config.application.usecase.GetConfigInteractor;
import com.guilherme.emobiliaria.config.application.usecase.SetConfigInteractor;
import com.guilherme.emobiliaria.config.domain.entity.Config;
import com.guilherme.emobiliaria.person.application.input.FindAllJuridicalPeopleInput;
import com.guilherme.emobiliaria.person.application.input.FindAllPhysicalPeopleInput;
import com.guilherme.emobiliaria.person.application.usecase.FindAllJuridicalPeopleInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindAllPhysicalPeopleInteractor;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PersonFilter;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.shared.di.GuiceFxmlLoader;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import com.guilherme.emobiliaria.shared.ui.ErrorHandler;
import jakarta.inject.Inject;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class ConfigController {

  private static final Logger log = LoggerFactory.getLogger(ConfigController.class);
  private static final String CONFIG_FXML =
      "/com/guilherme/emobiliaria/config/ui/view/config-view.fxml";

  private final GetConfigInteractor getConfig;
  private final SetConfigInteractor setConfig;
  private final FindAllPhysicalPeopleInteractor findAllPhysical;
  private final FindAllJuridicalPeopleInteractor findAllJuridical;
  private final GuiceFxmlLoader fxmlLoader;

  private ResourceBundle bundle;
  private List<PersonEntry> physicalEntries = new ArrayList<>();
  private List<PersonEntry> juridicalEntries = new ArrayList<>();

  @FXML private Label titleLabel;
  @FXML private Label subtitleLabel;
  @FXML private Label sectionTitleLabel;
  @FXML private Label sectionDescLabel;
  @FXML private Label typeLabel;
  @FXML private Label personLabel;
  @FXML private ComboBox<String> typeComboBox;
  @FXML private ComboBox<PersonEntry> personComboBox;
  @FXML private Button saveButton;
  @FXML private Label statusLabel;
  @FXML private ScrollPane contentScrollPane;

  @Inject
  public ConfigController(
      GetConfigInteractor getConfig,
      SetConfigInteractor setConfig,
      FindAllPhysicalPeopleInteractor findAllPhysical,
      FindAllJuridicalPeopleInteractor findAllJuridical,
      GuiceFxmlLoader fxmlLoader) {
    this.getConfig = getConfig;
    this.setConfig = setConfig;
    this.findAllPhysical = findAllPhysical;
    this.findAllJuridical = findAllJuridical;
    this.fxmlLoader = fxmlLoader;
  }

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(), getClass().getModule());

    titleLabel.setText(bundle.getString("config.title"));
    subtitleLabel.setText(bundle.getString("config.subtitle"));
    sectionTitleLabel.setText(bundle.getString("config.section.default_landlord"));
    sectionDescLabel.setText(bundle.getString("config.section.default_landlord.description"));
    typeLabel.setText(bundle.getString("config.field.type"));
    personLabel.setText(bundle.getString("config.field.person"));
    saveButton.setText(bundle.getString("config.button.save"));
    statusLabel.setText("");

    String physicalLabel = bundle.getString("config.type.physical");
    String juridicalLabel = bundle.getString("config.type.juridical");
    typeComboBox.setItems(FXCollections.observableArrayList(physicalLabel, juridicalLabel));
    typeComboBox.getSelectionModel().selectedItemProperty().addListener(
        (obs, old, selected) -> onTypeSelected(selected, physicalLabel));

    loadPersons();

    Config config = getConfig.execute().config();
    preselectCurrentLandlord(config, physicalLabel, juridicalLabel);
  }

  private void loadPersons() {
    PaginationInput page = new PaginationInput(1000, 0);

    physicalEntries = findAllPhysical
        .execute(new FindAllPhysicalPeopleInput(page, PersonFilter.NONE))
        .result().items().stream()
        .map(p -> new PersonEntry(p.getId(), "PHYSICAL", p.getName()))
        .toList();

    juridicalEntries = findAllJuridical
        .execute(new FindAllJuridicalPeopleInput(page, PersonFilter.NONE))
        .result().items().stream()
        .map(p -> new PersonEntry(p.getId(), "JURIDICAL", p.getCorporateName()))
        .toList();
  }

  private void onTypeSelected(String selectedLabel, String physicalLabel) {
    List<PersonEntry> entries = selectedLabel != null && selectedLabel.equals(physicalLabel)
        ? physicalEntries
        : juridicalEntries;

    PersonEntry noneEntry = new PersonEntry(null, null, bundle.getString("config.no_person_selected"));
    ObservableList<PersonEntry> items = FXCollections.observableArrayList();
    items.add(noneEntry);
    items.addAll(entries);

    personComboBox.setItems(items);
    personComboBox.getSelectionModel().selectFirst();
    statusLabel.setText("");
  }

  private void preselectCurrentLandlord(Config config, String physicalLabel, String juridicalLabel) {
    if (config.getDefaultLandlord() == null) {
      typeComboBox.getSelectionModel().selectFirst();
      return;
    }
    if (config.getDefaultLandlord() instanceof PhysicalPerson currentPerson) {
      typeComboBox.getSelectionModel().select(physicalLabel);
      personComboBox.getItems().stream()
          .filter(e -> e.id() != null && e.id().equals(currentPerson.getId()))
          .findFirst()
          .ifPresent(e -> personComboBox.getSelectionModel().select(e));
    } else if (config.getDefaultLandlord() instanceof JuridicalPerson currentPerson) {
      typeComboBox.getSelectionModel().select(juridicalLabel);
      personComboBox.getItems().stream()
          .filter(e -> e.id() != null && e.id().equals(currentPerson.getId()))
          .findFirst()
          .ifPresent(e -> personComboBox.getSelectionModel().select(e));
    }
  }

  @FXML
  private void onSave() {
    PersonEntry selected = personComboBox.getSelectionModel().getSelectedItem();
    Long id = selected != null ? selected.id() : null;
    String type = selected != null ? selected.type() : null;

    try {
      setConfig.execute(new SetConfigInput(id, type));
      statusLabel.setText(bundle.getString("config.save.success"));
      statusLabel.getStyleClass().removeAll("config-status-error");
      statusLabel.getStyleClass().add("config-status-success");
    } catch (Exception e) {
      log.error("Failed to save config", e);
      ErrorHandler.handle(e, bundle);
    }
  }

  public Node buildView() {
    URL resource = getClass().getResource(CONFIG_FXML);
    if (resource == null) {
      log.error("config-view.fxml not found at {}", CONFIG_FXML);
      return new StackPane();
    }
    try {
      return fxmlLoader.load(resource, this);
    } catch (IOException e) {
      log.error("Failed to load config view", e);
      return new StackPane();
    }
  }

  private record PersonEntry(Long id, String type, String displayName) {
    @Override
    public String toString() {
      return displayName;
    }
  }
}
