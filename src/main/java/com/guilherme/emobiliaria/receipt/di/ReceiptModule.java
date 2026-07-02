package com.guilherme.emobiliaria.receipt.di;

import com.google.inject.AbstractModule;
import com.guilherme.emobiliaria.receipt.domain.repository.PaymentProofRepository;
import com.guilherme.emobiliaria.receipt.domain.repository.ReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofPdfEmbeddingService;
import com.guilherme.emobiliaria.receipt.domain.service.PaymentProofStorageService;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptExportService;
import com.guilherme.emobiliaria.receipt.domain.service.ReceiptFileService;
import com.guilherme.emobiliaria.receipt.infrastructure.repository.JdbcPaymentProofRepository;
import com.guilherme.emobiliaria.receipt.infrastructure.repository.JdbcReceiptRepository;
import com.guilherme.emobiliaria.receipt.infrastructure.service.FileSystemReceiptExportService;
import com.guilherme.emobiliaria.receipt.infrastructure.service.OpenPdfProofEmbeddingService;
import com.guilherme.emobiliaria.receipt.infrastructure.service.PaymentProofStorageServiceImpl;
import com.guilherme.emobiliaria.receipt.infrastructure.service.ReceiptFileServiceImpl;

public class ReceiptModule extends AbstractModule {

  @Override
  protected void configure() {
    bind(ReceiptRepository.class).to(JdbcReceiptRepository.class);
    bind(ReceiptFileService.class).to(ReceiptFileServiceImpl.class);
    bind(ReceiptExportService.class).to(FileSystemReceiptExportService.class);
    bind(PaymentProofRepository.class).to(JdbcPaymentProofRepository.class);
    bind(PaymentProofStorageService.class).to(PaymentProofStorageServiceImpl.class);
    bind(PaymentProofPdfEmbeddingService.class).to(OpenPdfProofEmbeddingService.class);
  }
}
