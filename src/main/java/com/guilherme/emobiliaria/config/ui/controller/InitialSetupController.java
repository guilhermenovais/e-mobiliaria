package com.guilherme.emobiliaria.config.ui.controller;

import com.guilherme.emobiliaria.config.application.input.SetConfigInput;
import com.guilherme.emobiliaria.config.application.usecase.SetConfigInteractor;
import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreateJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.SearchAddressByCepInput;
import com.guilherme.emobiliaria.person.application.output.CreateAddressOutput;
import com.guilherme.emobiliaria.person.application.output.CreateJuridicalPersonOutput;
import com.guilherme.emobiliaria.person.application.output.CreatePhysicalPersonOutput;
import com.guilherme.emobiliaria.person.application.output.SearchAddressByCepOutput;
import com.guilherme.emobiliaria.person.application.usecase.CreateAddressInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreateJuridicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.CreatePhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchAddressByCepInteractor;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

public class InitialSetupController {

  private enum PersonType { PHYSICAL, JURIDICAL }

  // ── Injected use cases ─────────────────────────────────────────────────────

  private final CreateAddressInteractor createAddress;
  private final SearchAddressByCepInteractor searchByCep;
  private final CreatePhysicalPersonInteractor createPhysicalPerson;
  private final CreateJuridicalPersonInteractor createJuridicalPerson;
  private final SetConfigInteractor setConfig;

  @Inject
  public InitialSetupController(
      CreateAddressInteractor createAddress,
      SearchAddressByCepInteractor searchByCep,
      CreatePhysicalPersonInteractor createPhysicalPerson,
      CreateJuridicalPersonInteractor createJuridicalPerson,
      SetConfigInteractor setConfig) {
    this.createAddress = createAddress;
    this.searchByCep = searchByCep;
    this.createPhysicalPerson = createPhysicalPerson;
    this.createJuridicalPerson = createJuridicalPerson;
    this.setConfig = setConfig;
  }

  // ── FXML fields ────────────────────────────────────────────────────────────

  @FXML private Label stepTitleLabel;
  @FXML private Label stepSubtitleLabel;
  @FXML private HBox stepperContainer;
  @FXML private StackPane contentPane;
  @FXML private Button backButton;
  @FXML private Button nextButton;

  // ── Wizard state ───────────────────────────────────────────────────────────

  private int currentStep = 1;
  private PersonType selectedType = null;
  private ResourceBundle bundle;

  // Step 1 — type selection
  private PersonType typeCardSelection = null;
  private VBox physicalCard;
  private VBox juridicalCard;
  private Label typeErrorLabel;

  // Step 2A — physical person data
  private TextField nameField;
  private TextField nationalityField;
  private ComboBox<CivilState> civilStateCombo;
  private TextField occupationField;
  private TextField cpfField;
  private TextField idCardField;

  // Step 2B — company data
  private TextField corporateNameField;
  private TextField cnpjField;

  // Address forms — index 0 = landlord/company, index 1 = representative
  private final TextField[] cepField = new TextField[2];
  private final TextField[] streetField = new TextField[2];
  private final TextField[] numberField = new TextField[2];
  private final TextField[] complementField = new TextField[2];
  private final TextField[] neighborhoodField = new TextField[2];
  private final TextField[] cityField = new TextField[2];
  @SuppressWarnings("unchecked")
  private final ComboBox<BrazilianState>[] stateCombo = new ComboBox[2];
  private final Label[] cepErrorLabel = new Label[2];

  // Stepper dot nodes (max 5 steps)
  private final List<Label> stepperDots = new ArrayList<>();
  private final List<Region> stepperConnectors = new ArrayList<>();
  private final List<Label> stepperLabels = new ArrayList<>();

  // ── Initialization ─────────────────────────────────────────────────────────

  @FXML
  public void initialize() {
    bundle = ResourceBundle.getBundle("messages", Locale.getDefault(),
        getClass().getModule());
    buildStepper();
    showStep(1);
    backButton.setOnAction(e -> handleBack());
    nextButton.setOnAction(e -> handleNext());
  }

