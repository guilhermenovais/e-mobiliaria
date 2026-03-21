package com.guilherme.emobiliaria.person.application.usecase;

import com.guilherme.emobiliaria.person.application.input.CreateAddressInput;
import com.guilherme.emobiliaria.person.application.input.CreateJuridicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.CreatePhysicalPersonInput;
import com.guilherme.emobiliaria.person.application.input.FindAllJuridicalPeopleInput;
import com.guilherme.emobiliaria.person.application.output.FindAllJuridicalPeopleOutput;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.repository.FakeAddressRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakeJuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.FakePhysicalPersonRepository;
import com.guilherme.emobiliaria.shared.persistence.PaginationInput;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FindAllJuridicalPeopleInteractorTest {

  private FakeJuridicalPersonRepository juridicalPersonRepository;
  private FakePhysicalPersonRepository physicalPersonRepository;
  private FakeAddressRepository addressRepository;
  private FindAllJuridicalPeopleInteractor interactor;

  @BeforeEach
  void setUp() {
    juridicalPersonRepository = new FakeJuridicalPersonRepository();
    physicalPersonRepository = new FakePhysicalPersonRepository();
    addressRepository = new FakeAddressRepository();
    interactor = new FindAllJuridicalPeopleInteractor(juridicalPersonRepository);
  }

  private void createJuridicalPerson() {
    Long addressId = new CreateAddressInteractor(addressRepository)
        .execute(new CreateAddressInput("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
            BrazilianState.SP))
        .address().getId();
    Long representativeId = new CreatePhysicalPersonInteractor(physicalPersonRepository,
        addressRepository)
        .execute(new CreatePhysicalPersonInput("João Silva", "Brasileiro", CivilState.SINGLE,
            "Engenheiro", "529.982.247-25", "MG-1234567", addressId))
        .physicalPerson().getId();
    new CreateJuridicalPersonInteractor(juridicalPersonRepository, physicalPersonRepository,
        addressRepository)
        .execute(new CreateJuridicalPersonInput("Empresa Teste Ltda", "11222333000181",
            representativeId, addressId));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("Should return all juridical people with correct total")
    void shouldReturnAllJuridicalPeople() {
      createJuridicalPerson();

      FindAllJuridicalPeopleOutput output = interactor.execute(
          new FindAllJuridicalPeopleInput(new PaginationInput(null, null)));

      assertEquals(1, output.result().total());
      assertEquals(1, output.result().items().size());
    }
  }
}
