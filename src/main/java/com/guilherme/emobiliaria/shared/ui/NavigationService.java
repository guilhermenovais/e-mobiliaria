package com.guilherme.emobiliaria.shared.ui;

import jakarta.inject.Inject;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.Supplier;

/**
 * Browser-style navigation service. All navigate/goBack/goForward calls must be made from
 * the JavaFX Application Thread.
 */
public class NavigationService {

  private static final class NavigationEntry {
    private final Supplier<Node> viewFactory;
    private final String sidebarItemKey;

    private NavigationEntry(Supplier<Node> viewFactory, String sidebarItemKey) {
      this.viewFactory = viewFactory;
      this.sidebarItemKey = sidebarItemKey;
    }
  }

  private StackPane contentPane;
  private final Deque<NavigationEntry> backStack = new ArrayDeque<>();
  private final Deque<NavigationEntry> forwardStack = new ArrayDeque<>();
  private NavigationEntry currentEntry;
  private Runnable onNavigationChanged;

  @Inject
  public NavigationService() {}

  public void setContentPane(StackPane contentPane) {
    this.contentPane = contentPane;
  }

  public void setOnNavigationChanged(Runnable listener) {
    this.onNavigationChanged = listener;
  }

  public void navigate(Supplier<Node> viewFactory) {
    navigate(viewFactory, null);
  }

  public void navigate(Supplier<Node> viewFactory, String sidebarItemKey) {
    if (currentEntry != null) {
      backStack.push(currentEntry);
    }
    forwardStack.clear();
    currentEntry = new NavigationEntry(viewFactory, sidebarItemKey);
    loadView();
  }

  public void goBack() {
    if (backStack.isEmpty()) {
      return;
    }
    forwardStack.push(currentEntry);
    currentEntry = backStack.pop();
    loadView();
  }

  public void goForward() {
    if (forwardStack.isEmpty()) {
      return;
    }
    backStack.push(currentEntry);
    currentEntry = forwardStack.pop();
    loadView();
  }

  public boolean canGoBack() {
    return !backStack.isEmpty();
  }

  public boolean canGoForward() {
    return !forwardStack.isEmpty();
  }

  public String getCurrentSidebarItemKey() {
    if (currentEntry == null) {
      return null;
    }
    return currentEntry.sidebarItemKey;
  }

  private void loadView() {
    Node node = currentEntry.viewFactory.get();
    if (Platform.isFxApplicationThread()) {
      contentPane.getChildren().setAll(node);
      notifyNavigationChanged();
    } else {
      Platform.runLater(() -> {
        contentPane.getChildren().setAll(node);
        notifyNavigationChanged();
      });
    }
  }

  private void notifyNavigationChanged() {
    if (onNavigationChanged != null) {
      onNavigationChanged.run();
    }
  }
}