  // ── Stepper ────────────────────────────────────────────────────────────────

  private static final String[][] STEP_LABELS_PHYSICAL = {
      {"stepper.label.tipo"},
      {"stepper.label.dados"},
      {"stepper.label.endereco"}
  };

  private static final String[][] STEP_LABELS_JURIDICAL = {
      {"stepper.label.tipo"},
      {"stepper.label.empresa"},
      {"stepper.label.end_empresa"},
      {"stepper.label.representante"},
      {"stepper.label.end_repres"}
  };

  private void buildStepper() {
    stepperContainer.getChildren().clear();
    stepperDots.clear();
    stepperConnectors.clear();
    stepperLabels.clear();

    int totalSteps = (selectedType == PersonType.PHYSICAL) ? 3
        : (selectedType == PersonType.JURIDICAL) ? 5 : 3;

    String[][] labelKeys = (selectedType == PersonType.JURIDICAL)
        ? STEP_LABELS_JURIDICAL : STEP_LABELS_PHYSICAL;

    for (int i = 0; i < totalSteps; i++) {
      // Dot + label in a VBox
      Label dot = new Label(String.valueOf(i + 1));
      dot.getStyleClass().add("stepper-dot");
      dot.setMinSize(28, 28);
      dot.setMaxSize(28, 28);
      dot.setAlignment(Pos.CENTER);

      String labelText = bundle.getString(labelKeys[i][0]);
      Label label = new Label(labelText);
      label.getStyleClass().add("stepper-label");

      VBox dotBox = new VBox(5, dot, label);
      dotBox.setAlignment(Pos.CENTER);

      stepperDots.add(dot);
      stepperLabels.add(label);
      stepperContainer.getChildren().add(dotBox);

      if (i < totalSteps - 1) {
        Region connector = new Region();
        connector.getStyleClass().add("stepper-connector");
        HBox.setHgrow(connector, Priority.ALWAYS);
        connector.setTranslateY(-8); // align vertically with dot center
        stepperConnectors.add(connector);
        stepperContainer.getChildren().add(connector);
      }
    }

    updateStepperState();
  }

  private void updateStepperState() {
    int totalSteps = stepperDots.size();
    for (int i = 0; i < totalSteps; i++) {
      Label dot = stepperDots.get(i);
      Label label = stepperLabels.get(i);
      dot.getStyleClass().removeAll("stepper-dot", "stepper-dot-active", "stepper-dot-completed");
      label.getStyleClass().removeAll("stepper-label", "stepper-label-active");

      if (i + 1 < currentStep) {
        dot.setText("✓");
        dot.getStyleClass().add("stepper-dot-completed");
        label.getStyleClass().add("stepper-label-active");
      } else if (i + 1 == currentStep) {
        dot.setText(String.valueOf(i + 1));
        dot.getStyleClass().add("stepper-dot-active");
        label.getStyleClass().add("stepper-label-active");
      } else {
        dot.setText(String.valueOf(i + 1));
        dot.getStyleClass().add("stepper-dot");
        label.getStyleClass().add("stepper-label");
      }
    }
    for (int i = 0; i < stepperConnectors.size(); i++) {
      Region connector = stepperConnectors.get(i);
      connector.getStyleClass().removeAll("stepper-connector", "stepper-connector-completed");
      if (i + 1 < currentStep) {
        connector.getStyleClass().add("stepper-connector-completed");
      } else {
        connector.getStyleClass().add("stepper-connector");
      }
    }
  }

  // ── Step navigation ────────────────────────────────────────────────────────

