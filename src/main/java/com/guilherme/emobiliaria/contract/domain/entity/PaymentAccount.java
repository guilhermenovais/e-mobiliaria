package com.guilherme.emobiliaria.contract.domain.entity;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class PaymentAccount {
  private Long id;
  private String bank;
  private String bankBranch;
  private String accountNumber;
  private String pixKey;

  private PaymentAccount() {
  }

  public static PaymentAccount create(String bank, String bankBranch, String accountNumber,
      String pixKey) {
    PaymentAccount account = new PaymentAccount();
    account.setBank(bank);
    account.setBankBranch(bankBranch);
    account.setAccountNumber(accountNumber);
    account.pixKey = pixKey;
    return account;
  }

  public static PaymentAccount restore(Long id, String bank, String bankBranch,
      String accountNumber, String pixKey) {
    PaymentAccount account = create(bank, bankBranch, accountNumber, pixKey);
    account.setId(id);
    return account;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getBank() {
    return bank;
  }

  public void setBank(String bank) {
    if (bank == null || bank.isBlank()) {
      throw new BusinessException(ErrorMessage.PaymentAccount.BANK_EMPTY);
    }
    this.bank = bank;
  }

  public String getBankBranch() {
    return bankBranch;
  }

  public void setBankBranch(String bankBranch) {
    if (bankBranch == null || bankBranch.isBlank()) {
      throw new BusinessException(ErrorMessage.PaymentAccount.BANK_BRANCH_EMPTY);
    }
    this.bankBranch = bankBranch;
  }

  public String getAccountNumber() {
    return accountNumber;
  }

  public void setAccountNumber(String accountNumber) {
    if (accountNumber == null || accountNumber.isBlank()) {
      throw new BusinessException(ErrorMessage.PaymentAccount.ACCOUNT_NUMBER_EMPTY);
    }
    this.accountNumber = accountNumber;
  }

  public String getPixKey() {
    return pixKey;
  }

  public void setPixKey(String pixKey) {
    this.pixKey = pixKey;
  }
}
