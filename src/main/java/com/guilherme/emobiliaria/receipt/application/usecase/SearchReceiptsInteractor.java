package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.receipt.application.input.SearchReceiptsInput;
import com.guilherme.emobiliaria.receipt.application.output.SearchReceiptsOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.shared.persistence.PagedResult;
import jakarta.inject.Inject;

public class SearchReceiptsInteractor {

  private final ReceiptRepository receiptRepository;

  @Inject
  public SearchReceiptsInteractor(ReceiptRepository receiptRepository) {
    this.receiptRepository = receiptRepository;
  }

  public SearchReceiptsOutput execute(SearchReceiptsInput input) {
    PagedResult<Receipt> result = receiptRepository.search(input.query(), input.contractId(), input.pagination());
    return new SearchReceiptsOutput(result);
  }
}