  private void showStep(int step) {
    contentPane.getChildren().clear();

    String titleKey = titleKeyForStep(step);
    String subtitleKey = subtitleKeyForStep(step);
    stepTitleLabel.setText(bundle.getString(titleKey));
    stepSubtitleLabel.setText(bundle.getString(subtitleKey));

    backButton.setVisible(step > 1);
    backButton.setManaged(step > 1);
    backButton.setText(bundle.getString("setup.button.back"));

    boolean isLastStep = isLastStep(step);
    nextButton.setText(isLastStep
        ? bundle.getString("setup.button.finish")
        : bundle.getString("setup.button.next"));

    contentPane.getChildren().add(buildStepContent(step));
    updateStepperState();
  }

  private String titleKeyForStep(int step) {
    if (step == 1) return "setup.step.type.title";
    if (selectedType == PersonType.JURIDICAL) {
      return switch (step) {
        case 2 -> "setup.step.company.title";
        case 3 -> "setup.step.company_address.title";
        case 4 -> "setup.step.representative.title";
        case 5 -> "setup.step.representative_address.title";
        default -> "setup.step.type.title";
      };
    }
    return switch (step) {
      case 2 -> "setup.step.personal.title";
      case 3 -> "setup.step.address.title";
      default -> "setup.step.type.title";
    };
  }

  private String subtitleKeyForStep(int step) {
    if (step == 1) return "setup.step.type.subtitle";
    if (selectedType == PersonType.JURIDICAL) {
      return switch (step) {
        case 2 -> "setup.step.company.subtitle";
        case 3 -> "setup.step.company_address.subtitle";
        case 4 -> "setup.step.representative.subtitle";
        case 5 -> "setup.step.representative_address.subtitle";
        default -> "setup.step.type.subtitle";
      };
    }
    return switch (step) {
      case 2 -> "setup.step.personal.subtitle";
      case 3 -> "setup.step.address.subtitle";
      default -> "setup.step.type.subtitle";
    };
  }

  private boolean isLastStep(int step) {
    if (selectedType == PersonType.PHYSICAL) return step == 3;
    if (selectedType == PersonType.JURIDICAL) return step == 5;
    return false;
  }

  private Node buildStepContent(int step) {
    if (step == 1) return buildStep1();
    if (selectedType == PersonType.JURIDICAL) {
      return switch (step) {
        case 2 -> buildStepCompanyData();
        case 3 -> buildStepAddress(0);
        case 4 -> buildStepPersonalData(true);
        case 5 -> buildStepAddress(1);
        default -> new VBox();
      };
    }
    return switch (step) {
      case 2 -> buildStepPersonalData(false);
      case 3 -> buildStepAddress(0);
      default -> new VBox();
    };
  }

  private void handleNext() {
    if (!validateCurrentStep()) return;

    if (currentStep == 1) {
      selectedType = typeCardSelection;
      buildStepper();
    }

    if (isLastStep(currentStep)) {
      handleSubmit();
      return;
    }

    currentStep++;
    showStep(currentStep);
  }

  private void handleBack() {
    if (currentStep <= 1) return;
    currentStep--;
    showStep(currentStep);
  }

  // ── Validation ─────────────────────────────────────────────────────────────

  private boolean validateCurrentStep() {
    if (currentStep == 1) {
      if (typeCardSelection == null) {
        if (typeErrorLabel != null) {
          typeErrorLabel.setVisible(true);
          typeErrorLabel.setManaged(true);
        }
        return false;
      }
      return true;
    }

    if (selectedType == PersonType.JURIDICAL) {
      return switch (currentStep) {
        case 2 -> validateCompanyData();
        case 3 -> validateAddress(0);
        case 4 -> validatePersonalData(true);
        case 5 -> validateAddress(1);
        default -> true;
      };
    }
    return switch (currentStep) {
      case 2 -> validatePersonalData(false);
      case 3 -> validateAddress(0);
      default -> true;
    };
  }

  private boolean validatePersonalData(boolean isRepresentative) {
    boolean valid = true;
    if (isEmpty(nameField)) { markError(nameField); valid = false; }
    if (isEmpty(nationalityField)) { markError(nationalityField); valid = false; }
    if (civilStateCombo != null && civilStateCombo.getValue() == null) { markComboError(civilStateCombo); valid = false; }
    if (isEmpty(occupationField)) { markError(occupationField); valid = false; }
    if (isEmpty(cpfField)) { markError(cpfField); valid = false; }
    if (isEmpty(idCardField)) { markError(idCardField); valid = false; }
    return valid;
  }

