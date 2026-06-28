package com.guilherme.emobiliaria.receipt.domain.entity;

import com.guilherme.emobiliaria.shared.exception.BusinessException;
import com.guilherme.emobiliaria.shared.exception.ErrorMessage;

import java.time.LocalDate;

public class PaymentProof {

  private Long id;
  private String originalFileName;
  private String storedFileName;
  private ProofFileType fileType;
  private LocalDate attachedAt;
  private Long receiptId;

  private PaymentProof() {
  }

  public static PaymentProof create(String originalFileName, String storedFileName,
      ProofFileType fileType, LocalDate attachedAt, Long receiptId) {
    PaymentProof proof = new PaymentProof();
    proof.setOriginalFileName(originalFileName);
    proof.setStoredFileName(storedFileName);
    proof.setFileType(fileType);
    proof.setAttachedAt(attachedAt);
    proof.setReceiptId(receiptId);
    return proof;
  }

  public static PaymentProof restore(Long id, String originalFileName, String storedFileName,
      ProofFileType fileType, LocalDate attachedAt, Long receiptId) {
    PaymentProof proof = create(originalFileName, storedFileName, fileType, attachedAt, receiptId);
    proof.setId(id);
    return proof;
  }

  public Long getId() {
    return id;
  }

  public void setId(Long id) {
    this.id = id;
  }

  public String getOriginalFileName() {
    return originalFileName;
  }

  public void setOriginalFileName(String originalFileName) {
    if (originalFileName == null || originalFileName.isBlank()) {
      throw new BusinessException(ErrorMessage.PaymentProof.ORIGINAL_FILENAME_BLANK);
    }
    this.originalFileName = originalFileName;
  }

  public String getStoredFileName() {
    return storedFileName;
  }

  public void setStoredFileName(String storedFileName) {
    if (storedFileName == null || storedFileName.isBlank()) {
      throw new BusinessException(ErrorMessage.PaymentProof.STORED_FILENAME_BLANK);
    }
    this.storedFileName = storedFileName;
  }

  public ProofFileType getFileType() {
    return fileType;
  }

  public void setFileType(ProofFileType fileType) {
    if (fileType == null) {
      throw new BusinessException(ErrorMessage.PaymentProof.FILE_TYPE_NULL);
    }
    this.fileType = fileType;
  }

  public LocalDate getAttachedAt() {
    return attachedAt;
  }

  public void setAttachedAt(LocalDate attachedAt) {
    if (attachedAt == null) {
      throw new BusinessException(ErrorMessage.PaymentProof.ATTACHED_AT_NULL);
    }
    this.attachedAt = attachedAt;
  }

  public Long getReceiptId() {
    return receiptId;
  }

  public void setReceiptId(Long receiptId) {
    if (receiptId == null) {
      throw new BusinessException(ErrorMessage.PaymentProof.RECEIPT_ID_NULL);
    }
    this.receiptId = receiptId;
  }
}
