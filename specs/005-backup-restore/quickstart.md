# Quickstart: Backup & Restore Implementation

## Prerequisites

- Java 24, JavaFX 21, Google Guice 7
- `java.desktop` module already required (used for drive detection)
- `java.base` module provides `java.util.zip` (no new dependencies)

## Step-by-Step Implementation Order

### 1. Create exception classes (shared/exception or backup/domain)

Add to `shared/exception/` or `backup/domain/`:

- `NoDrivesFoundException extends BusinessException`
- `NoBackupsFoundException extends BusinessException`
- `InsufficientSpaceException extends BusinessException`
- `BackupException extends BusinessException`
- `RestoreException extends BusinessException`

### 2. Create domain value objects

Create `backup/domain/entity/`:

- `RemovableDrive.java` — record with `label` (String) and `path` (Path)
- `BackupFile.java` — record with `filename`, `path`, `timestamp` (LocalDateTime); implements `Comparable<BackupFile>`
  by timestamp descending

### 3. Create domain service interfaces

Create `backup/domain/service/`:

- `DriveDetectionService.java`
- `BackupCreationService.java`
- `RestoreService.java`

### 4. Create application layer

Create `backup/application/input/`:

- `CreateBackupInput.java` (record: `Path drivePath`)
- `ListDriveBackupsInput.java` (record: `Path drivePath`)
- `RestoreBackupInput.java` (record: `Path backupFilePath`)

Create `backup/application/output/`:

- `DetectDrivesOutput.java` (record: `List<RemovableDrive> drives`)
- `ListDriveBackupsOutput.java` (record: `List<BackupFile> backups`)
- `CreateBackupOutput.java` (marker record)

Create `backup/application/usecase/`:

- `DetectDrivesInteractor` — calls `driveDetectionService.detectRemovableDrives()`, throws `NoDrivesFoundException` if
  empty
- `ListDriveBackupsInteractor` — scans `<drivePath>/e-mobiliaria-backup/*.zip`, parses filenames, sorts descending;
  throws `NoBackupsFoundException` if none found
- `CreateBackupInteractor` — delegates to `backupCreationService.createBackup(input.drivePath())`
- `RestoreBackupInteractor` — delegates to `restoreService.restore(input.backupFilePath())`

### 5. Create infrastructure implementations

Create `backup/infrastructure/service/`:

**`WindowsDriveDetectionService.java`**:

```java
File[] roots = File.listRoots();
FileSystemView fsv = FileSystemView.getFileSystemView();
// Filter: fsv.getSystemTypeDescription(root) contains "Removable" or "remov" (locale-aware)
// Build RemovableDrive(label, root.toPath()) for each match
```

**`ZipBackupCreationService.java`**:

1. Calculate required space: `database .mv.db` size + total proofs size
2. Check `Files.getFileStore(targetDrivePath).getUsableSpace()` → throw `InsufficientSpaceException` if insufficient
3. Ensure `targetDrivePath/e-mobiliaria-backup/` directory exists
4. Generate filename: `backup-<LocalDateTime.now formatted as yyyy-MM-dd-HHmm>.zip`
5. Use H2 `BACKUP TO '<temp>.zip'` via JDBC to create a safe snapshot of the live database
6. Extract `emobiliaria.mv.db` from the H2 ZIP into a temp location
7. Write final ZIP using `ZipOutputStream`:
    - Entry: `database/emobiliaria.mv.db` (from H2 temp snapshot)
    - Entries: `proofs/<filename>` for each file in proofs directory
8. Verify: open final ZIP with `ZipFile`, iterate all entries and `transferTo(OutputStream.nullOutputStream())` — throw
   `BackupException` on any IOException
9. Clean up temp H2 ZIP

**`ProcessRestoreService.java`**:

1. Extract backup ZIP to `Files.createTempDirectory("emobiliaria-restore-")`:
    - Write `database/emobiliaria.mv.db` → `tempDir/database/emobiliaria.mv.db`
    - Write `proofs/<name>` → `tempDir/proofs/<name>`
2. Get current process PID: `ProcessHandle.current().pid()`
3. Get current exe path: `ProcessHandle.current().info().command().orElseThrow()`
4. Get app data paths: `AppDataPaths.databaseDirectory()`, `AppDataPaths.proofStorageDirectory()`
5. Build PowerShell script that:
    - Polls until current PID is dead
    - Removes all files from database directory
    - Copies `tempDir/database/emobiliaria.mv.db` to database directory
    - Removes all files from proofs directory (recursively)
    - Copies all files from `tempDir/proofs/` to proofs directory (recursively)
    - Starts the app executable
