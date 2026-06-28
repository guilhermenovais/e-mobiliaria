# Data Model: Backup & Restore

## Domain Entities / Value Objects

The backup feature introduces **no persisted entities** (no new database tables, no new repository interfaces beyond
what domain services need). All data is stored as files on USB drives. The two domain value objects capture what's
needed to pass between layers.

---

### `RemovableDrive`

Represents a detected removable USB drive connected to the system.

| Field   | Type     | Notes                                                  |
|---------|----------|--------------------------------------------------------|
| `label` | `String` | Display name shown to the user (e.g., "Kingston (E:)") |
| `path`  | `Path`   | Root path of the drive (e.g., `E:\`)                   |

**Validation**: `path` must be non-null and refer to an existing, accessible directory at detection time.

---

### `BackupFile`

Represents a backup ZIP file found on a USB drive's `e-mobiliaria-backup` folder.

| Field       | Type            | Notes                                                   |
|-------------|-----------------|---------------------------------------------------------|
| `filename`  | `String`        | ZIP filename, e.g., `backup-2026-06-28-1530.zip`        |
| `path`      | `Path`          | Absolute path to the ZIP file                           |
| `timestamp` | `LocalDateTime` | Parsed from the filename; used for display and ordering |

**Ordering**: Most recent `timestamp` first.

**Filename format**: `backup-YYYY-MM-DD-HHmm.zip` where the date/time is the UTC+local timestamp at backup creation
time.

---

## Domain Service Interfaces

### `DriveDetectionService`

```
detectRemovableDrives() → List<RemovableDrive>
```

Returns all currently connected removable drives in arbitrary order. Returns an empty list (never null) when no drives
are found.

---

### `BackupCreationService`

```
createBackup(targetDrivePath: Path) → void
  throws InsufficientSpaceException
  throws BackupException
```

Creates a timestamped ZIP in `<targetDrivePath>/e-mobiliaria-backup/`, verifies integrity, and leaves the ZIP file on
the drive. Throws `InsufficientSpaceException` when the drive has less free space than the estimated backup size. Throws
`BackupException` for any other failure (including integrity verification failure).

---

### `RestoreService`

```
restore(backupFilePath: Path) → void
  throws RestoreException
```

Extracts the backup ZIP to a temp directory, launches the OS-level restore script (waits for JVM exit, replaces files,
relaunches app), then terminates the JVM. This method does not return normally on success — the JVM exits.

---

## Application Layer: Use Cases

| Interactor                   | Input                                      | Output                                     |
|------------------------------|--------------------------------------------|--------------------------------------------|
| `DetectDrivesInteractor`     | (none)                                     | `DetectDrivesOutput(List<RemovableDrive>)` |
| `ListDriveBackupsInteractor` | `ListDriveBackupsInput(drivePath: Path)`   | `ListDriveBackupsOutput(List<BackupFile>)` |
| `CreateBackupInteractor`     | `CreateBackupInput(drivePath: Path)`       | `CreateBackupOutput` (marker, success)     |
| `RestoreBackupInteractor`    | `RestoreBackupInput(backupFilePath: Path)` | (does not return — exits JVM)              |

---

## Exceptions

| Exception                    | Extends             | Meaning                                      |
|------------------------------|---------------------|----------------------------------------------|
| `InsufficientSpaceException` | `BusinessException` | Drive has less free space than required      |
| `BackupException`            | `BusinessException` | General backup failure (I/O, integrity)      |
| `RestoreException`           | `BusinessException` | General restore failure (extraction, script) |
| `NoDrivesFoundException`     | `BusinessException` | No removable drives detected                 |
| `NoBackupsFoundException`    | `BusinessException` | No backup files found on the selected drive  |

(`BusinessException` already exists at `shared/exception/BusinessException.java`.)

---

## ZIP Archive Structure

```
backup-YYYY-MM-DD-HHmm.zip
├── database/
│   └── emobiliaria.mv.db
└── proofs/
    ├── <proof-file-1>
    ├── <proof-file-2>
    └── ...
```

Proofs are stored flat under the `proofs/` entry prefix (preserving filenames only, not subdirectory structure, since
`AppDataPaths.proofStorageDirectory()` is a flat directory).

---

## State Transitions

### Backup Flow

```
[Idle] → user clicks Backup
  → detect drives
  → [No drives] → show error → [Idle]
  → [1 drive] → show confirmation (drive name + path) → user confirms
  → [>1 drive] → show drive selection dialog → user picks
  → show "Backing up…" progress modal (non-closeable)
  → createBackup on background thread
    → [InsufficientSpaceException] → dismiss modal → show space error
    → [BackupException] → dismiss modal → show error
    → [success] → dismiss modal → show success
  → [Idle]
```

### Restore Flow

```
[Idle] → user clicks Restore
  → detect drives
  → [No drives] → show error → [Idle]
  → [1 drive] → listBackups on that drive
  → [>1 drive] → show drive selection dialog → user picks → listBackups
  → [NoBackupsFoundException] → show "no backups" error → [Idle]
  → show backup-list dialog (most recent pre-selected)
  → user clicks Restore button
  → show confirmation dialog (type RESTORE / RESTAURAR)
    → user clicks Cancel → [backup-list dialog]
    → user types wrong word + clicks Restore → show validation error
    → user types correct word + clicks Restore
  → show "Restoring…" progress modal (non-closeable)
  → restoreBackup on background thread
    → [RestoreException] → dismiss modal → show error → [Idle]
    → [success] → JVM exits (app relaunches via OS script)
```
