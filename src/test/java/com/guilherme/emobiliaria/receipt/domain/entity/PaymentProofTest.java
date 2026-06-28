package com.guilherme.emobiliaria.receipt.domain.entity;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentProofTest {

  private static final String VALID_ORIGINAL_FILE_NAME = "comprovante.pdf";
  private static final String VALID_STORED_FILE_NAME = "uuid-stored.pdf";
  private static final ProofFileType VALID_FILE_TYPE = ProofFileType.PDF;
  private static final LocalDate VALID_ATTACHED_AT = LocalDate.of(2026, 6, 1);
  private static final Long VALID_RECEIPT_ID = 1L;

  private PaymentProof validProof() {
    return PaymentProof.create(VALID_ORIGINAL_FILE_NAME, VALID_STORED_FILE_NAME, VALID_FILE_TYPE,
        VALID_ATTACHED_AT, VALID_RECEIPT_ID);
  }

  @Nested
  class Create {

    @Test
    @DisplayName("When all fields are valid, should create PaymentProof with null id")
    void shouldCreateWithNullIdWhenAllFieldsAreValid() {
      PaymentProof proof = validProof();

      assertNull(proof.getId());
      assertEquals(VALID_ORIGINAL_FILE_NAME, proof.getOriginalFileName());
      assertEquals(VALID_STORED_FILE_NAME, proof.getStoredFileName());
      assertEquals(VALID_FILE_TYPE, proof.getFileType());
      assertEquals(VALID_ATTACHED_AT, proof.getAttachedAt());
      assertEquals(VALID_RECEIPT_ID, proof.getReceiptId());
    }

    @Test
    @DisplayName("When originalFileName is null, should throw BusinessException")
    void shouldThrowWhenOriginalFileNameIsNull() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> PaymentProof.create(null, VALID_STORED_FILE_NAME, VALID_FILE_TYPE,
              VALID_ATTACHED_AT, VALID_RECEIPT_ID));
      assertEquals(ErrorMessage.PaymentProof.ORIGINAL_FILENAME_BLANK, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When originalFileName is blank, should throw BusinessException")
    void shouldThrowWhenOriginalFileNameIsBlank() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> PaymentProof.create("  ", VALID_STORED_FILE_NAME, VALID_FILE_TYPE,
              VALID_ATTACHED_AT, VALID_RECEIPT_ID));
      assertEquals(ErrorMessage.PaymentProof.ORIGINAL_FILENAME_BLANK, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When storedFileName is null, should throw BusinessException")
    void shouldThrowWhenStoredFileNameIsNull() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> PaymentProof.create(VALID_ORIGINAL_FILE_NAME, null, VALID_FILE_TYPE,
              VALID_ATTACHED_AT, VALID_RECEIPT_ID));
      assertEquals(ErrorMessage.PaymentProof.STORED_FILENAME_BLANK, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When storedFileName is blank, should throw BusinessException")
    void shouldThrowWhenStoredFileNameIsBlank() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> PaymentProof.create(VALID_ORIGINAL_FILE_NAME, "", VALID_FILE_TYPE,
              VALID_ATTACHED_AT, VALID_RECEIPT_ID));
      assertEquals(ErrorMessage.PaymentProof.STORED_FILENAME_BLANK, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When fileType is null, should throw BusinessException")
    void shouldThrowWhenFileTypeIsNull() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> PaymentProof.create(VALID_ORIGINAL_FILE_NAME, VALID_STORED_FILE_NAME, null,
              VALID_ATTACHED_AT, VALID_RECEIPT_ID));
      assertEquals(ErrorMessage.PaymentProof.FILE_TYPE_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When attachedAt is null, should throw BusinessException")
    void shouldThrowWhenAttachedAtIsNull() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> PaymentProof.create(VALID_ORIGINAL_FILE_NAME, VALID_STORED_FILE_NAME,
              VALID_FILE_TYPE, null, VALID_RECEIPT_ID));
      assertEquals(ErrorMessage.PaymentProof.ATTACHED_AT_NULL, ex.getErrorMessage());
    }

    @Test
    @DisplayName("When receiptId is null, should throw BusinessException")
    void shouldThrowWhenReceiptIdIsNull() {
      BusinessException ex = assertThrows(BusinessException.class,
          () -> PaymentProof.create(VALID_ORIGINAL_FILE_NAME, VALID_STORED_FILE_NAME,
              VALID_FILE_TYPE, VALID_ATTACHED_AT, null));
      assertEquals(ErrorMessage.PaymentProof.RECEIPT_ID_NULL, ex.getErrorMessage());
    }
  }


  @Nested
  class Restore {

    @Test
    @DisplayName("When restored with id, should set id and all fields")
    void shouldRestoreWithId() {
      PaymentProof proof =
          PaymentProof.restore(42L, VALID_ORIGINAL_FILE_NAME, VALID_STORED_FILE_NAME,
              VALID_FILE_TYPE, VALID_ATTACHED_AT, VALID_RECEIPT_ID);

      assertEquals(42L, proof.getId());
      assertEquals(VALID_ORIGINAL_FILE_NAME, proof.getOriginalFileName());
      assertEquals(VALID_STORED_FILE_NAME, proof.getStoredFileName());
      assertEquals(VALID_FILE_TYPE, proof.getFileType());
      assertEquals(VALID_ATTACHED_AT, proof.getAttachedAt());
      assertEquals(VALID_RECEIPT_ID, proof.getReceiptId());
    }
  }


  @Nested
  class SetOriginalFileName {

    @Test
    @DisplayName("When originalFileName is valid, should set it")
    void shouldSetWhenValid() {
      PaymentProof proof = validProof();
      assertDoesNotThrow(() -> proof.setOriginalFileName("other.jpg"));
      assertEquals("other.jpg", proof.getOriginalFileName());
    }

    @Test
    @DisplayName("When originalFileName is null, should throw BusinessException")
    void shouldThrowWhenNull() {
      PaymentProof proof = validProof();
      BusinessException ex =
          assertThrows(BusinessException.class, () -> proof.setOriginalFileName(null));
      assertEquals(ErrorMessage.PaymentProof.ORIGINAL_FILENAME_BLANK, ex.getErrorMessage());
    }
  }


  @Nested
  class SetFileType {

    @Test
    @DisplayName("When fileType is IMAGE, should set it")
    void shouldSetImageFileType() {
      PaymentProof proof = validProof();
      assertDoesNotThrow(() -> proof.setFileType(ProofFileType.IMAGE));
      assertEquals(ProofFileType.IMAGE, proof.getFileType());
    }

    @Test
    @DisplayName("When fileType is null, should throw BusinessException")
    void shouldThrowWhenNull() {
      PaymentProof proof = validProof();
      BusinessException ex = assertThrows(BusinessException.class, () -> proof.setFileType(null));
      assertEquals(ErrorMessage.PaymentProof.FILE_TYPE_NULL, ex.getErrorMessage());
    }
  }
}