  private boolean validateCompanyData() {
    boolean valid = true;
    if (isEmpty(corporateNameField)) { markError(corporateNameField); valid = false; }
    if (isEmpty(cnpjField)) { markError(cnpjField); valid = false; }
    return valid;
  }

  private boolean validateAddress(int idx) {
    boolean valid = true;
    if (isEmpty(cepField[idx])) { markError(cepField[idx]); valid = false; }
    if (isEmpty(streetField[idx])) { markError(streetField[idx]); valid = false; }
    if (isEmpty(numberField[idx])) { markError(numberField[idx]); valid = false; }
    if (isEmpty(neighborhoodField[idx])) { markError(neighborhoodField[idx]); valid = false; }
    if (isEmpty(cityField[idx])) { markError(cityField[idx]); valid = false; }
    if (stateCombo[idx] != null && stateCombo[idx].getValue() == null) { markComboError(stateCombo[idx]); valid = false; }
    return valid;
  }

  private boolean isEmpty(TextField field) {
    return field == null || field.getText() == null || field.getText().isBlank();
  }

  private void markError(TextField field) {
    if (field == null) return;
    field.getStyleClass().removeAll("form-input-error");
    field.getStyleClass().add("form-input-error");
    field.textProperty().addListener((obs, o, n) ->
        field.getStyleClass().removeAll("form-input-error"));
  }

  private void markComboError(ComboBox<?> combo) {
    if (combo == null) return;
    combo.getStyleClass().removeAll("form-input-error");
    combo.getStyleClass().add("form-input-error");
    combo.valueProperty().addListener((obs, o, n) ->
        combo.getStyleClass().removeAll("form-input-error"));
  }

  // ── Step 1: Type Selection ─────────────────────────────────────────────────

  private Node buildStep1() {
    VBox root = new VBox(20);

    Label prompt = new Label(bundle.getString("setup.step.type.subtitle"));
    prompt.getStyleClass().add("step1-prompt");

    physicalCard = buildTypeCard(
        bundle.getString("setup.type.physical"),
        bundle.getString("setup.type.physical.description"),
        PersonType.PHYSICAL
    );
    juridicalCard = buildTypeCard(
        bundle.getString("setup.type.juridical"),
        bundle.getString("setup.type.juridical.description"),
        PersonType.JURIDICAL
    );

    HBox cards = new HBox(16, physicalCard, juridicalCard);
    HBox.setHgrow(physicalCard, Priority.ALWAYS);
    HBox.setHgrow(juridicalCard, Priority.ALWAYS);

    typeErrorLabel = new Label(bundle.getString("setup.error.type_not_selected"));
    typeErrorLabel.getStyleClass().add("form-error-label");
    typeErrorLabel.setVisible(false);
    typeErrorLabel.setManaged(false);

    root.getChildren().addAll(prompt, cards, typeErrorLabel);

    // Restore selection if user navigated back
    if (typeCardSelection == PersonType.PHYSICAL) {
      applyCardSelection(physicalCard, juridicalCard);
    } else if (typeCardSelection == PersonType.JURIDICAL) {
      applyCardSelection(juridicalCard, physicalCard);
    }

    return root;
  }

  private VBox buildTypeCard(String title, String description, PersonType type) {
    Label titleLabel = new Label(title);
    titleLabel.getStyleClass().add("type-card-title");
    Label descLabel = new Label(description);
    descLabel.getStyleClass().add("type-card-description");
    descLabel.setWrapText(true);

    VBox card = new VBox(8, titleLabel, descLabel);
    card.getStyleClass().add("type-card");

    card.setOnMouseClicked(e -> {
      typeCardSelection = type;
      if (typeErrorLabel != null) {
        typeErrorLabel.setVisible(false);
        typeErrorLabel.setManaged(false);
      }
      VBox other = (type == PersonType.PHYSICAL) ? juridicalCard : physicalCard;
      applyCardSelection(card, other);
    });

    return card;
  }

