package com.guilherme.emobiliaria.config.domain.entity;

import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ConfigTest {

  private Address sampleAddress() {
    return Address.restore(1L, "01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private PhysicalPerson samplePerson() {
    return PhysicalPerson.restore(1L, "João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
        "52998224725", "MG-1234567", sampleAddress());
  }

  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with a person, should hold the given id and defaultLandlord")
    void shouldRestoreWithPersonAndId() {
      PhysicalPerson person = samplePerson();

      Config config = Config.restore(1L, person);

      assertEquals(1L, config.getId());
      assertEquals(person, config.getDefaultLandlord());
    }

    @Test
    @DisplayName("When restored with null defaultLandlord, should hold null defaultLandlord")
    void shouldRestoreWithNullDefaultLandlord() {
      Config config = Config.restore(1L, null);

      assertEquals(1L, config.getId());
      assertNull(config.getDefaultLandlord());
    }
  }

  @Nested
  class SetDefaultLandlord {

    @Test
    @DisplayName("When set to a person, should update defaultLandlord")
    void shouldUpdateDefaultLandlord() {
      Config config = Config.restore(1L, null);
      PhysicalPerson person = samplePerson();

      config.setDefaultLandlord(person);

      assertEquals(person, config.getDefaultLandlord());
    }

    @Test
    @DisplayName("When set to null, should clear defaultLandlord")
    void shouldClearDefaultLandlordWhenSetToNull() {
      PhysicalPerson person = samplePerson();
      Config config = Config.restore(1L, person);

      config.setDefaultLandlord(null);

      assertNull(config.getDefaultLandlord());
    }
  }
}
