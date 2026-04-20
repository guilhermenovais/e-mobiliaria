package com.guilherme.emobiliaria.contract.infrastructure.service;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.service.ContractFileService;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.ContractTemplate;
import jakarta.inject.Inject;

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
}
