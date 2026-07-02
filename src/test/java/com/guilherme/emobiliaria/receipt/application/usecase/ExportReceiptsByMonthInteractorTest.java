package com.guilherme.emobiliaria.receipt.application.usecase;

import com.guilherme.emobiliaria.contract.domain.entity.Contract;
import com.guilherme.emobiliaria.contract.domain.entity.PaymentAccount;
import com.guilherme.emobiliaria.person.domain.entity.Address;
import com.guilherme.emobiliaria.person.domain.entity.BrazilianState;
import com.guilherme.emobiliaria.person.domain.entity.CivilState;
import com.guilherme.emobiliaria.person.domain.entity.Person;
import com.guilherme.emobiliaria.person.domain.entity.PhysicalPerson;
import com.guilherme.emobiliaria.property.domain.entity.Property;
import com.guilherme.emobiliaria.receipt.application.input.ExportReceiptsByMonthInput;
import com.guilherme.emobiliaria.receipt.domain.entity.Receipt;
import com.guilherme.emobiliaria.receipt.domain.entity.ReceiptExportResult;
import com.guilherme.emobiliaria.receipt.domain.repository.FakeReceiptRepository;
import com.guilherme.emobiliaria.receipt.domain.service.FakeReceiptExportService;
import com.guilherme.emobiliaria.receipt.domain.service.FakeReceiptFileService;
import com.guilherme.emobiliaria.shared.exception.ExportFolderNotWritableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.Period;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExportReceiptsByMonthInteractorTest {

  private static final Path FOLDER = Path.of("/exports");

  private final FakeReceiptRepository receiptRepository = new FakeReceiptRepository();
  private final FakeReceiptFileService receiptFileService = new FakeReceiptFileService();
  private final FakeReceiptExportService receiptExportService = new FakeReceiptExportService();
  private final ExportReceiptsByMonthInteractor interactor =
      new ExportReceiptsByMonthInteractor(receiptRepository, receiptFileService,
          receiptExportService);

  private Address validAddress() {
    return Address.create("01001000", "Praça da Sé", "1", null, "Sé", "São Paulo",
        BrazilianState.SP);
  }

  private Contract validContract() {
    Person tenant =
        PhysicalPerson.create("João Silva", "Brasileiro", CivilState.SINGLE, "Engenheiro",
            "529.982.247-25", "MG-1234567", validAddress());
    PaymentAccount paymentAccount =
        PaymentAccount.create("Banco do Brasil", "1234-5", "12345-6", null);
    Property property =
        Property.create("Apto 1", "Apartamento", "1234567890", "0987654321", "IPTU-001",
            validAddress());
    return Contract.create(LocalDate.of(2026, 1, 1), Period.ofMonths(12), 10, 150000, "Residencial",
        paymentAccount, property, tenant, List.of(tenant), List.of(), List.of());
  }

  private Receipt createReceipt(LocalDate date) {
    return receiptRepository.create(
        Receipt.create(date, date, date, date, 0, 0, null, validContract()));
  }

  @Test
  @DisplayName(
      "When all receipts in the month succeed, should export all and write one file per receipt")
  void shouldExportAllReceiptsInMonth() {
    createReceipt(LocalDate.of(2026, 6, 1));
    createReceipt(LocalDate.of(2026, 6, 15));
    createReceipt(LocalDate.of(2026, 6, 30));

    ReceiptExportResult result =
        interactor.execute(new ExportReceiptsByMonthInput(YearMonth.of(2026, 6), FOLDER)).result();

    assertEquals(3, result.exportedCount());
    assertTrue(result.failures().isEmpty());
    assertEquals(3, receiptExportService.writtenFiles().size());
  }

  @Test
  @DisplayName("When one receipt fails to generate, should skip it and export the rest")
  void shouldSkipFailingReceiptAndExportRest() {
    Receipt r1 = createReceipt(LocalDate.of(2026, 6, 1));
    Receipt r2 = createReceipt(LocalDate.of(2026, 6, 15));
    Receipt r3 = createReceipt(LocalDate.of(2026, 6, 30));
    List<Long> createdIds = List.of(r1.getId(), r2.getId(), r3.getId());

    receiptFileService.failNext(RuntimeException::new);

    ReceiptExportResult result =
        interactor.execute(new ExportReceiptsByMonthInput(YearMonth.of(2026, 6), FOLDER)).result();

    assertEquals(2, result.exportedCount());
    assertEquals(1, result.failures().size());
    assertTrue(createdIds.contains(result.failures().get(0).receiptId()));
  }

  @Test
  @DisplayName(
      "When the destination folder is not writable, should abort without returning a partial result")
  void shouldAbortWhenFolderNotWritable() {
    createReceipt(LocalDate.of(2026, 6, 1));
    createReceipt(LocalDate.of(2026, 6, 15));

    receiptExportService.failNext(ExportFolderNotWritableException::new);

    assertThrows(ExportFolderNotWritableException.class,
        () -> interactor.execute(new ExportReceiptsByMonthInput(YearMonth.of(2026, 6), FOLDER)));
  }

  @Test
  @DisplayName("When receipts exist outside the selected month, should never touch them")
  void shouldNotTouchReceiptsOutsideSelectedMonth() {
    createReceipt(LocalDate.of(2026, 6, 1));
    createReceipt(LocalDate.of(2026, 7, 1));

    ReceiptExportResult result =
        interactor.execute(new ExportReceiptsByMonthInput(YearMonth.of(2026, 6), FOLDER)).result();

    assertEquals(1, result.exportedCount());
    assertEquals(1, receiptExportService.writtenFiles().size());
  }
}
