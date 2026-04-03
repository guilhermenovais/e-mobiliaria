package com.guilherme.emobiliaria.contract.application.usecase;

import com.guilherme.emobiliaria.contract.application.input.EditContractInput;
import com.guilherme.emobiliaria.contract.application.input.PersonReference;
import com.guilherme.emobiliaria.contract.application.output.EditContractOutput;
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

public class EditContractInteractor {

  private final ContractRepository contractRepository;
  private final PaymentAccountRepository paymentAccountRepository;
  private final PropertyRepository propertyRepository;
  private final PhysicalPersonRepository physicalPersonRepository;
  private final JuridicalPersonRepository juridicalPersonRepository;

  @Inject
  public EditContractInteractor(
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

  public EditContractOutput execute(EditContractInput input) {
    Contract contract = contractRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));
    PaymentAccount paymentAccount = paymentAccountRepository.findById(input.paymentAccountId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.PaymentAccount.NOT_FOUND));
    Property property = propertyRepository.findById(input.propertyId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Property.NOT_FOUND));
    Person landlord = resolvePerson(input.landlord());
    List<Person> tenants = input.tenants().stream()
        .map(this::resolvePerson)
        .toList();
    contract.setStartDate(input.startDate());
    contract.setDuration(input.duration());
    contract.setPaymentDay(input.paymentDay());
    contract.setRent(input.rent());
    contract.setPaymentAccount(paymentAccount);
    contract.setProperty(property);
    contract.setLandlord(landlord);
    contract.setTenants(tenants);
    Contract updated = contractRepository.update(contract);
    return new EditContractOutput(updated);
  }

  private Person resolvePerson(PersonReference ref) {
    return switch (ref.type()) {
      case PHYSICAL -> physicalPersonRepository.findById(ref.id())
          .orElseThrow(() -> new BusinessException(ErrorMessage.PhysicalPerson.NOT_FOUND));
      case JURIDICAL -> juridicalPersonRepository.findById(ref.id())
          .orElseThrow(() -> new BusinessException(ErrorMessage.JuridicalPerson.NOT_FOUND));
    };
  }
}
