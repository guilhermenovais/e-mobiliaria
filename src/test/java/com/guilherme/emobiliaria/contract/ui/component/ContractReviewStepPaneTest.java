package com.guilherme.emobiliaria.contract.ui.component;

import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import javafx.application.Platform;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;

class ContractReviewStepPaneTest {

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
        ContractReviewStepPane.class.getModule());
  }

  private static Property sampleProperty() {
    Address address =
        Address.restore(1L, "30130010", "Rua A", "10", null, "Centro", "Belo Horizonte",
            BrazilianState.MG);
    return Property.restore(1L, "Apartamento 101", "Apartamento", "111", "222", "333", address);
  }

  private static Person samplePerson(String name, String cpf) {
    Address address =
        Address.restore(2L, "30130010", "Rua B", "20", null, "Centro", "Belo Horizonte",
            BrazilianState.MG);
    return PhysicalPerson.restore(1L, name, "Brasileiro", CivilState.SINGLE, "Engenheiro", cpf,
        "MG123456", address);
  }

  private static List<String> accountSectionTexts(ContractReviewStepPane pane) throws Exception {
    var accountSectionField = ContractReviewStepPane.class.getDeclaredField("accountSection");
    accountSectionField.setAccessible(true);
    VBox accountSection = (VBox) accountSectionField.get(pane);

    return accountSection.getChildren().stream().filter(Label.class::isInstance)
        .map(Label.class::cast).map(Label::getText).toList();
  }

  @Test
  @DisplayName("Should render payment account details in review when account is provided")
  void shouldRenderPaymentAccountDetailsInReviewWhenAccountIsProvided() throws Exception {
    List<String> values = onFX(() -> {
      ContractReviewStepPane pane = new ContractReviewStepPane(messages());
      pane.populate(sampleProperty(), samplePerson("Locador", "52998224725"),
          List.of(samplePerson("Locatário", "11144477735")), List.of(), List.of(),
          LocalDate.of(2026, 1, 1), 12, 150000, 5, "Residencial",
          PaymentAccount.restore(10L, "Banco XPTO", "1234", "998877", "pix-x"));

      return accountSectionTexts(pane);
    });

    assertTrue(values.contains("Banco XPTO - Agência 1234 - Conta 998877"));
    assertTrue(values.contains("PIX: pix-x"));
  }

  @Test
  @DisplayName("Should render empty-account message in review when account is null")
  void shouldRenderEmptyAccountMessageInReviewWhenAccountIsNull() throws Exception {
    String emptyMessage = messages().getString("contract.wizard.step8.section.account.empty");

    List<String> values = onFX(() -> {
      ContractReviewStepPane pane = new ContractReviewStepPane(messages());
      pane.populate(sampleProperty(), samplePerson("Locador", "52998224725"),
          List.of(samplePerson("Locatário", "11144477735")), List.of(), List.of(),
          LocalDate.of(2026, 1, 1), 12, 150000, 5, "Residencial", null);

      return accountSectionTexts(pane);
    });

    assertTrue(values.contains(emptyMessage));
  }
}
