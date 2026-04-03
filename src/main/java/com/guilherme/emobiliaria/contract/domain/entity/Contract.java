package com.guilherme.emobiliaria.contract.domain.entity;

import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

public class Contract {
  private Long id;
  private LocalDate startDate;
  private Period duration;
  private int paymentDay;
  private int rent;
  private PaymentAccount paymentAccount;
  private Property property;
  private Person landlord;
  private List<Person> tenants;

  private Contract() {
  }

  public static Contract create(LocalDate startDate, Period duration, int paymentDay, int rent,
      PaymentAccount paymentAccount, Property property, Person landlord, List<Person> tenants) {
    Contract contract = new Contract();
    contract.setStartDate(startDate);
    contract.setDuration(duration);
    contract.setPaymentDay(paymentDay);
    contract.setRent(rent);
    contract.setPaymentAccount(paymentAccount);
    contract.setProperty(property);
    contract.setLandlord(landlord);
    contract.setTenants(tenants);
    return contract;
  }

  public static Contract restore(Long id, LocalDate startDate, Period duration, int paymentDay,
      int rent,
      PaymentAccount paymentAccount, Property property, Person landlord, List<Person> tenants) {
    Contract contract =
        create(startDate, duration, paymentDay, rent, paymentAccount, property, landlord, tenants);
    contract.setId(id);
    return contract;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDate getStartDate() {
    return startDate;
  }

  public void setStartDate(LocalDate startDate) {
    if (startDate == null) {
      throw new BusinessException(ErrorMessage.Contract.START_DATE_NULL);
    }
    this.startDate = startDate;
  }

  public Period getDuration() {
    return duration;
  }

  public void setDuration(Period duration) {
    if (duration == null) {
      throw new BusinessException(ErrorMessage.Contract.DURATION_NULL);
    }
    this.duration = duration;
  }

  public int getPaymentDay() {
    return paymentDay;
  }

  public void setPaymentDay(int paymentDay) {
    if (paymentDay < 1 || paymentDay > 31) {
      throw new BusinessException(ErrorMessage.Contract.PAYMENT_DAY_INVALID);
    }
    this.paymentDay = paymentDay;
  }

  public int getRent() {
    return rent;
  }

  public void setRent(int rent) {
    if (rent < 0) {
      throw new BusinessException(ErrorMessage.Contract.RENT_NEGATIVE);
    }
    this.rent = rent;
  }

  public PaymentAccount getPaymentAccount() {
    return paymentAccount;
  }

  public void setPaymentAccount(PaymentAccount paymentAccount) {
    if (paymentAccount == null) {
      throw new BusinessException(ErrorMessage.Contract.PAYMENT_ACCOUNT_NULL);
    }
    this.paymentAccount = paymentAccount;
  }

  public Property getProperty() {
    return property;
  }

  public void setProperty(Property property) {
    if (property == null) {
      throw new BusinessException(ErrorMessage.Contract.PROPERTY_NULL);
    }
    this.property = property;
  }

  public Person getLandlord() {
    return landlord;
  }

  public void setLandlord(Person landlord) {
    if (landlord == null) {
      throw new BusinessException(ErrorMessage.Contract.LANDLORD_NULL);
    }
    this.landlord = landlord;
  }

  public List<Person> getTenants() {
    return tenants;
  }

  public void setTenants(List<Person> tenants) {
    if (tenants == null || tenants.isEmpty()) {
      throw new BusinessException(ErrorMessage.Contract.TENANTS_EMPTY);
    }
    this.tenants = tenants;
  }
}
