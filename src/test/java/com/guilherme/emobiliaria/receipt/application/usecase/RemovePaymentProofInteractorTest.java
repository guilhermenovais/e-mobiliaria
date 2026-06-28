package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.application.input.RemovePaymentProofInput;
import com.guilherme.emobiliaria.receipt.domain.entity.PaymentProof;
import com.guilherme.emobiliaria.receipt.domain.entity.ProofFileType;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.repository.FakePaymentProofRepository;
import com.guilherme.emobiliaria.receipt.domain.repository.FakeReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.FakePaymentProofStorageService;
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

class RemovePaymentProofInteractorTest {

  private FakePaymentProofRepository proofRepository;
  private FakePaymentProofStorageService storageService;
  private RemovePaymentProofInteractor interactor;
  private Long receiptId;

  @BeforeEach
  void setUp() {
    FakeReceiptRepository receiptRepository = new FakeReceiptRepository();
    proofRepository = new FakePaymentProofRepository();
    storageService = new FakePaymentProofStorageService();
    interactor = new RemovePaymentProofInteractor(proofRepository, storageService);

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

  private PaymentProof createProof() {
    return proofRepository.create(
        PaymentProof.create("comprovante.pdf", "Comprovante", "stored-uuid.pdf", ProofFileType.PDF,
            LocalDate.of(2026, 6, 1), receiptId));
  }

  @Nested
  class Execute {

    @Test
    @DisplayName("When proof exists, should delete file from storage and remove from repository")
    void shouldDeleteProofAndFileWhenExists() {
      PaymentProof proof = createProof();

      interactor.execute(new RemovePaymentProofInput(receiptId, proof.getId()));

      assertTrue(proofRepository.findAllByReceiptId(receiptId).isEmpty());
      assertTrue(storageService.getDeletedFiles().contains("stored-uuid.pdf"));
    }

    @Test
    @DisplayName(
        "When proof does not exist for receipt, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenProofNotFound() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> interactor.execute(new RemovePaymentProofInput(receiptId, 999L)));
      assertEquals(ErrorMessage.PaymentProof.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
