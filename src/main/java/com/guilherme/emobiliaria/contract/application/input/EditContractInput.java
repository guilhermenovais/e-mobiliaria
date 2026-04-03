package com.guilherme.emobiliaria.contract.application.input;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public record EditContractInput(
    Long id,
    LocalDate startDate,
    Period duration,
    int paymentDay,
    int rent,
    Long paymentAccountId,
    Long propertyId,
    PersonReference landlord,
    List<PersonReference> tenants
) {}
