# Tasks: Backup & Restore

**Input**: Design documents from `/specs/005-backup-restore/`
**Prerequisites**: plan.md ✅, spec.md ✅, data-model.md ✅, research.md ✅, quickstart.md ✅

**Organization**: Tasks are grouped by user story to enable independent implementation and testing.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Can run in parallel (different files, no blocking dependencies)
- **[Story]**: Which user story this task belongs to (US1 = Backup, US2 = Restore)
- Exact file paths are included in every task description

## Path Conventions

Java source root: `src/main/java/com/guilherme/emobiliaria/`  
Resources root: `src/main/resources/`

---

## Phase 1: Setup (Domain and Application Layer)

**Purpose**: Create all pure-Java domain and application-layer artifacts — exceptions, value objects,
service interfaces, input/output records, and use-case interactors. No framework or UI dependencies.

- [X] T001 [P] Create backup exception classes `NoDrivesFoundException`, `NoBackupsFoundException`,
  `InsufficientSpaceException`, `BackupException`, `RestoreException` (each extending `BusinessException`) in
  `src/main/java/com/guilherme/emobiliaria/shared/exception/`
- [X] T002 [P] Create domain value objects `RemovableDrive` (record: `String label`, `Path path`) and `BackupFile` (
  record: `String filename`, `Path path`, `LocalDateTime timestamp`; implements `Comparable<BackupFile>` descending by
  timestamp) in `src/main/java/com/guilherme/emobiliaria/backup/domain/entity/`
- [X] T003 Create domain service interfaces `DriveDetectionService` (`detectRemovableDrives(): List<RemovableDrive>`),
  `BackupCreationService` (`createBackup(Path): void` throws `InsufficientSpaceException`, `BackupException`), and
  `RestoreService` (`restore(Path): void` throws `RestoreException`) in
  `src/main/java/com/guilherme/emobiliaria/backup/domain/service/`
- [X] T004 [P] Create application input records `CreateBackupInput(Path drivePath)`,
  `ListDriveBackupsInput(Path drivePath)`, `RestoreBackupInput(Path backupFilePath)` in
  `src/main/java/com/guilherme/emobiliaria/backup/application/input/`
- [X] T005 Create application output records `DetectDrivesOutput(List<RemovableDrive> drives)`,
  `ListDriveBackupsOutput(List<BackupFile> backups)`, `CreateBackupOutput` (marker record) in
  `src/main/java/com/guilherme/emobiliaria/backup/application/output/`
- [X] T006 Create use-case interactors `DetectDrivesInteractor` (throws `NoDrivesFoundException` on empty list),
  `ListDriveBackupsInteractor` (scans `e-mobiliaria-backup/*.zip`, parses filenames, sorts descending, throws
  `NoBackupsFoundException`), `CreateBackupInteractor` (delegates to `BackupCreationService`),
  `RestoreBackupInteractor` (delegates to `RestoreService`) in
  `src/main/java/com/guilherme/emobiliaria/backup/application/usecase/`

**Checkpoint**: Pure domain and application layer is complete — no compilation requires framework or UI classes

---

## Phase 2: Foundational (DI Wiring and UI Skeleton)

**Purpose**: Wire the backup feature into the app's DI container and expose the Settings section entry
point. All changes here are non-breaking stubs; no real functionality is added yet.

**⚠️ CRITICAL**: Complete this phase before implementing any user story — the app will not compile
or run with backup functionality until BackupModule, AppModule, and module-info are wired.

- [X] T007 [P] Create `BackupModule.java` skeleton (empty `configure()` body for now) in
  `src/main/java/com/guilherme/emobiliaria/backup/di/BackupModule.java`
- [X] T008 [P] Add `install(new BackupModule())` to `AppModule.configure()` in
  `src/main/java/com/guilherme/emobiliaria/shared/di/AppModule.java`
- [X] T009 [P] Open backup packages to Guice and JavaFX FXML in `src/main/java/module-info.java`:
  `opens com.guilherme.emobiliaria.backup.di to com.google.guice`,
  `opens com.guilherme.emobiliaria.backup.application.usecase to com.google.guice`,
  `opens com.guilherme.emobiliaria.backup.infrastructure.service to com.google.guice`,
  `opens com.guilherme.emobiliaria.backup.ui.controller to javafx.fxml, com.google.guice`
- [X] T010 [P] Add `fx:id="configSections"` attribute to the existing `<VBox styleClass="config-sections">` element in
  `src/main/resources/com/guilherme/emobiliaria/config/ui/view/config-view.fxml`
- [X] T011 Create `BackupRestoreController.java` with `@Inject` constructor accepting all four interactors and
  `ResourceBundle`; add stub `buildSection()` returning an empty `VBox`; add `@FXML VBox configSections` field, inject
  `BackupRestoreController` in `ConfigController.java` constructor, and call
  `configSections.getChildren().add(backupRestoreController.buildSection())` in `initialize()` in files
  `src/main/java/com/guilherme/emobiliaria/backup/ui/controller/BackupRestoreController.java` and
  `src/main/java/com/guilherme/emobiliaria/config/ui/controller/ConfigController.java`

