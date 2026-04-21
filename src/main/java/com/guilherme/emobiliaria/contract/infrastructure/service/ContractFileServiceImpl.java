package com.guilherme.emobiliaria.contract.infrastructure.service;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.service.ContractFileService;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.ContractRescissionTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.ContractTemplate;
import com.guilherme.emobiliaria.shared.pdf.templates.ContractTerminationNoticeTemplate;
import jakarta.inject.Inject;

import java.time.LocalDate;
import java.util.ResourceBundle;

public class ContractFileServiceImpl implements ContractFileService {

  private final PdfGenerationService pdfGenerationService;
  private final ResourceBundle bundle;

  @Inject
  public ContractFileServiceImpl(PdfGenerationService pdfGenerationService, ResourceBundle bundle) {
    this.pdfGenerationService = pdfGenerationService;
    this.bundle = bundle;
  }

  @Override
  public byte[] generateContractPdf(Contract contract) {
    return pdfGenerationService.generatePdf(new ContractTemplate(contract, bundle));
  }

  @Override
  public byte[] generateRescissionPdf(Contract contract) {
    LocalDate noticeDate =
        contract.getRescindedAt() != null ? contract.getRescindedAt() : LocalDate.now();
    return pdfGenerationService.generatePdf(
        new ContractRescissionTemplate(contract, noticeDate, bundle));
  }

  @Override
  public byte[] generateTerminationNoticePdf(Contract contract) {
    return pdfGenerationService.generatePdf(
        new ContractTerminationNoticeTemplate(contract, LocalDate.now(), bundle));
  }
}
