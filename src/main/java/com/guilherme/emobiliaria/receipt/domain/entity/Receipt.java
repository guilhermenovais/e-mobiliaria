package com.guilherme.emobiliaria.receipt.domain.entity;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Receipt {
  private Long id;
  private LocalDate date;
  private LocalDate paymentDueDate;
  private LocalDate intervalStart;
  private LocalDate intervalEnd;
  private int discount;
  private int fine;
  private String observation;
  private Contract contract;
  private List<PaymentProof> proofs = new ArrayList<>();

  private Receipt() {
  }

  public static Receipt create(LocalDate date, LocalDate paymentDueDate, LocalDate intervalStart,
      LocalDate intervalEnd, int discount, int fine, String observation, Contract contract) {
    Receipt receipt = new Receipt();
    receipt.setDate(date);
    receipt.setPaymentDueDate(paymentDueDate);
    receipt.setIntervalStart(intervalStart);
    receipt.setIntervalEnd(intervalEnd);
    receipt.setDiscount(discount);
    receipt.setFine(fine);
    receipt.setObservation(observation);
    receipt.setContract(contract);
    return receipt;
  }

  public static Receipt restore(Long id, LocalDate date, LocalDate paymentDueDate,
      LocalDate intervalStart, LocalDate intervalEnd, int discount, int fine, String observation,
      Contract contract) {
    Receipt receipt =
        create(date, paymentDueDate, intervalStart, intervalEnd, discount, fine, observation,
            contract);
    receipt.setId(id);
    return receipt;
  }

  public static Receipt restoreWithProofs(Long id, LocalDate date, LocalDate paymentDueDate,
      LocalDate intervalStart, LocalDate intervalEnd, int discount, int fine, String observation,
      Contract contract, List<PaymentProof> proofs) {
    Receipt receipt =
        restore(id, date, paymentDueDate, intervalStart, intervalEnd, discount, fine, observation,
            contract);
    receipt.setProofs(proofs);
    return receipt;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public LocalDate getDate() {
    return date;
  }

  public void setDate(LocalDate date) {
    if (date == null) {
      throw new BusinessException(ErrorMessage.Receipt.DATE_NULL);
    }
    this.date = date;
  }

  public LocalDate getPaymentDueDate() {
    return paymentDueDate;
  }

  public void setPaymentDueDate(LocalDate paymentDueDate) {
    if (paymentDueDate == null) {
      throw new BusinessException(ErrorMessage.Receipt.PAYMENT_DUE_DATE_NULL);
    }
    this.paymentDueDate = paymentDueDate;
  }

  public LocalDate getIntervalStart() {
    return intervalStart;
  }

  public void setIntervalStart(LocalDate intervalStart) {
    if (intervalStart == null) {
      throw new BusinessException(ErrorMessage.Receipt.INTERVAL_START_NULL);
    }
    if (intervalEnd != null && intervalStart.isAfter(intervalEnd)) {
      throw new BusinessException(ErrorMessage.Receipt.INTERVAL_START_AFTER_INTERVAL_END);
    }
    this.intervalStart = intervalStart;
  }

  public LocalDate getIntervalEnd() {
    return intervalEnd;
  }

  public void setIntervalEnd(LocalDate intervalEnd) {
    if (intervalEnd == null) {
      throw new BusinessException(ErrorMessage.Receipt.INTERVAL_END_NULL);
    }
    if (intervalStart != null && intervalEnd.isBefore(intervalStart)) {
      throw new BusinessException(ErrorMessage.Receipt.INTERVAL_END_BEFORE_INTERVAL_START);
    }
    this.intervalEnd = intervalEnd;
  }

  public int getDiscount() {
    return discount;
  }

  public void setDiscount(int discount) {
    if (discount < 0) {
      throw new BusinessException(ErrorMessage.Receipt.DISCOUNT_NEGATIVE);
    }
    this.discount = discount;
  }

  public int getFine() {
    return fine;
  }

  public void setFine(int fine) {
    if (fine < 0) {
      throw new BusinessException(ErrorMessage.Receipt.FINE_NEGATIVE);
    }
    this.fine = fine;
  }

  public String getObservation() {
    return observation;
  }

  public void setObservation(String observation) {
    this.observation = observation;
  }

  public Contract getContract() {
    return contract;
  }

  public void setContract(Contract contract) {
    if (contract == null) {
      throw new BusinessException(ErrorMessage.Receipt.CONTRACT_NULL);
    }
    this.contract = contract;
  }

  public List<PaymentProof> getProofs() {
    return Collections.unmodifiableList(proofs);
  }

  public void setProofs(List<PaymentProof> proofs) {
    this.proofs = proofs != null ? new ArrayList<>(proofs) : new ArrayList<>();
  }

  public boolean hasProofs() {
    return !proofs.isEmpty();
  }
}
