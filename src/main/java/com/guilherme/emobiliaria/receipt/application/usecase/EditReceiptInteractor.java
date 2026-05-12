package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.receipt.application.input.EditReceiptInput;
import com.guilherme.emobiliaria.receipt.application.output.EditReceiptOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class EditReceiptInteractor {

  private final ReceiptRepository receiptRepository;
  private final ContractRepository contractRepository;

  @Inject
  public EditReceiptInteractor(ReceiptRepository receiptRepository,
      ContractRepository contractRepository) {
    this.receiptRepository = receiptRepository;
    this.contractRepository = contractRepository;
  }

  public EditReceiptOutput execute(EditReceiptInput input) {
    Receipt receipt = receiptRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    Contract contract = contractRepository.findById(input.contractId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));
    if (receiptRepository.existsByContractAndPaymentDueDate(input.contractId(),
        input.paymentDueDate(), input.id())) {
      throw new BusinessException(ErrorMessage.Receipt.DUPLICATE_PAYMENT_DUE_DATE);
    }
    Receipt updated = receiptRepository.update(
        Receipt.restore(receipt.getId(), input.date(), input.paymentDueDate(),
            input.intervalStart(), input.intervalEnd(),
            input.discount(), input.fine(), input.observation(), contract));
    return new EditReceiptOutput(updated);
  }
}