**Checkpoint**: App compiles and launches; Settings page renders without the backup section (buildSection returns empty
VBox)

---

## Phase 3: User Story 1 — Backup App Data to USB Drive (Priority: P1) 🎯 MVP

**Goal**: Users can click "Backup" in Settings, confirm or select a USB drive, and receive a verified
timestamped ZIP at `e-mobiliaria-backup/backup-YYYY-MM-DD-HHmm.zip` on the drive.

**Independent Test**: Connect a USB drive, open Settings, click "Backup", confirm the drive prompt,
and verify that a valid ZIP file appears in `<drive>:\e-mobiliaria-backup\` and contains
`database/emobiliaria.mv.db` plus all files from the proofs directory.

### Implementation for User Story 1

- [X] T012 [P] [US1] Implement `WindowsDriveDetectionService.java` using
  `FileSystemView.getFileSystemView().getSystemTypeDescription(root)` on `File.listRoots()` to filter removable drives;
  build `RemovableDrive(label, root.toPath())` per match in
  `src/main/java/com/guilherme/emobiliaria/backup/infrastructure/service/WindowsDriveDetectionService.java`
- [X] T013 [P] [US1] Implement `ZipBackupCreationService.java`: calculate required space (database `.mv.db` size +
  proofs total), check `Files.getFileStore(targetPath).getUsableSpace()` and throw `InsufficientSpaceException` if
  insufficient; create `e-mobiliaria-backup/` dir if absent; run `BACKUP TO '<temp>.zip'` via JDBC; extract
  `emobiliaria.mv.db` from H2 ZIP; write final ZIP with `ZipOutputStream` (entries: `database/emobiliaria.mv.db` +
  `proofs/<filename>` for each proofs file); verify integrity with `ZipFile` (read all entries via
  `transferTo(OutputStream.nullOutputStream())`); delete temp H2 ZIP in
  `src/main/java/com/guilherme/emobiliaria/backup/infrastructure/service/ZipBackupCreationService.java`
- [X] T014 [P] [US1] Add backup keys to `src/main/resources/messages.properties` (`backup.section.title`,
  `backup.section.description`, `backup.button.backup`, `backup.button.restore`, `backup.error.no_drives`,
  `backup.error.insufficient_space`, `backup.error.failed`, `backup.success`, `backup.progress.backing_up`,
  `backup.drive.select.title`) and matching translated keys to `src/main/resources/messages_pt_BR.properties`
- [X] T015 [US1] Bind `DriveDetectionService` → `WindowsDriveDetectionService` and `BackupCreationService` →
  `ZipBackupCreationService` in `src/main/java/com/guilherme/emobiliaria/backup/di/BackupModule.java`
- [X] T016 [US1] Implement `BackupRestoreController.buildSection()` to return a `VBox` containing: section title label,
  description label, and two `Button`s ("Backup" and "Restore") wired to `onBackup()` and `onRestore()` stubs, using
  i18n keys from the injected `ResourceBundle` in
  `src/main/java/com/guilherme/emobiliaria/backup/ui/controller/BackupRestoreController.java`
- [X] T017 [US1] Implement `BackupRestoreController.onBackup()`: call `DetectDrivesInteractor` → catch
  `NoDrivesFoundException` and show error alert; if 1 drive show confirmation dialog (drive label + path); if >1 drives
  show programmatic `Stage` with `ListView<RemovableDrive>` for selection; show non-closeable `APPLICATION_MODAL`
  progress stage ("Backing up…") with `ProgressBar.INDETERMINATE_PROGRESS`; run `CreateBackupInteractor` on background
  `javafx.concurrent.Task`; on success close modal and show success alert; on `InsufficientSpaceException` close modal
  and show space-specific error alert; on other failure close modal and show generic error alert in
  `src/main/java/com/guilherme/emobiliaria/backup/ui/controller/BackupRestoreController.java`

**Checkpoint**: Backup flow is fully functional and independently testable — ZIP appears on USB drive after clicking
Backup

---

## Phase 4: User Story 2 — Restore App Data from USB Drive (Priority: P2)

**Goal**: Users can click "Restore" in Settings, select a backup file from the list (most recent
pre-selected), type the locale-specific confirmation word, and have the app replace its data and
automatically restart.

**Independent Test**: With a valid backup ZIP on a connected USB drive, open Settings, click
"Restore", select the backup from the list, type "RESTORE" (or "RESTAURAR" in pt_BR), click
"Restore", and verify the app closes and relaunches with the restored data.

### Implementation for User Story 2

- [X] T018 [US2] Implement `ProcessRestoreService.java`: extract backup ZIP to
  `Files.createTempDirectory("emobiliaria-restore-")` (preserving `database/` and `proofs/` structure); get current PID
  via `ProcessHandle.current().pid()` and exe path via `ProcessHandle.current().info().command().orElseThrow()`; build
  PowerShell script that polls until PID is gone, replaces database directory contents, replaces proofs directory
  contents, and starts the exe; Base64-encode and launch via `powershell.exe -NonInteractive -EncodedCommand`; call
  `Platform.exit()` then `System.exit(0)` in
  `src/main/java/com/guilherme/emobiliaria/backup/infrastructure/service/ProcessRestoreService.java`
- [X] T019 [P] [US2] Add restore i18n keys to `src/main/resources/messages.properties` (`backup.restore.confirm.title`,
  `backup.restore.confirm.message`, `backup.restore.confirm.word=RESTORE`, `backup.restore.confirm.error`,
  `backup.restore.no_backups`, `backup.progress.restoring`) and matching translated keys (with
  `backup.restore.confirm.word=RESTAURAR`) to `src/main/resources/messages_pt_BR.properties`
- [X] T020 [US2] Bind `RestoreService` → `ProcessRestoreService` in
  `src/main/java/com/guilherme/emobiliaria/backup/di/BackupModule.java`
- [X] T021 [US2] Implement `BackupRestoreController.onRestore()`: call `DetectDrivesInteractor` → catch
  `NoDrivesFoundException` and show error alert; drive selection for >1 drives (reuse same dialog as onBackup); call
  `ListDriveBackupsInteractor` → catch `NoBackupsFoundException` and show error alert; show programmatic `Stage` with
  `ListView<BackupFile>` (most recent pre-selected) and "Restore" button; on "Restore" click show confirmation dialog
  with message requiring typed confirmation word, text field, and Restore/Cancel buttons; on word mismatch show inline
  validation error; on word match close list dialog and show non-closeable `APPLICATION_MODAL` progress stage ("
  Restoring…"); run `RestoreBackupInteractor` on background `javafx.concurrent.Task` (JVM exits on success); on failure
  close modal and show error alert in
  `src/main/java/com/guilherme/emobiliaria/backup/ui/controller/BackupRestoreController.java`

**Checkpoint**: Restore flow is fully functional — app replaces data and automatically relaunches after successful
restore

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Final validation and quickstart walkthrough.

- [ ] T022 Run quickstart.md end-to-end validation: verify backup creates a valid ZIP containing
  `database/emobiliaria.mv.db` and all proof files; verify restore replaces database and proofs and relaunches the app;
  verify all error paths (no drive, insufficient space, bad confirmation word) show correct messages

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: No dependencies — start immediately; T001 and T002 are parallel; T004 is parallel to T001/T002;
  T003 and T005 depend on T002; T006 depends on T001, T003, T004, T005
- **Foundational (Phase 2)**: Depends on Phase 1 completion — BLOCKS all user stories; T007–T010 are parallel; T011
  depends on T007
- **User Story 1 (Phase 3)**: Depends on Foundational completion; T012, T013, T014 are parallel; T015 depends on T012
  and T013; T016 depends on T014; T017 depends on T015 and T016
- **User Story 2 (Phase 4)**: Depends on Foundational completion; T018 and T019 are parallel; T020 depends on T018; T021
  depends on T019 and T020
- **Polish (Phase 5)**: Depends on both user stories being complete

### User Story Dependencies

- **US1 (P1)**: Can start after Foundational — no dependency on US2
- **US2 (P2)**: Can start after Foundational — depends on US1 being visually present (Restore button already created in
  T016) but restore infrastructure is fully independent of backup infrastructure

### Parallel Opportunities

- T001, T002, T004 (Phase 1 — different files, no internal deps)
- T007, T008, T009, T010 (Phase 2 — different files, no internal deps)
- T012, T013, T014 (Phase 3 — different files, no internal deps)
- T018, T019 (Phase 4 — different files, no internal deps)

---

## Parallel Example: User Story 1

```bash
# These three tasks have no interdependencies and can run concurrently:
Task T012: "Implement WindowsDriveDetectionService.java"
Task T013: "Implement ZipBackupCreationService.java"
Task T014: "Add backup i18n keys to messages.properties and messages_pt_BR.properties"
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (domain + application layer)
2. Complete Phase 2: Foundational (DI wiring, FXML change, controller skeleton)
3. Complete Phase 3: User Story 1 (backup infrastructure + UI)
4. **STOP and VALIDATE**: Connect a USB drive and test the full backup flow
5. Deliver backup capability

### Incremental Delivery

1. Complete Setup + Foundational → app compiles and Settings page loads (empty backup section)
2. Add User Story 1 → backup works → validate independently → demo
3. Add User Story 2 → restore works → validate independently → demo
4. Polish → final validation

---

## Notes

- [P] tasks operate on different files with no blocking dependencies and can be worked concurrently
- [US1]/[US2] labels map each task to its user story for traceability
- No test tasks generated — not requested in the feature specification
- `BackupModule` is created as a skeleton in T007 (Phase 2) and updated incrementally in T015 (Phase 3) and T020 (Phase
  4) as infrastructure classes become available
- The PowerShell restore script in T018 follows the same pattern as the existing `UpdateService.applyUpdate()` — refer
  to that implementation for Base64-encoding approach and `ProcessBuilder` usage
- `BackupRestoreController` progress dialogs must consume close requests (`stage.setOnCloseRequest(Event::consume)`) and
  use `Modality.APPLICATION_MODAL`, following the `UpdateProgressWindow` pattern
- Restore always terminates the JVM at the end — `RestoreBackupInteractor.execute()` never returns normally on success