  private void applyCardSelection(VBox selected, VBox deselected) {
    selected.getStyleClass().removeAll("type-card", "type-card-selected");
    selected.getStyleClass().add("type-card-selected");
    deselected.getStyleClass().removeAll("type-card-selected");
    if (!deselected.getStyleClass().contains("type-card")) {
      deselected.getStyleClass().add("type-card");
    }
  }

  // ── Step 2A / 4B: Personal Data ────────────────────────────────────────────

  private Node buildStepPersonalData(boolean unused) {
    // Reuse existing fields if already built (preserves state on back navigation)
    if (nameField == null) {
      nameField = styledInput();
      nationalityField = styledInput();
      civilStateCombo = buildCivilStateCombo();
      occupationField = styledInput();
      cpfField = styledInput();
      idCardField = styledInput();
    }

    GridPane grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(16);
    ColumnConstraints col1 = new ColumnConstraints();
    col1.setHgrow(Priority.ALWAYS);
    ColumnConstraints col2 = new ColumnConstraints();
    col2.setHgrow(Priority.ALWAYS);
    grid.getColumnConstraints().addAll(col1, col2);

    int row = 0;
    grid.add(formField(bundle.getString("setup.field.name"), nameField), 0, row, 2, 1);
    row++;
    grid.add(formField(bundle.getString("setup.field.nationality"), nationalityField), 0, row);
    grid.add(formField(bundle.getString("setup.field.civil_state"), civilStateCombo), 1, row);
    row++;
    grid.add(formField(bundle.getString("setup.field.occupation"), occupationField), 0, row, 2, 1);
    row++;
    grid.add(formField(bundle.getString("setup.field.cpf"), cpfField), 0, row);
    grid.add(formField(bundle.getString("setup.field.id_card"), idCardField), 1, row);

    return grid;
  }

  private ComboBox<CivilState> buildCivilStateCombo() {
    ComboBox<CivilState> combo = new ComboBox<>(
        FXCollections.observableArrayList(CivilState.values()));
    combo.getStyleClass().add("form-combo");
    combo.setMaxWidth(Double.MAX_VALUE);
    combo.setConverter(new StringConverter<>() {
      @Override public String toString(CivilState cs) {
        if (cs == null) return "";
        return bundle.getString("civil_state." + cs.name());
      }
      @Override public CivilState fromString(String s) { return null; }
    });
    return combo;
  }

  // ── Step 2B: Company Data ──────────────────────────────────────────────────

  private Node buildStepCompanyData() {
    if (corporateNameField == null) {
      corporateNameField = styledInput();
      cnpjField = styledInput();
    }

    GridPane grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(16);
    ColumnConstraints col = new ColumnConstraints();
    col.setHgrow(Priority.ALWAYS);
    grid.getColumnConstraints().add(col);

    grid.add(formField(bundle.getString("setup.field.corporate_name"), corporateNameField), 0, 0);
    grid.add(formField(bundle.getString("setup.field.cnpj"), cnpjField), 0, 1);

    return grid;
  }

  // ── Address Step ───────────────────────────────────────────────────────────