6. Base64-encode the script and launch via: `powershell.exe -NonInteractive -EncodedCommand <encoded>`
7. Call `Platform.exit()` then `System.exit(0)`

### 6. Create `BackupModule` and wire to `AppModule`

Create `backup/di/BackupModule.java`:

```java
bind(DriveDetectionService .class).

to(WindowsDriveDetectionService .class);

bind(BackupCreationService .class).

to(ZipBackupCreationService .class);

bind(RestoreService .class).

to(ProcessRestoreService .class);
```

Add `install(new BackupModule())` to `AppModule`.

### 7. Open packages in `module-info.java`

Add:

```java
opens com.guilherme.emobiliaria.backup.di to com.google.guice;
opens com.guilherme.emobiliaria.backup.application.usecase to com.google.guice;
opens com.guilherme.emobiliaria.backup.infrastructure.service to com.google.guice;
opens com.guilherme.emobiliaria.backup.ui.controller to javafx.fxml, com.google.guice;
```

### 8. Create `BackupRestoreController` (UI)

Create `backup/ui/controller/BackupRestoreController.java`:

- Inject: `DetectDrivesInteractor`, `ListDriveBackupsInteractor`, `CreateBackupInteractor`, `RestoreBackupInteractor`,
  `ResourceBundle`
- `buildSection()` → returns a `VBox` Node containing the Backup/Restore section (title, description, two buttons)
- `onBackup()` handler:
    1. `detectDrives.execute()` → catch `NoDrivesFoundException` → show error alert
    2. If 1 drive → confirm dialog with drive label
    3. If >1 drives → drive selection dialog (programmatic `Stage` with `ListView<RemovableDrive>`)
    4. Show progress modal ("Backing up…") — non-closeable APPLICATION_MODAL
    5. Run `CreateBackupInteractor` on background thread
    6. On success → close modal → show success alert
    7. On `InsufficientSpaceException` → close modal → show specific error
    8. On other failure → close modal → show generic error
- `onRestore()` handler:
    1. `detectDrives.execute()` → catch `NoDrivesFoundException` → show error alert
    2. Drive selection (same as backup if >1)
    3. `listBackups.execute(drive)` → catch `NoBackupsFoundException` → show error
    4. Show backup list dialog (programmatic `Stage` with `ListView<BackupFile>`, most recent pre-selected)
    5. User clicks Restore → show confirmation dialog (typed word validation)
    6. On mismatch → inline error in dialog
    7. On match → close list dialog → show progress modal ("Restoring…") — non-closeable
    8. Run `RestoreBackupInteractor` on background thread (JVM exits on success)
    9. On failure → close modal → show error

### 9. Integrate into `ConfigController`

- Add `BackupRestoreController` as constructor injection parameter
- In `initialize()`, call `configSections.getChildren().add(backupRestoreController.buildSection())`
- Add `@FXML VBox configSections` to bind the VBox in config-view.fxml

### 10. Update `config-view.fxml`

- Add `fx:id="configSections"` to the existing `<VBox styleClass="config-sections">` element so the controller can
  append the backup section

### 11. Add i18n keys

Add to `messages.properties` and `messages_pt_BR.properties`:

```
backup.section.title=Backup & Restore
backup.section.description=...
backup.button.backup=Backup
backup.button.restore=Restore
backup.error.no_drives=...
backup.error.insufficient_space=...
backup.error.failed=...
backup.success=...
backup.progress.backing_up=Backing up...
backup.progress.restoring=Restoring...
backup.restore.confirm.title=...
backup.restore.confirm.message=...
backup.restore.confirm.word=RESTORE       (pt_BR: RESTAURAR)
backup.restore.confirm.error=...
backup.restore.no_backups=...
backup.drive.select.title=...
```

## Key Invariants

- All domain service calls happen on a background thread; UI updates go through `Platform.runLater()` or
  `javafx.concurrent.Task` callbacks.
- Progress modals consume close requests (`stage.setOnCloseRequest(Event::consume)`) and use
  `Modality.APPLICATION_MODAL`.
- The restore flow always terminates the JVM at the end — the `RestoreBackupInteractor` never returns normally.
- The backup ZIP is only considered successful after passing integrity verification; a failed verification leaves the (
  corrupt) file on disk for manual inspection.
- The `BackupCreationService` uses H2's `BACKUP TO` command — **never** copy the `.mv.db` file directly while H2 is
  running.
