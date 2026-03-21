package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.receipt.application.input.FindAllReceiptsByContractIdInput;
import com.guilherme.emobiliaria.receipt.application.output.FindAllReceiptsByContractIdOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;

public class FindAllReceiptsByContractIdInteractor {

  private final ReceiptRepository receiptRepository;

  public FindAllReceiptsByContractIdInteractor(ReceiptRepository receiptRepository) {
    this.receiptRepository = receiptRepository;
  }

  public FindAllReceiptsByContractIdOutput execute(FindAllReceiptsByContractIdInput input) {
    PagedResult<Receipt> result = receiptRepository.findAllByContractId(input.contractId(),
        input.pagination());
    return new FindAllReceiptsByContractIdOutput(result);
  }
}
