# Implementation Plan: Backup & Restore

**Branch**: `005-backup-restore` | **Date**: 2026-06-28 | **Spec**: [spec.md](spec.md)  
**Input**: Feature specification from `/specs/005-backup-restore/spec.md`

## Summary

Add a Backup/Restore section to the Settings page that lets users copy app data to a USB drive (as a timestamped ZIP)
and restore from a previously saved backup. Backup uses H2's live `BACKUP TO` SQL command for safe database snapshotting
combined with `ZipOutputStream` for packaging; restore uses a PowerShell exit-and-replace script (same pattern as the
existing auto-update) to work around Windows file locks, followed by an automatic app relaunch.

## Technical Context

**Language/Version**: Java 24  
**Primary Dependencies**: JavaFX 21, Google Guice 7, H2 2.4.240, HikariCP 7, java.util.zip (java.base),
javax.swing.filechooser.FileSystemView (java.desktop — already required)  
**Storage**: H2 embedded database (backup source); USB drive filesystem (backup destination)  
**Testing**: JUnit 5 (maven-surefire-plugin)  
**Target Platform**: Windows desktop (jpackage native installer)  
**Project Type**: Desktop app (JavaFX + Guice)  
**Performance Goals**: Full backup completes in under 2 minutes for a typical dataset (SC-001)  
**Constraints**: Cannot copy `.mv.db` directly while H2 is open (Windows file lock); restore requires JVM exit before
file replacement  
**Scale/Scope**: Single-user, single-machine; all app data fits in one ZIP

## Constitution Check

The project constitution file is a placeholder template with no concrete rules filled in. No formal gates to evaluate.
Proceeding on the basis of the `CLAUDE.md` architectural guidelines:

- ✅ Package-by-Feature: new `backup` feature package with internal layers
- ✅ Domain/application purity: no UI or framework imports in domain or application layers
- ✅ Infrastructure enforces no business rules: service implementations are pure I/O
- ✅ Constructor injection with `@Inject` throughout; no field injection
- ✅ `GuiceFxmlLoader` used for any FXML loading (no direct `new FXMLLoader`)
- ✅ No new persistence/repository layer (backups are files, not DB rows)

## Project Structure

### Documentation (this feature)

```text
specs/005-backup-restore/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output
├── data-model.md        # Phase 1 output
├── quickstart.md        # Phase 1 output
└── tasks.md             # Phase 2 output (/speckit-tasks command)
```

### Source Code (repository root)

```text
src/main/java/com/guilherme/emobiliaria/
├── backup/                                   # NEW feature package
│   ├── domain/
│   │   ├── entity/
│   │   │   ├── RemovableDrive.java           # record(label, path)
│   │   │   └── BackupFile.java               # record(filename, path, timestamp)
│   │   └── service/
│   │       ├── DriveDetectionService.java    # interface
│   │       ├── BackupCreationService.java    # interface
│   │       └── RestoreService.java           # interface
│   ├── application/
│   │   ├── input/
│   │   │   ├── CreateBackupInput.java
│   │   │   ├── ListDriveBackupsInput.java
│   │   │   └── RestoreBackupInput.java
│   │   ├── output/
│   │   │   ├── DetectDrivesOutput.java
│   │   │   ├── ListDriveBackupsOutput.java
│   │   │   └── CreateBackupOutput.java
│   │   └── usecase/
│   │       ├── DetectDrivesInteractor.java
│   │       ├── ListDriveBackupsInteractor.java
│   │       ├── CreateBackupInteractor.java
│   │       └── RestoreBackupInteractor.java
│   ├── infrastructure/
│   │   └── service/
│   │       ├── WindowsDriveDetectionService.java  # FileSystemView-based
│   │       ├── ZipBackupCreationService.java       # H2 BACKUP TO + ZipOutputStream
│   │       └── ProcessRestoreService.java          # PowerShell exit-replace-relaunch
│   ├── di/
│   │   └── BackupModule.java
│   └── ui/
│       └── controller/
│           └── BackupRestoreController.java        # builds section Node + dialog logic
│
├── config/
│   └── ui/
│       └── controller/
│           └── ConfigController.java               # MODIFIED: inject BackupRestoreController
│
└── shared/
    └── exception/                                  # MODIFIED: add backup-specific exceptions
        ├── NoDrivesFoundException.java
        ├── NoBackupsFoundException.java
        ├── InsufficientSpaceException.java
        ├── BackupException.java
        └── RestoreException.java

src/main/java/module-info.java                      # MODIFIED: open backup packages
src/main/resources/
├── messages.properties                             # MODIFIED: add backup.* keys
├── messages_pt_BR.properties                       # MODIFIED: add backup.* keys
└── com/guilherme/emobiliaria/config/ui/view/
    └── config-view.fxml                            # MODIFIED: fx:id on configSections VBox
```

**Structure Decision**: Single-project, package-by-feature. The backup feature is a new top-level package alongside
existing features (contract, receipt, etc.). No new FXML files are needed — all backup dialogs are built
programmatically following the `UpdateProgressWindow` pattern. The config-view.fxml receives only a minor addition (an
`fx:id` attribute on an existing `VBox` so the controller can inject the backup section).

## Complexity Tracking

No constitution violations. Standard feature addition within the established architecture.

---

## Design Decisions (from research.md)

| Concern            | Decision                                                                                          |
|--------------------|---------------------------------------------------------------------------------------------------|
| Drive detection    | `FileSystemView.getSystemTypeDescription()` on `File.listRoots()` — java.desktop already required |
| Database backup    | H2 `BACKUP TO '<temp>.zip'` via JDBC; extract `.mv.db` from H2 zip into final zip                 |
| ZIP creation       | `java.util.zip.ZipOutputStream` (java.base, no new dep)                                           |
| ZIP verification   | Open with `ZipFile`, read all bytes of every entry; fail on any IOException                       |
| Restore mechanism  | Extract to temp; PowerShell waits for PID to die, replaces files, relaunches exe                  |
| Progress dialogs   | Programmatic Stage, `Modality.APPLICATION_MODAL`, close-request consumed                          |
| Background tasks   | `javafx.concurrent.Task` + `new Thread(task).start()` (existing pattern)                          |
| Confirmation word  | `messages.properties` key `backup.restore.confirm.word` — "RESTORE" / "RESTAURAR"                 |
| Space check        | `Files.getFileStore(path).getUsableSpace()` vs. total file sizes before backup                    |
| Restart on restore | `ProcessHandle.current().info().command()` for exe path; `System.exit(0)` after script            |
