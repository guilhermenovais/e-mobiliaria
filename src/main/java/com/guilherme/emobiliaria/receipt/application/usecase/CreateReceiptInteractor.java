package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.receipt.application.input.CreateReceiptInput;
import com.guilherme.emobiliaria.receipt.application.output.CreateReceiptOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class CreateReceiptInteractor {

  private final ReceiptRepository receiptRepository;
  private final ContractRepository contractRepository;

  public CreateReceiptInteractor(ReceiptRepository receiptRepository,
      ContractRepository contractRepository) {
    this.receiptRepository = receiptRepository;
    this.contractRepository = contractRepository;
  }

  public CreateReceiptOutput execute(CreateReceiptInput input) {
    Contract contract = contractRepository.findById(input.contractId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));
    Receipt receipt = Receipt.create(input.date(), input.intervalStart(), input.intervalEnd(),
        input.discount(), input.fine(), contract);
    Receipt created = receiptRepository.create(receipt);
    return new CreateReceiptOutput(created);
  }
}