  private Node buildStepAddress(int idx) {
    if (cepField[idx] == null) {
      cepField[idx] = styledInput();
      streetField[idx] = styledInput();
      numberField[idx] = styledInput();
      complementField[idx] = styledInput();
      neighborhoodField[idx] = styledInput();
      cityField[idx] = styledInput();
      stateCombo[idx] = buildStateCombo();
      cepErrorLabel[idx] = new Label();
      cepErrorLabel[idx].getStyleClass().add("form-error-label");
      cepErrorLabel[idx].setVisible(false);
      cepErrorLabel[idx].setManaged(false);

      // Auto-filled fields start as readonly placeholders
      setReadonly(streetField[idx], true);
      setReadonly(neighborhoodField[idx], true);
      setReadonly(cityField[idx], true);
      setReadonlyCombo(stateCombo[idx], true);

      setupCepListener(idx);
    }

    GridPane grid = new GridPane();
    grid.setHgap(16);
    grid.setVgap(16);

    ColumnConstraints colWide = new ColumnConstraints();
    colWide.setHgrow(Priority.ALWAYS);
    colWide.setPercentWidth(50);
    ColumnConstraints colNarrow = new ColumnConstraints();
    colNarrow.setHgrow(Priority.ALWAYS);
    colNarrow.setPercentWidth(50);
    grid.getColumnConstraints().addAll(colWide, colNarrow);

    int row = 0;
    // CEP row with hint
    HBox cepRow = new HBox(10, cepField[idx], cepErrorLabel[idx]);
    cepRow.setAlignment(Pos.CENTER_LEFT);
    HBox.setHgrow(cepField[idx], Priority.NEVER);
    cepField[idx].setPrefWidth(140);
    VBox cepBox = formField(bundle.getString("setup.field.cep"), cepRow);
    Label hint = new Label(bundle.getString("setup.field.cep.hint"));
    hint.getStyleClass().add("form-hint");
    cepBox.getChildren().add(hint);
    grid.add(cepBox, 0, row, 2, 1);
    row++;

    // Street + Number
    grid.add(formField(bundle.getString("setup.field.street"), streetField[idx]), 0, row);
    grid.add(formField(bundle.getString("setup.field.number"), numberField[idx]), 1, row);
    row++;

    // Complement (full width)
    grid.add(formField(bundle.getString("setup.field.complement"), complementField[idx]), 0, row, 2, 1);
    row++;

    // Neighborhood + City + State
    ColumnConstraints c1 = new ColumnConstraints();
    c1.setHgrow(Priority.ALWAYS);
    c1.setPercentWidth(35);
    ColumnConstraints c2 = new ColumnConstraints();
    c2.setHgrow(Priority.ALWAYS);
    c2.setPercentWidth(40);
    ColumnConstraints c3 = new ColumnConstraints();
    c3.setHgrow(Priority.ALWAYS);
    c3.setPercentWidth(25);

    GridPane bottomRow = new GridPane();
    bottomRow.setHgap(16);
    bottomRow.getColumnConstraints().addAll(c1, c2, c3);
    bottomRow.add(formField(bundle.getString("setup.field.neighborhood"), neighborhoodField[idx]), 0, 0);
    bottomRow.add(formField(bundle.getString("setup.field.city"), cityField[idx]), 1, 0);
    bottomRow.add(formField(bundle.getString("setup.field.state"), stateCombo[idx]), 2, 0);

    grid.add(bottomRow, 0, row, 2, 1);

    return grid;
  }

  private ComboBox<BrazilianState> buildStateCombo() {
    ComboBox<BrazilianState> combo = new ComboBox<>(
        FXCollections.observableArrayList(BrazilianState.values()));
    combo.getStyleClass().add("form-combo");
    combo.setMaxWidth(Double.MAX_VALUE);
    return combo;
  }

  private void setupCepListener(int idx) {
    cepField[idx].textProperty().addListener((obs, oldVal, newVal) -> {
      String digits = newVal.replaceAll("\\D", "");
      if (digits.length() == 8) {
        cepErrorLabel[idx].setVisible(false);
        cepErrorLabel[idx].setManaged(false);
        nextButton.setDisable(true);

        Task<SearchAddressByCepOutput> task = new Task<>() {
          @Override protected SearchAddressByCepOutput call() {
            return searchByCep.execute(new SearchAddressByCepInput(digits));
          }
        };

        task.setOnSucceeded(e -> {
          SearchAddressByCepOutput result = task.getValue();
          streetField[idx].setText(result.result().address());
          neighborhoodField[idx].setText(result.result().neighborhood());
          cityField[idx].setText(result.result().city());
          stateCombo[idx].setValue(result.result().state());
          setReadonly(streetField[idx], true);
          setReadonly(neighborhoodField[idx], true);
          setReadonly(cityField[idx], true);
          setReadonlyCombo(stateCombo[idx], true);
          nextButton.setDisable(false);
        });

        task.setOnFailed(e -> {
          clearAutoFilledFields(idx);
          String msg = bundle.getString("setup.error.cep_not_found");
          cepErrorLabel[idx].setText(msg);
          cepErrorLabel[idx].setVisible(true);
          cepErrorLabel[idx].setManaged(true);
          nextButton.setDisable(false);
        });

        new Thread(task).start();
      } else if (digits.length() < 8) {
        clearAutoFilledFields(idx);
      }
    });
  }

