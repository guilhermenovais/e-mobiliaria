package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.application.input.AttachPaymentProofFromBytesInput;
import com.guilherme.emobiliaria.receipt.application.input.AttachPaymentProofFromFileInput;
import com.guilherme.emobiliaria.receipt.application.output.AttachPaymentProofOutput;
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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AttachPaymentProofInteractorTest {

  private FakeReceiptRepository receiptRepository;
  private FakePaymentProofRepository proofRepository;
  private FakePaymentProofStorageService storageService;
  private AttachPaymentProofInteractor interactor;
  private Long receiptId;

  @BeforeEach
  void setUp() {
    receiptRepository = new FakeReceiptRepository();
    proofRepository = new FakePaymentProofRepository();
    storageService = new FakePaymentProofStorageService();
    interactor =
        new AttachPaymentProofInteractor(receiptRepository, proofRepository, storageService);

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
  class ExecuteFromFile {

    @Test
    @DisplayName("When receipt exists and file is valid PDF, should attach proof and return output")
    void shouldAttachPdfProofFromFile() throws IOException {
      Path tempFile = Files.createTempFile("test-proof", ".pdf");
      Files.write(tempFile, new byte[] {1, 2, 3});

      AttachPaymentProofOutput output = interactor.execute(
          new AttachPaymentProofFromFileInput(receiptId, tempFile, "prova.pdf", "prova.pdf"));

      assertNotNull(output.proof().getId());
      assertEquals("prova.pdf", output.proof().getOriginalFileName());
      assertEquals(ProofFileType.PDF, output.proof().getFileType());
      assertEquals(receiptId, output.proof().getReceiptId());

      Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("When file has unsupported extension, should throw BusinessException")
    void shouldThrowWhenExtensionUnsupported() throws IOException {
      Path tempFile = Files.createTempFile("test-proof", ".docx");

      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(
          new AttachPaymentProofFromFileInput(receiptId, tempFile, "prova.docx", "prova.docx")));
      assertEquals(ErrorMessage.PaymentProof.UNSUPPORTED_FILE_TYPE, ex.getErrorMessage());

      Files.deleteIfExists(tempFile);
    }

    @Test
    @DisplayName("When receipt does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenReceiptNotFound() throws IOException {
      Path tempFile = Files.createTempFile("test-proof", ".pdf");

      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(
          new AttachPaymentProofFromFileInput(999L, tempFile, "prova.pdf", "prova.pdf")));
      assertEquals(ErrorMessage.Receipt.NOT_FOUND, ex.getErrorMessage());

      Files.deleteIfExists(tempFile);
    }
  }


  @Nested
  class ExecuteFromBytes {

    @Test
    @DisplayName(
        "When receipt exists and image bytes are valid, should attach proof and return output")
    void shouldAttachImageProofFromBytes() {
      byte[] imageBytes = new byte[] {1, 2, 3};

      AttachPaymentProofOutput output = interactor.execute(
          new AttachPaymentProofFromBytesInput(receiptId, imageBytes, "clipboard.png",
              "clipboard.png"));

      assertNotNull(output.proof().getId());
      assertEquals("clipboard.png", output.proof().getOriginalFileName());
      assertEquals(ProofFileType.IMAGE, output.proof().getFileType());
      assertEquals(receiptId, output.proof().getReceiptId());
    }

    @Test
    @DisplayName("When bytes file has unsupported extension, should throw BusinessException")
    void shouldThrowWhenExtensionUnsupportedForBytes() {
      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(
          new AttachPaymentProofFromBytesInput(receiptId, new byte[] {1}, "prova.bmp2",
              "prova.bmp2")));
      assertEquals(ErrorMessage.PaymentProof.UNSUPPORTED_FILE_TYPE, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When receipt does not exist, should throw BusinessException with NOT_FOUND")
    void shouldThrowWhenReceiptNotFoundForBytes() {
      BusinessException ex = assertThrows(BusinessException.class, () -> interactor.execute(
          new AttachPaymentProofFromBytesInput(999L, new byte[] {1}, "clipboard.png",
              "clipboard.png")));
      assertEquals(ErrorMessage.Receipt.NOT_FOUND, ex.getErrorMessage());
    }
  }
}
