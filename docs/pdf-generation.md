# Pdf Generation Instructions

- The PDFs in this project are generated using JasperReports.
- The JasperReport templates are in the src/main/resources/reports folder.
- The generation is handled by the class com.guilherme.emobiliaria.shared.pdf.PdfGenerationService. To generate a PDF,
  it's generatePdf method should be called with a PdfTemplate as parameter.
- PdfTemplates are defined in the com.guilherme.emobiliaria.shared.pdf.templates package.
- Inside each PdfTemplate implementation, there should be two enums defined: <TemplateName>Parameters and <TemplateName>
  Collections.
- PdfTemplates should receive the entities it needs to fill the parameters and collections. The extraction of the data
  from the entities should be done in the PdfTemplate implementation.