  private void clearAutoFilledFields(int idx) {
    streetField[idx].clear();
    neighborhoodField[idx].clear();
    cityField[idx].clear();
    stateCombo[idx].setValue(null);
    setReadonly(streetField[idx], false);
    setReadonly(neighborhoodField[idx], false);
    setReadonly(cityField[idx], false);
    setReadonlyCombo(stateCombo[idx], false);
  }

  private void setReadonly(TextField field, boolean readonly) {
    field.setEditable(!readonly);
    field.getStyleClass().removeAll("form-input-readonly");
    if (readonly) field.getStyleClass().add("form-input-readonly");
  }

  private void setReadonlyCombo(ComboBox<?> combo, boolean readonly) {
    combo.setDisable(readonly);
    combo.getStyleClass().removeAll("form-input-readonly");
    if (readonly) combo.getStyleClass().add("form-input-readonly");
  }

  // ── Submission ─────────────────────────────────────────────────────────────

  private void handleSubmit() {
    nextButton.setDisable(true);
    backButton.setDisable(true);

    if (selectedType == PersonType.PHYSICAL) {
      submitPhysicalFlow();
    } else {
      submitJuridicalFlow();
    }
  }

  private void submitPhysicalFlow() {
    CreateAddressInput addressInput = buildAddressInput(0);

    Task<CreateAddressOutput> addressTask = new Task<>() {
      @Override protected CreateAddressOutput call() {
        return createAddress.execute(addressInput);
      }
    };

    addressTask.setOnSucceeded(e -> {
      long addressId = addressTask.getValue().address().getId();

      Task<CreatePhysicalPersonOutput> personTask = new Task<>() {
        @Override protected CreatePhysicalPersonOutput call() {
          return createPhysicalPerson.execute(buildPhysicalPersonInput(addressId));
        }
      };

      personTask.setOnSucceeded(pe -> {
        long personId = personTask.getValue().physicalPerson().getId();

        Task<Void> configTask = new Task<>() {
          @Override protected Void call() {
            setConfig.execute(new SetConfigInput(personId, "PHYSICAL"));
            return null;
          }
        };

        configTask.setOnSucceeded(ce -> navigateToMain());
        configTask.setOnFailed(ce -> handleError(configTask.getException()));
        new Thread(configTask).start();
      });

      personTask.setOnFailed(pe -> handleError(personTask.getException()));
      new Thread(personTask).start();
    });

    addressTask.setOnFailed(e -> handleError(addressTask.getException()));
    new Thread(addressTask).start();
  }

