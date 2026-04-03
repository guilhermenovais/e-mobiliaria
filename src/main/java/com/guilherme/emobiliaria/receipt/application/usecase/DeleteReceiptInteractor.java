package com.guilherme.emobiliaria.receipt.application.usecase;

import com.google.inject.Inject;
import com.guilherme.emobiliaria.receipt.application.input.DeleteReceiptInput;
import com.guilherme.emobiliaria.receipt.application.output.DeleteReceiptOutput;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

public class DeleteReceiptInteractor {

  private final ReceiptRepository receiptRepository;

  @Inject
  public DeleteReceiptInteractor(ReceiptRepository receiptRepository) {
    this.receiptRepository = receiptRepository;
  }

  public DeleteReceiptOutput execute(DeleteReceiptInput input) {
    receiptRepository.findById(input.id())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Receipt.NOT_FOUND));
    receiptRepository.delete(input.id());
    return new DeleteReceiptOutput();
  }
}
