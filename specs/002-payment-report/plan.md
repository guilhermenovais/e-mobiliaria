# Implementation Plan: Payment Report

**Branch**: `002-payment-report` | **Date**: 2026-05-12 | **Spec**: [spec.md](spec.md)
**Input**: Feature specification from `/specs/002-payment-report/spec.md`

## Summary

Add a Payment Report screen that lets the property manager select a month from a dropdown and view payment status (paid,
unpaid, vacant) for every registered property, with PDF export. The feature extends the existing `reports` module: new
`PaymentReportRow` domain entity → new repository methods on `ReportRepository` → three new interactors → new
`PaymentReportController` + FXML view + JasperReports template. No schema migrations required.

## Technical Context

**Language/Version**: Java 24  
**Primary Dependencies**: JavaFX 21 (UI), Google Guice 7 (DI), H2 embedded via JDBC (bare SQL, no ORM), JasperReports +
OpenPDF (PDF generation)  
**Storage**: H2 SQL embedded database; JDBC `PreparedStatement` pattern throughout  
**Testing**: JUnit Jupiter 5; unit tests for domain entity and interactors using fake repositories; JDBC integration
tests for repository methods  
**Target Platform**: Desktop (Linux / macOS / Windows) — JavaFX desktop app  
**Project Type**: Desktop app  
**Performance Goals**: SC-001 — table renders in under 3 seconds; a single SQL join query is sufficient  
**Constraints**: Domain and application layers must be pure Java (no UI/JDBC imports); constructor injection only (
`@jakarta.inject.Inject`); no field injection; all FXML loading via `GuiceFxmlLoader`  
**Scale/Scope**: Small portfolio (< 100 properties, < 1000 contracts)

## Constitution Check

The project constitution file (`.specify/memory/constitution.md`) is an unfilled placeholder template with no active
principles. No gates to evaluate. Proceeding.

## Project Structure

### Documentation (this feature)

```text
specs/002-payment-report/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
└── tasks.md             # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

The feature is entirely within the existing `reports` module. No new top-level packages or modules.

```text
src/main/java/com/guilherme/emobiliaria/reports/
├── domain/entity/
│   ├── PaymentReportRow.java                     (NEW — record)
│   └── PaymentReportRowStatus.java               (NEW — enum: PAID, UNPAID, VACANT)
├── domain/repository/
│   └── ReportRepository.java                     (EXTEND — +2 methods)
├── domain/service/
│   └── ReportFileService.java                    (EXTEND — +1 method)
├── application/input/
│   ├── GetPaymentReportMonthsInput.java           (NEW — empty record)
│   ├── LoadPaymentReportInput.java                (NEW — record with YearMonth)
│   └── GeneratePaymentReportPdfInput.java         (NEW — record with YearMonth)
├── application/output/
│   ├── GetPaymentReportMonthsOutput.java          (NEW — record with List<YearMonth>)
│   ├── LoadPaymentReportOutput.java               (NEW — record with List<PaymentReportRow>)
│   └── GeneratePaymentReportPdfOutput.java        (NEW — record with byte[])
├── application/usecase/
│   ├── GetPaymentReportMonthsInteractor.java      (NEW)
│   ├── LoadPaymentReportInteractor.java           (NEW)
│   └── GeneratePaymentReportPdfInteractor.java    (NEW)
├── infrastructure/repository/
│   └── JdbcReportRepository.java                 (EXTEND — implement 2 new methods)
└── infrastructure/service/
    ├── ReportFileServiceImpl.java                 (EXTEND — implement new PDF method)
    └── PaymentReportRowBean.java                  (NEW — JasperReports DTO)

src/main/resources/com/guilherme/emobiliaria/reports/ui/view/
├── payment-report-view.fxml                       (NEW)
└── payment-report-view.css                        (NEW)

src/main/resources/reports/
└── payment_report.jrxml                           (NEW — JasperReports template)

src/main/java/com/guilherme/emobiliaria/reports/ui/controller/
├── ReportsController.java                         (EXTEND — add payment report card)
└── PaymentReportController.java                   (NEW)

src/main/java/module-info.java                     (EXTEND — no new packages; verify existing opens are sufficient)
```

**Structure Decision**: Extends existing `reports` module only. `ReportsModule.java` requires no changes — the three new
interactors are concrete classes resolved via Guice JIT binding; no new interface-to-implementation bindings needed.

## Complexity Tracking

> *No constitution violations — table not applicable.*
