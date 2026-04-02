package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.usecase.FindAllJuridicalPeopleInteractor;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.JuridicalPerson;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.shared.ui.NavigationService;
import javafx.application.Platform;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JuridicalPersonListControllerTest {

  @BeforeAll
  static void setup() throws InterruptedException {
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
    return future.get(10, TimeUnit.SECONDS);
  }

  private static void runOnFX(Runnable action) throws Exception {
    onFX(() -> {
      action.run();
      return null;
    });
  }

  private static Address sampleAddress(long id) {
    return Address.restore(id, "01001000", "Rua A", "10", null, "Centro", "São Paulo",
        BrazilianState.SP);
  }

  private static PhysicalPerson sampleRepresentative(long id, Address address) {
    return PhysicalPerson.restore(id, "Representante " + id, "Brasileiro", CivilState.SINGLE,
        "Administrador", "52998224725", "MG-1234567", address);
  }

  private static JuridicalPerson sampleJuridicalPerson(String name, long id) {
    Address address = sampleAddress(100 + id);
    PhysicalPerson representative = sampleRepresentative(200 + id, sampleAddress(300 + id));
    return JuridicalPerson.restore(id, name, "11222333000181", representative, address);
  }

  private JuridicalPersonListController createController(FakeJuridicalPersonRepository repo) {
    FindAllJuridicalPeopleInteractor findAll = new FindAllJuridicalPeopleInteractor(repo);
    NavigationService navigationService = new NavigationService();
    return new JuridicalPersonListController(findAll, navigationService, null);
  }

  @Nested
  @DisplayName("LoadPage")
  class LoadPage {

    @Test
    @DisplayName("Should populate table when page loaded with 3 juridical people")
    void shouldPopulateTableWhenPageLoaded() throws Exception {
      FakeJuridicalPersonRepository repo = new FakeJuridicalPersonRepository();
      repo.create(sampleJuridicalPerson("Empresa A", 1));
      repo.create(sampleJuridicalPerson("Empresa B", 2));
      repo.create(sampleJuridicalPerson("Empresa C", 3));

      JuridicalPersonListController controller = createController(repo);
      runOnFX(controller::initialize);
      Thread.sleep(500);

      List<JuridicalPerson> items = onFX(() -> controller.getTableView().getItems());
      assertEquals(3, items.size());
    }

    @Test
    @DisplayName("Should show empty table when no records exist")
    void shouldShowEmptyTableWhenNoRecordsExist() throws Exception {
      FakeJuridicalPersonRepository repo = new FakeJuridicalPersonRepository();
      JuridicalPersonListController controller = createController(repo);

      runOnFX(controller::initialize);
      Thread.sleep(500);

      List<JuridicalPerson> items = onFX(() -> controller.getTableView().getItems());
      assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Should disable prev and next buttons on first page when total <= page size")
    void shouldDisablePaginationButtonsWhenSinglePage() throws Exception {
      FakeJuridicalPersonRepository repo = new FakeJuridicalPersonRepository();
      repo.create(sampleJuridicalPerson("Empresa Única", 1));
      JuridicalPersonListController controller = createController(repo);

      runOnFX(controller::initialize);
      Thread.sleep(500);

      boolean prevDisabled = onFX(() -> controller.getPrevButton().isDisable());
      boolean nextDisabled = onFX(() -> controller.getNextButton().isDisable());
      assertTrue(prevDisabled);
      assertTrue(nextDisabled);
    }
  }
}
