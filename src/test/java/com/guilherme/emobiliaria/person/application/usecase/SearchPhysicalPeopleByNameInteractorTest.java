package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.SearchPhysicalPeopleByNameInput;
import com.guilherme.emobiliaria.person.application.output.SearchPhysicalPeopleByNameOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SearchPhysicalPeopleByNameInteractorTest {

  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeAddressRepository addressRepository;
  private SearchPhysicalPeopleByNameInteractor interactor;
  private int cpfSeed = 0;

  @BeforeEach
  void setUp() {
    physicalPersonRepository = new FakePhysicalPersonRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new SearchPhysicalPeopleByNameInteractor(physicalPersonRepository);
  }

  private void createPhysicalPerson(String name) {
    String[] validCpfs = {
        "52998224725",
        "11144477735",
        "16899535009",
        "45317828791",
        "39053344705",
        "98765432100",
        "70548445052"
    };
    String cpf = validCpfs[cpfSeed++];
    Long addressId = new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
    new CreatePhysicalPersonInteractor(physicalPersonRepository, addressRepository)
        .execute(new CreatePhysicalPersonInput(name, "Brasileiro", CivilState.SINGLE,
            "Engenheiro", cpf, "MG-1234567", addressId));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return matching results when name matches")
    void shouldReturnMatchingResultsWhenNameMatches() {
      createPhysicalPerson("João Silva");
      createPhysicalPerson("Maria Souza");

      SearchPhysicalPeopleByNameOutput output = interactor.execute(
          new SearchPhysicalPeopleByNameInput("João", new PaginationInput(null, null)));

      assertEquals(1, output.result().total());
      assertEquals(1, output.result().items().size());
      assertEquals("João Silva", output.result().items().getFirst().getName());
    }

    @Test
    @DisplayName("Should return empty when no name matches")
    void shouldReturnEmptyWhenNoNameMatches() {
      createPhysicalPerson("João Silva");
      createPhysicalPerson("Maria Souza");

      SearchPhysicalPeopleByNameOutput output = interactor.execute(
          new SearchPhysicalPeopleByNameInput("Carlos", new PaginationInput(null, null)));

      assertTrue(output.result().items().isEmpty());
      assertEquals(0, output.result().total());
    }

    @Test
    @DisplayName("Should respect pagination when multiple matches exist")
    void shouldRespectPaginationWhenMultipleMatchesExist() {
      for (int i = 1; i <= 5; i++) {
        createPhysicalPerson("Silva " + i);
      }

      SearchPhysicalPeopleByNameOutput output = interactor.execute(
          new SearchPhysicalPeopleByNameInput("Silva", new PaginationInput(2, 0)));

      assertEquals(2, output.result().items().size());
      assertEquals(5, output.result().total());
    }
  }
}
