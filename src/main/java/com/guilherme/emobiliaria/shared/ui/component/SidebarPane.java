package com.guilherme.emobiliaria.shared.ui.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class SidebarPane extends VBox {

  private static final String CSS_SIDEBAR_ITEM = "sidebar-item";
  private static final String CSS_SIDEBAR_ITEM_ACTIVE = "sidebar-item-active";
  private static final String CSS_SIDEBAR_BRAND_CONTAINER = "sidebar-brand-container";
  private static final String CSS_SIDEBAR_BRAND = "sidebar-brand";
  private static final String CSS_SIDEBAR_MENU = "sidebar-menu";

  private static final String KEY_BRAND = "sidebar.brand";
  private static final String KEY_DASHBOARD = "sidebar.dashboard";
  private static final String KEY_PHYSICAL_PEOPLE = "sidebar.physical_people";
  private static final String KEY_JURIDICAL_PEOPLE = "sidebar.juridical_people";
  private static final String KEY_PROPERTIES = "sidebar.properties";
  private static final String KEY_CONTRACTS = "sidebar.contracts";
  private static final String KEY_RECEIPTS = "sidebar.receipts";
  private static final String KEY_REPORTS = "sidebar.reports";
  private static final String KEY_CONFIG = "sidebar.config";

  private final Map<String, Button> entries = new LinkedHashMap<>();

  public SidebarPane(ResourceBundle bundle) {
    getStyleClass().add("sidebar");

    Label brandLabel = new Label(bundle.getString(KEY_BRAND));
    brandLabel.getStyleClass().add(CSS_SIDEBAR_BRAND);
    VBox brandContainer = new VBox(brandLabel);
    brandContainer.getStyleClass().add(CSS_SIDEBAR_BRAND_CONTAINER);

    VBox menuContainer = new VBox();
    menuContainer.getStyleClass().add(CSS_SIDEBAR_MENU);
    VBox.setVgrow(menuContainer, Priority.ALWAYS);

    getChildren().addAll(brandContainer, menuContainer);

    String[] keys = {
        KEY_DASHBOARD,
        KEY_PHYSICAL_PEOPLE,
        KEY_JURIDICAL_PEOPLE,
        KEY_PROPERTIES,
        KEY_CONTRACTS,
        KEY_RECEIPTS,
        KEY_REPORTS,
        KEY_CONFIG
    };

    for (String key : keys) {
      Button button = new Button(bundle.getString(key));
      button.getStyleClass().add(CSS_SIDEBAR_ITEM);
      button.setMaxWidth(Double.MAX_VALUE);
      button.setAlignment(Pos.CENTER_LEFT);

      boolean enabled = KEY_DASHBOARD.equals(key) || KEY_PHYSICAL_PEOPLE.equals(key)
          || KEY_JURIDICAL_PEOPLE.equals(key) || KEY_PROPERTIES.equals(key)
          || KEY_CONTRACTS.equals(key) || KEY_RECEIPTS.equals(key) || KEY_REPORTS.equals(key)
          || KEY_CONFIG.equals(key);
      button.setDisable(!enabled);

      entries.put(key, button);
      VBox.setMargin(button, new Insets(1, 12, 1, 12));
      menuContainer.getChildren().add(button);
    }
  }

  public void setActiveItem(String key) {
    entries.forEach((k, button) ->
        button.getStyleClass().remove(CSS_SIDEBAR_ITEM_ACTIVE));
    Button active = entries.get(key);
    if (active != null) {
      active.getStyleClass().add(CSS_SIDEBAR_ITEM_ACTIVE);
    }
  }

  public void setOnDashboardAction(Runnable r) {
    Button button = entries.get(KEY_DASHBOARD);
    if (button != null) {
      button.setOnAction(e -> r.run());
    }
  }

  public void setOnPhysicalPeopleAction(Runnable r) {
    Button button = entries.get(KEY_PHYSICAL_PEOPLE);
    if (button != null) {
      button.setOnAction(e -> r.run());
    }
  }

  public void setOnJuridicalPeopleAction(Runnable r) {
    Button button = entries.get(KEY_JURIDICAL_PEOPLE);
    if (button != null) {
      button.setOnAction(e -> r.run());
    }
  }

  public void setOnPropertiesAction(Runnable r) {
    Button button = entries.get(KEY_PROPERTIES);
    if (button != null) {
      button.setOnAction(e -> r.run());
    }
  }

  public void setOnContractsAction(Runnable r) {
    Button button = entries.get(KEY_CONTRACTS);
    if (button != null) {
      button.setOnAction(e -> r.run());
    }
  }

  public void setOnReceiptsAction(Runnable r) {
    Button button = entries.get(KEY_RECEIPTS);
    if (button != null) {
      button.setOnAction(e -> r.run());
    }
  }

  public void setOnReportsAction(Runnable r) {
    Button button = entries.get(KEY_REPORTS);
    if (button != null) {
      button.setOnAction(e -> r.run());
    }
  }

  public void setOnConfigAction(Runnable r) {
    Button button = entries.get(KEY_CONFIG);
    if (button != null) {
      button.setOnAction(e -> r.run());
    }
  }
}