  private void submitJuridicalFlow() {
    CreateAddressInput companyAddressInput = buildAddressInput(0);
    CreateAddressInput reprAddressInput = buildAddressInput(1);

    Task<CreateAddressOutput> companyAddrTask = new Task<>() {
      @Override protected CreateAddressOutput call() {
        return createAddress.execute(companyAddressInput);
      }
    };

    companyAddrTask.setOnSucceeded(e -> {
      long companyAddressId = companyAddrTask.getValue().address().getId();

      Task<CreateAddressOutput> reprAddrTask = new Task<>() {
        @Override protected CreateAddressOutput call() {
          return createAddress.execute(reprAddressInput);
        }
      };

      reprAddrTask.setOnSucceeded(e2 -> {
        long reprAddressId = reprAddrTask.getValue().address().getId();

        Task<CreatePhysicalPersonOutput> reprPersonTask = new Task<>() {
          @Override protected CreatePhysicalPersonOutput call() {
            return createPhysicalPerson.execute(buildPhysicalPersonInput(reprAddressId));
          }
        };

        reprPersonTask.setOnSucceeded(e3 -> {
          long reprPersonId = reprPersonTask.getValue().physicalPerson().getId();

          Task<CreateJuridicalPersonOutput> juridicalTask = new Task<>() {
            @Override protected CreateJuridicalPersonOutput call() {
              return createJuridicalPerson.execute(new CreateJuridicalPersonInput(
                  corporateNameField.getText().trim(),
                  cnpjField.getText().trim(),
                  reprPersonId,
                  companyAddressId
              ));
            }
          };

          juridicalTask.setOnSucceeded(e4 -> {
            long juridicalId = juridicalTask.getValue().juridicalPerson().getId();

            Task<Void> configTask = new Task<>() {
              @Override protected Void call() {
                setConfig.execute(new SetConfigInput(juridicalId, "JURIDICAL"));
                return null;
              }
            };

            configTask.setOnSucceeded(e5 -> navigateToMain());
            configTask.setOnFailed(e5 -> handleError(configTask.getException()));
            new Thread(configTask).start();
          });

          juridicalTask.setOnFailed(e4 -> handleError(juridicalTask.getException()));
          new Thread(juridicalTask).start();
        });

        reprPersonTask.setOnFailed(e3 -> handleError(reprPersonTask.getException()));
        new Thread(reprPersonTask).start();
      });

      reprAddrTask.setOnFailed(e2 -> handleError(reprAddrTask.getException()));
      new Thread(reprAddrTask).start();
    });

    companyAddrTask.setOnFailed(e -> handleError(companyAddrTask.getException()));
    new Thread(companyAddrTask).start();
  }

  private CreateAddressInput buildAddressInput(int idx) {
    return new CreateAddressInput(
        cepField[idx].getText().replaceAll("\\D", ""),
        streetField[idx].getText().trim(),
        numberField[idx].getText().trim(),
        complementField[idx].getText().trim(),
        neighborhoodField[idx].getText().trim(),
        cityField[idx].getText().trim(),
        stateCombo[idx].getValue()
    );
  }

  private CreatePhysicalPersonInput buildPhysicalPersonInput(long addressId) {
    return new CreatePhysicalPersonInput(
        nameField.getText().trim(),
        nationalityField.getText().trim(),
        civilStateCombo.getValue(),
        occupationField.getText().trim(),
        cpfField.getText().trim(),
        idCardField.getText().trim(),
        addressId
    );
  }

  private void navigateToMain() {
    Stage stage = (Stage) nextButton.getScene().getWindow();
    stage.setResizable(true);
    // Main view will be loaded here in a future task
    stage.setTitle("e-Mobiliária");
  }

  private void handleError(Throwable t) {
    Platform.runLater(() -> {
      nextButton.setDisable(false);
      backButton.setDisable(false);
      String message;
      if (t instanceof BusinessException be) {
        message = bundle.getString(be.getErrorMessage().getTranslationKey());
      } else {
        message = bundle.getString("setup.error.generic");
      }
      Alert alert = new Alert(Alert.AlertType.ERROR);
      alert.setTitle("Erro");
      alert.setHeaderText(null);
      alert.setContentText(message);
      alert.showAndWait();
    });
  }

  // ── Form helpers ───────────────────────────────────────────────────────────

  private TextField styledInput() {
    TextField field = new TextField();
    field.getStyleClass().add("form-input");
    field.setMaxWidth(Double.MAX_VALUE);
    return field;
  }

  private VBox formField(String labelText, Node input) {
    Label label = new Label(labelText);
    label.getStyleClass().add("form-label");
    return new VBox(4, label, input);
  }
}
