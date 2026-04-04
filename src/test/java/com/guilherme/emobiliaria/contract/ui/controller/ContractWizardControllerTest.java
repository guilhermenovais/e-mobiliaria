package com.guilherme.emobiliaria.contract.ui.controller;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.ui.component.ContractPaymentAccountStepPane;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractWizardControllerTest {

  @BeforeAll
  static void startFx() throws InterruptedException {
    CountDownLatch latch = new CountDownLatch(1);
    try {
      Platform.startup(latch::countDown);
    } catch (IllegalStateException ignored) {
      latch.countDown();
    }
    assertTrue(latch.await(5, TimeUnit.SECONDS), "JavaFX platform did not start");
  }

  private static <T> T onFX(java.util.concurrent.Callable<T> action) throws Exception {
    CompletableFuture<T> future = new CompletableFuture<>();
    Platform.runLater(() -> {
      try {
        future.complete(action.call());
      } catch (Throwable t) {
        future.completeExceptionally(t);
      }
    });
    return future.get(5, TimeUnit.SECONDS);
  }

  private static ResourceBundle messages() {
    return ResourceBundle.getBundle("messages", Locale.getDefault(),
        ContractWizardController.class.getModule());
  }

  private static void setPrivateField(ContractPaymentAccountStepPane pane, String fieldName,
      Object value) throws Exception {
    var field = ContractPaymentAccountStepPane.class.getDeclaredField(fieldName);
    field.setAccessible(true);
    Object target = field.get(pane);

    switch (fieldName) {
      case "newAccountCheckBox" ->
          ((javafx.scene.control.CheckBox) target).setSelected((Boolean) value);
      case "bankField", "branchField", "accountNumberField", "pixField" ->
          ((javafx.scene.control.TextField) target).setText((String) value);
      default -> throw new IllegalArgumentException("Unsupported field: " + fieldName);
    }
  }

  @Test
  @DisplayName("Should return selected account for review when using existing account")
  void shouldReturnSelectedAccountForReviewWhenUsingExistingAccount() throws Exception {
    PaymentAccount existing = PaymentAccount.restore(7L, "Banco XPTO", "1234", "998877", "pix-x");

    PaymentAccount resolved = onFX(() -> {
      ContractPaymentAccountStepPane pane = new ContractPaymentAccountStepPane(messages());
      pane.setAccounts(java.util.List.of(existing));
      pane.populate(existing);
      return ContractWizardController.resolveAccountForReview(pane);
    });

    assertSame(existing, resolved);
  }

  @Test
  @DisplayName("Should return preview account for review when using new account")
  void shouldReturnPreviewAccountForReviewWhenUsingNewAccount() throws Exception {
    PaymentAccount resolved = onFX(() -> {
      ContractPaymentAccountStepPane pane = new ContractPaymentAccountStepPane(messages());
      setPrivateField(pane, "newAccountCheckBox", true);
      setPrivateField(pane, "bankField", "Banco Novo");
      setPrivateField(pane, "branchField", "0001");
      setPrivateField(pane, "accountNumberField", "12345-6");
      setPrivateField(pane, "pixField", "nova-chave");
      return ContractWizardController.resolveAccountForReview(pane);
    });

    assertNotNull(resolved);
    assertNull(resolved.getId());
    assertEquals("Banco Novo", resolved.getBank());
    assertEquals("0001", resolved.getBankBranch());
    assertEquals("12345-6", resolved.getAccountNumber());
    assertEquals("nova-chave", resolved.getPixKey());
  }
}
