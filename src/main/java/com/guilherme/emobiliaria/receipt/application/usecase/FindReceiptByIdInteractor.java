package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.receipt.application.input.FindReceiptByIdInput;
import com.guilherme.emobiliaria.receipt.application.output.FindReceiptByIdOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class FindReceiptByIdInteractor {

  private final ReceiptRepository receiptRepository;

  public FindReceiptByIdInteractor(ReceiptRepository receiptRepository) {
    this.receiptRepository = receiptRepository;
  }

  public FindReceiptByIdOutput execute(FindReceiptByIdInput input) {
    Receipt receipt = receiptRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    return new FindReceiptByIdOutput(receipt);
  }
}
