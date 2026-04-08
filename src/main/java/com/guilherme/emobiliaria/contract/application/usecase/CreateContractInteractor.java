package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.CreateContractInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.output.CreateContractOutput;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.contract.domain.repository.PaymentAccountRepository;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.repository.JuridicalPersonRepository;
import com.guilherme.emobiliaria.person.domain.repository.PhysicalPersonRepository;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.property.domain.repository.PropertyRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import com.google.inject.Inject;

import java.util.List;

public class CreateContractInteractor {

  private final ContractRepository contractRepository;
  private final PaymentAccountRepository paymentAccountRepository;
  private final PropertyRepository propertyRepository;
  private final PhysicalPersonRepository physicalPersonRepository;
  private final JuridicalPersonRepository juridicalPersonRepository;

  @Inject
  public CreateContractInteractor(
      ContractRepository contractRepository,
      PaymentAccountRepository paymentAccountRepository,
      PropertyRepository propertyRepository,
      PhysicalPersonRepository physicalPersonRepository,
      JuridicalPersonRepository juridicalPersonRepository
  ) {
    this.contractRepository = contractRepository;
    this.paymentAccountRepository = paymentAccountRepository;
    this.propertyRepository = propertyRepository;
    this.physicalPersonRepository = physicalPersonRepository;
    this.juridicalPersonRepository = juridicalPersonRepository;
  }

  public CreateContractOutput execute(CreateContractInput input) {
    PaymentAccount paymentAccount = paymentAccountRepository.findById(input.paymentAccountId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PaymentAccount.NOT_FOUND));
    Property property = propertyRepository.findById(input.propertyId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Property.NOT_FOUND));
    Person landlord = resolvePerson(input.landlord());
    List<Person> tenants = resolvePeople(input.tenants());
    List<Person> guarantors = resolvePeople(input.guarantors());
    List<Person> witnesses = resolvePeople(input.witnesses());
    Contract contract = Contract.create(
        input.startDate(),
        input.duration(),
        input.paymentDay(),
        input.rent(),
        paymentAccount,
        property,
        landlord,
        tenants,
        guarantors,
        witnesses
    );
    Contract created = contractRepository.create(contract);
    return new CreateContractOutput(created);
  }

  private Person resolvePerson(PersonReference ref) {
    return switch (ref.type()) {
      case PHYSICAL -> physicalPersonRepository.findById(ref.id())
          .orElseThrow(() -> new BusinessException(ErrorMessage.PhysicalPerson.NOT_FOUND));
      case JURIDICAL -> juridicalPersonRepository.findById(ref.id())
          .orElseThrow(() -> new BusinessException(ErrorMessage.JuridicalPerson.NOT_FOUND));
    };
  }

  private List<Person> resolvePeople(List<PersonReference> references) {
    if (references == null) {
      return List.of();
    }
    return references.stream()
        .map(this::resolvePerson)
        .toList();
  }
}
