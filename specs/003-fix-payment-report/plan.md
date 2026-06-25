# Implementation Plan: Fix Payment Report

**Branch**: `003-fix-payment-report` | **Date**: 2026-06-25 | **Spec**: `specs/003-fix-payment-report/spec.md`
**Input**: Feature specification from `/specs/003-fix-payment-report/spec.md`

## Summary

Fix four issues in the payment report: (1) group payments by receipt date instead of payment due date, (2) show adjusted
rent values (rent - discount + fine), (3) order PDF rows by receipt date, and (4) include contracts starting mid-month.
All changes are query and application logic — no database schema or UI layout changes required.

## Technical Context

**Language/Version**: Java 24
**Primary Dependencies**: JavaFX, Google Guice 7, JasperReports 7.0.3, H2 (embedded DB)
**Storage**: H2 relational database with Flyway migrations
**Testing**: JUnit 5 with fake repositories (unit) and H2 in-memory DB (integration)
**Target Platform**: Desktop (Linux/macOS/Windows)
**Project Type**: Desktop app (JavaFX)
**Performance Goals**: N/A (desktop app, small dataset)
**Constraints**: N/A
**Scale/Scope**: Single-user desktop app managing property lease contracts

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

Constitution is uninitialized (template only). No gates to check. ✅

## Project Structure

### Documentation (this feature)

```text
specs/003-fix-payment-report/
├── plan.md              # This file
├── research.md          # Phase 0 output — query restructuring decisions
├── data-model.md        # Phase 1 output — behavioral changes to PaymentReportRow
├── quickstart.md        # Phase 1 output — key files and build instructions
└── tasks.md             # Phase 2 output (created by /speckit-tasks)
```

### Source Code (repository root)

```text
src/main/java/com/guilherme/emobiliaria/
├── reports/
│   ├── domain/entity/
│   │   ├── PaymentReportRow.java          # No changes
│   │   └── PaymentReportRowStatus.java    # No changes
│   ├── domain/repository/
│   │   └── ReportRepository.java          # No changes
│   ├── application/usecase/
│   │   ├── LoadPaymentReportInteractor.java        # No changes
│   │   └── GeneratePaymentReportPdfInteractor.java # No changes
│   ├── infrastructure/repository/
│   │   └── JdbcReportRepository.java      # ✏️ MODIFY: rewrite loadPaymentReportData() SQL
│   ├── infrastructure/service/
│   │   ├── ReportFileServiceImpl.java     # ✏️ MODIFY: change PDF sorting logic
│   │   └── PaymentReportRowBean.java      # No changes
│   └── ui/controller/
│       └── PaymentReportController.java   # No changes (total already sums rent())
└── shared/pdf/templates/
    └── PaymentReportTemplate.java         # No changes

src/test/java/com/guilherme/emobiliaria/
├── reports/infrastructure/repository/
│   └── JdbcReportRepositoryTest.java      # ✏️ MODIFY: add/update integration tests
└── reports/infrastructure/service/
    └── ReportFileServiceImplTest.java     # ✏️ MODIFY: update sorting tests
```

**Structure Decision**: Existing package-by-feature structure. Only infrastructure layer files are modified. No new
packages or classes required.

## Complexity Tracking

No constitution violations to justify.
