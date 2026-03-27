package com.guilherme.emobiliaria.contract.infrastructure.service;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.service.ContractFileService;
import com.guilherme.emobiliaria.shared.pdf.PdfGenerationService;
import com.guilherme.emobiliaria.shared.pdf.templates.ContractTemplate;
import jakarta.inject.Inject;

public class ContractFileServiceImpl implements ContractFileService {

  private final PdfGenerationService pdfGenerationService;

  @Inject
  public ContractFileServiceImpl(PdfGenerationService pdfGenerationService) {
    this.pdfGenerationService = pdfGenerationService;
  }

  @Override
  public byte[] generateContractPdf(Contract contract) {
    return pdfGenerationService.generatePdf(new ContractTemplate(contract));
  }
}
