package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.receipt.application.output.GetExportableReceiptMonthsOutput;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import jakarta.inject.Inject;

public class GetExportableReceiptMonthsInteractor {

  private final ReceiptRepository receiptRepository;

  @Inject
  public GetExportableReceiptMonthsInteractor(ReceiptRepository receiptRepository) {
    this.receiptRepository = receiptRepository;
  }

  public GetExportableReceiptMonthsOutput execute() {
    return new GetExportableReceiptMonthsOutput(receiptRepository.findAllReceiptMonths());
  }
}
