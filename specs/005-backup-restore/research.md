# Research: Backup & Restore Feature

## Decision 1: Removable USB Drive Detection on Windows

**Decision**: Use `javax.swing.filechooser.FileSystemView` (java.desktop module, already required) to enumerate root
file system paths and identify removable drives by type description.

**Rationale**: The `java.desktop` module is already declared in `module-info.java`.
`FileSystemView.getFileSystemView().getSystemTypeDescription(root)` returns a locale-aware string (e.g., "Removable
Disk") that identifies removable storage on Windows. Iterating `File.listRoots()` gives all mounted drives; filtering by
type description gives removable ones. This avoids spawning a PowerShell/WMI subprocess, keeping the detection
synchronous and simple.

**Alternatives considered**:

- PowerShell `Get-Volume | Where-Object { $_.DriveType -eq 'Removable' }` — works but adds subprocess overhead and a
  potential startup delay; not needed when java.desktop is already available.
- `Files.getFileStores()` — does not expose drive type (removable vs. fixed) in a reliable cross-platform way on
  Windows.

---

## Decision 2: Database Backup Strategy (H2 live backup)

**Decision**: Use H2's SQL command `BACKUP TO '<temp-file>.zip'` executed via JDBC while the database is running.
Extract the `.mv.db` file from H2's backup ZIP, then embed it in the final backup ZIP.

**Rationale**: H2's `BACKUP TO` creates a consistent, hot snapshot of the database file without requiring a connection
pool shutdown. Copying the raw `.mv.db` file while H2 has it open risks data corruption on Windows (file locks +
in-memory pages not flushed). H2's built-in BACKUP command is the documented safe approach.

**Alternatives considered**:

- Direct file copy — unsafe: H2 may hold a file lock and in-memory state may not be flushed.
- `SHUTDOWN COMPACT` + copy — closes all connections and compacts the DB, but requires the entire DI container to be
  torn down and restarted, which is not feasible mid-session.

---

## Decision 3: ZIP Creation and Verification

**Decision**: Use `java.util.zip.ZipOutputStream` (java.base) to create the backup archive. Verification reads every
entry in full using `java.util.zip.ZipFile` — if any entry throws an IOException, the backup is considered failed.

**Rationale**: `ZipOutputStream` / `ZipFile` are part of java.base; no new dependency is needed. The verification
approach (open + read all bytes from all entries) matches the spec requirement exactly and catches CRC mismatches,
truncated writes, and I/O errors.

**ZIP archive layout**:

```
backup-YYYY-MM-DD-HHmm.zip
├── database/emobiliaria.mv.db   (from H2 BACKUP TO temp ZIP)
└── proofs/<filename>…           (all files from proofs/ directory, flat)
```

---

## Decision 4: Restore Mechanism (File Replacement + Restart)

**Decision**: Extract the backup ZIP to a temp directory, then launch a PowerShell script (using `ProcessBuilder` +
Base64-encoded command) that waits for the current JVM process to exit, replaces the app-data files, and re-launches the
app executable. The JVM then calls `Platform.exit()` + `System.exit(0)`.

**Rationale**: On Windows, H2 holds a file lock on the `.mv.db` while the JVM is alive — the file cannot be replaced
in-place. The same PowerShell-delayed-execution pattern is already used in `UpdateService.applyUpdate()`. The current
executable path is retrieved via `ProcessHandle.current().info().command()`.

**PowerShell script outline**:

```powershell
$pid = <current-pid>
while (Get-Process -Id $pid -ErrorAction SilentlyContinue) { Start-Sleep -Milliseconds 500 }
Remove-Item -Path "$dbDir\*" -Force
Copy-Item -Path "$tempDir\database\emobiliaria.mv.db" -Destination "$dbDir\" -Force
Remove-Item -Path "$proofsDir\*" -Recurse -Force
Copy-Item -Path "$tempDir\proofs\*" -Destination "$proofsDir\" -Recurse -Force
Start-Process -FilePath "$exePath"
```

**Alternatives considered**:

- Java `Files.copy()` while JVM is alive — not possible; H2 file lock prevents it on Windows.
- `Platform.runLater` + restart via a new Java process — same outcome but PowerShell script is simpler and already a
  proven pattern in this codebase.

---

## Decision 5: Progress Dialogs (Non-Closeable Modals)

**Decision**: Build progress dialogs programmatically (no FXML), following the exact pattern of `UpdateProgressWindow`.
Use `Stage.initModality(Modality.APPLICATION_MODAL)` + `stage.setOnCloseRequest(Event::consume)` to prevent closing.

**Rationale**: `UpdateProgressWindow` already demonstrates this pattern with `ProgressBar.INDETERMINATE_PROGRESS` for an
indeterminate spinner effect. Building programmatically avoids an extra FXML file for a simple modal.

---

## Decision 6: Background Task Execution

**Decision**: Use `javafx.concurrent.Task<Void>` with `new Thread(task).start()`, setting `onSucceeded` and `onFailed`
handlers on the FX thread. This is the existing pattern in `InitialSetupController`.

---

## Decision 7: Locale-Specific Confirmation Word

**Decision**: Add keys `backup.restore.confirm.word` to `messages.properties` ("RESTORE") and
`messages_pt_BR.properties` ("RESTAURAR"). The `ConfigController` reads the `ResourceBundle` it already holds to get the
correct word at runtime.

---

## Decision 8: Insufficient Space Detection

**Decision**: Before creating the ZIP, calculate the total size of files to backup (`database/*.mv.db` size + total size
of all proofs files). Compare against `Files.getFileStore(targetDrivePath).getUsableSpace()`. If insufficient, throw a
domain-level `InsufficientSpaceException` (extends `BusinessException`).

---

## Decision 9: Feature Package Placement

**Decision**: Create a new `backup` top-level feature package following the project's Package-by-Feature architecture.
The `BackupRestoreController` (in `backup/ui/controller/`) builds its section Node, which `ConfigController` embeds into
the config-view VBox after calling `backupRestoreController.buildSection()`.

**Rationale**: The backup domain (drive detection, ZIP creation, restore logic) is substantial enough to warrant its own
feature package. The UI section lives in the backup package, keeping the config package focused on landlord
configuration.
