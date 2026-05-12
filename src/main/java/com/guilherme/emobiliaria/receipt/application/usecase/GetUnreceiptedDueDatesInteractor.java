package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.ContractStatus;
import com.guilherme.emobiliaria.contract.domain.repository.ContractRepository;
import com.guilherme.emobiliaria.contract.domain.service.PaymentDueDateService;
import com.guilherme.emobiliaria.receipt.application.input.GetUnreceiptedDueDatesInput;
import com.guilherme.emobiliaria.receipt.application.output.GetUnreceiptedDueDatesOutput;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GetUnreceiptedDueDatesInteractor {

  private final ContractRepository contractRepository;
  private final ReceiptRepository receiptRepository;
  private final PaymentDueDateService paymentDueDateService;

  @Inject
  public GetUnreceiptedDueDatesInteractor(ContractRepository contractRepository,
      ReceiptRepository receiptRepository, PaymentDueDateService paymentDueDateService) {
    this.contractRepository = contractRepository;
    this.receiptRepository = receiptRepository;
    this.paymentDueDateService = paymentDueDateService;
  }

  public GetUnreceiptedDueDatesOutput execute(GetUnreceiptedDueDatesInput input) {
    Contract contract = contractRepository.findById(input.contractId())
        .orElseThrow(() -> new BusinessException(ErrorMessage.Contract.NOT_FOUND));

    ContractStatus status = contract.getStatus();
    if (status != ContractStatus.ACTIVE && status != ContractStatus.EXPIRING) {
      return new GetUnreceiptedDueDatesOutput(List.of());
    }

    List<LocalDate> computed =
        paymentDueDateService.computeDueDates(contract.getStartDate(), contract.getPaymentDay(),
            input.today());

    Set<LocalDate> receipted =
        new HashSet<>(receiptRepository.findAllPaymentDueDatesByContractId(input.contractId()));

    if (input.excludeReceiptId() != null) {
      receiptRepository.findById(input.excludeReceiptId())
          .ifPresent(r -> receipted.remove(r.getPaymentDueDate()));
    }

    List<LocalDate> result = new ArrayList<>();
    for (LocalDate d : computed) {
      if (!receipted.contains(d)) {
        result.add(d);
      }
    }
    return new GetUnreceiptedDueDatesOutput(result);
  }
}
