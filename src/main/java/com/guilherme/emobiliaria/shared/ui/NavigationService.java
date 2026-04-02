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

  private StackPane contentPane;
  private final Deque<Supplier<Node>> backStack = new ArrayDeque<>();
  private final Deque<Supplier<Node>> forwardStack = new ArrayDeque<>();
  private Supplier<Node> currentViewFactory;
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
    if (currentViewFactory != null) {
      backStack.push(currentViewFactory);
    }
    forwardStack.clear();
    currentViewFactory = viewFactory;
    loadView();
  }

  public void goBack() {
    if (backStack.isEmpty()) {
      return;
    }
    forwardStack.push(currentViewFactory);
    currentViewFactory = backStack.pop();
    loadView();
  }

  public void goForward() {
    if (forwardStack.isEmpty()) {
      return;
    }
    backStack.push(currentViewFactory);
    currentViewFactory = forwardStack.pop();
    loadView();
  }

  public boolean canGoBack() {
    return !backStack.isEmpty();
  }

  public boolean canGoForward() {
    return !forwardStack.isEmpty();
  }

  private void loadView() {
    Node node = currentViewFactory.get();
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
