package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.application.input.FindPaymentProofsByReceiptIdInput;
import com.guilherme.emobiliaria.receipt.application.output.FindPaymentProofsByReceiptIdOutput;
import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.FakePaymentProofRepository;
import com.guilherme.emobiliaria.receipt.domain.repository.FakeReceiptRepository;
import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FindPaymentProofsByReceiptIdInteractorTest {

  private FakeReceiptRepository receiptRepository;
  private FakePaymentProofRepository proofRepository;
  private FindPaymentProofsByReceiptIdInteractor interactor;
  private Long receiptId;

  @BeforeEach
  void setUp() {
    receiptRepository = new FakeReceiptRepository();
    proofRepository = new FakePaymentProofRepository();
    interactor = new FindPaymentProofsByReceiptIdInteractor(receiptRepository, proofRepository);

    Address address =
        Address.create("01001000", "Rua A", "1", null, "Centro", "São Paulo", BrazilianState.SP);
    PhysicalPerson person =
        PhysicalPerson.create("João", "Brasileiro", CivilState.SINGLE, "Eng", "529.982.247-25",
            "MG-123", address);
    Property property =
        Property.create("Apto", "Apartamento", "1234567890", "0987654321", "IPTU-001", address);
    PaymentAccount paymentAccount = PaymentAccount.create("BB", "1234-5", "12345-6", null);
    Contract contract =
        Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, 150000, "Residencial",
            paymentAccount, property, person, List.of(person), List.of(), List.of());
    contract.setId(1L);
    Receipt receipt = Receipt.create(LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 15),
        LocalDate.of(2026, 6, 1), LocalDate.of(2026, 6, 30), 0, 0, null, contract);
    receiptId = receiptRepository.create(receipt).getId();
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When receipt exists with proofs, should return all proofs")
    void shouldReturnProofsWhenReceiptExists() {
      proofRepository.create(
          PaymentProof.create("a.pdf", "a.pdf", "stored-a.pdf", ProofFileType.PDF,
              LocalDate.of(2026, 6, 1), receiptId));
      proofRepository.create(
          PaymentProof.create("b.jpg", "b.jpg", "stored-b.jpg", ProofFileType.IMAGE,
              LocalDate.of(2026, 6, 2), receiptId));

      FindPaymentProofsByReceiptIdOutput output =
          interactor.execute(new FindPaymentProofsByReceiptIdInput(receiptId));

      assertEquals(2, output.proofs().size());
    }

    @Test
    @DisplayName("When receipt exists with no proofs, should return empty list")
    void shouldReturnEmptyListWhenNoProofs() {
      FindPaymentProofsByReceiptIdOutput output =
          interactor.execute(new FindPaymentProofsByReceiptIdInput(receiptId));

      assertTrue(output.proofs().isEmpty());
    }

    @Test
    @DisplayName("When receipt does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenReceiptNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new FindPaymentProofsByReceiptIdInput(999L)));
      assertEquals(ErrorMessage.Receipt.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
