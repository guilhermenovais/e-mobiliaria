package com.guilherme.emobiliaria.person.ui.controller;

import com.guilherme.emobiliaria.person.application.usecase.DeletePhysicalPersonInteractor;
import com.guilherme.emobiliaria.person.application.usecase.FindAllPhysicalPeopleInteractor;
import com.guilherme.emobiliaria.person.application.usecase.SearchPhysicalPeopleByNameInteractor;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
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

class PhysicalPersonListControllerTest {

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

  // ── Helpers ────────────────────────────────────────────────────────────────

  private static Address sampleAddress() {
    return Address.restore(1L, "01001000", "Rua A", "10", null, "Centro", "São Paulo",
        BrazilianState.SP);
  }

  private static PhysicalPerson samplePerson(String name) {
    return PhysicalPerson.create(name, "Brasileiro", CivilState.SINGLE,
        "Engenheiro", "52998224725", "MG-1234567", sampleAddress());
  }

  private PhysicalPersonListController createController(FakePhysicalPersonRepository repo) {
    FindAllPhysicalPeopleInteractor findAll = new FindAllPhysicalPeopleInteractor(repo);
    SearchPhysicalPeopleByNameInteractor searchByName = new SearchPhysicalPeopleByNameInteractor(
        repo);
    DeletePhysicalPersonInteractor deleteInteractor = new DeletePhysicalPersonInteractor(repo);
    NavigationService navigationService = new NavigationService();
    return new PhysicalPersonListController(findAll, searchByName, deleteInteractor,
        navigationService, null, null);
  }

  // ── Tests ──────────────────────────────────────────────────────────────────

  @Nested
  @DisplayName("LoadPage")
  class LoadPage {

    @Test
    @DisplayName("Should populate table when page loaded with 3 people")
    void shouldPopulateTableWhenPageLoaded() throws Exception {
      FakePhysicalPersonRepository repo = new FakePhysicalPersonRepository();
      repo.create(samplePerson("Alice"));
      repo.create(samplePerson("Bob"));
      repo.create(samplePerson("Carlos"));

      PhysicalPersonListController controller = createController(repo);

      runOnFX(controller::initialize);

      // wait for background task to finish
      Thread.sleep(500);

      List<PhysicalPerson> items = onFX(() -> controller.getTableView().getItems());
      assertEquals(3, items.size());
    }

    @Test
    @DisplayName("Should show empty table when no records exist")
    void shouldShowEmptyTableWhenNoRecordsExist() throws Exception {
      FakePhysicalPersonRepository repo = new FakePhysicalPersonRepository();
      PhysicalPersonListController controller = createController(repo);

      runOnFX(controller::initialize);
      Thread.sleep(500);

      List<PhysicalPerson> items = onFX(() -> controller.getTableView().getItems());
      assertTrue(items.isEmpty());
    }

    @Test
    @DisplayName("Should disable prev button on first page")
    void shouldDisablePrevButtonOnFirstPage() throws Exception {
      FakePhysicalPersonRepository repo = new FakePhysicalPersonRepository();
      PhysicalPersonListController controller = createController(repo);

      runOnFX(controller::initialize);
      Thread.sleep(500);

      boolean prevDisabled = onFX(() -> controller.getPrevButton().isDisable());
      assertTrue(prevDisabled);
    }

    @Test
    @DisplayName("Should disable next button when only one page exists")
    void shouldDisableNextButtonWhenOnlyOnePage() throws Exception {
      FakePhysicalPersonRepository repo = new FakePhysicalPersonRepository();
      repo.create(samplePerson("Alice"));
      repo.create(samplePerson("Bob"));
      repo.create(samplePerson("Carlos"));

      PhysicalPersonListController controller = createController(repo);

      runOnFX(controller::initialize);
      Thread.sleep(500);

      boolean nextDisabled = onFX(() -> controller.getNextButton().isDisable());
      assertTrue(nextDisabled);
    }
  }

  @Nested
  @DisplayName("HandleDelete")
  class HandleDelete {

    @Test
    @DisplayName("Should not delete when confirmation is cancelled")
    void shouldNotDeleteWhenConfirmationCancelled() throws Exception {
      FakePhysicalPersonRepository repo = new FakePhysicalPersonRepository();
      PhysicalPerson person = repo.create(samplePerson("Alice"));

      PhysicalPersonListController controller = createController(repo);
      runOnFX(controller::initialize);
      Thread.sleep(500);

      // Call the package-private overload with confirmed = false
      runOnFX(() -> controller.handleDelete(person, false));
      Thread.sleep(200);

      long count = repo.findAll(new PaginationInput(100, 0)).total();
      assertEquals(1, count, "Person should NOT have been deleted");
    }

    @Test
    @DisplayName("Should delete person when confirmation is accepted")
    void shouldDeletePersonWhenConfirmed() throws Exception {
      FakePhysicalPersonRepository repo = new FakePhysicalPersonRepository();
      PhysicalPerson person = repo.create(samplePerson("Alice"));

      PhysicalPersonListController controller = createController(repo);
      runOnFX(controller::initialize);
      Thread.sleep(500);

      // Call the package-private overload with confirmed = true
      runOnFX(() -> controller.handleDelete(person, true));
      Thread.sleep(500);

      long count = repo.findAll(new PaginationInput(100, 0)).total();
      assertEquals(0, count, "Person should have been deleted");
    }
  }
}
