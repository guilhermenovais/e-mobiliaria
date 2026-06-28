# Feature Specification: Backup and Restore

**Feature Branch**: `005-backup-restore`  
**Created**: 2026-06-28  
**Status**: Draft  
**Input**: User description: "I want to create a backup/restore feature for this app."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Backup App Data to USB Drive (Priority: P1)

The user navigates to the Settings page and initiates a backup. The system detects connected USB drives and guides the
user through confirming the target drive. The backup is created as a timestamped ZIP file on the USB drive, verified for
integrity, and the user is informed of success or any failure.

**Why this priority**: Backup is the primary protection against data loss and must work reliably before restore is
valuable. It is the most frequently used of the two operations.

**Independent Test**: Can be fully tested by clicking "Backup" in Settings and verifying that a valid ZIP file appears
on the connected USB drive in the `e-mobiliaria-backup` folder.

**Acceptance Scenarios**:

1. **Given** no USB drive is connected, **When** the user clicks "Backup", **Then** the app displays a clear error
   message stating no removable drive was found.
2. **Given** exactly one USB drive is connected, **When** the user clicks "Backup", **Then** the app shows a
   confirmation prompt displaying the drive's name and identifier (e.g., "Backup to Kingston (E:)?").
3. **Given** exactly one USB drive and the user confirms, **When** the backup completes and is verified successfully, *
   *Then** a success message is shown and a timestamped ZIP file (e.g., `backup-2026-06-28-1530.zip`) is present in the
   `e-mobiliaria-backup` folder on the drive's root.
4. **Given** more than one USB drive is connected, **When** the user clicks "Backup", **Then** a drive selection dialog
   is shown listing all detected removable drives, and the user can pick one to proceed.
5. **Given** the target USB drive has insufficient free space, **When** a backup is attempted, **Then** the app displays
   a specific message informing the user of insufficient space and instructing them to manually delete old backups.
6. **Given** any other unexpected error occurs during backup, **When** the process fails, **Then** the app clearly
   informs the user of the error.

---

### User Story 2 - Restore App Data from USB Drive (Priority: P2)

The user navigates to the Settings page and initiates a restore. The system detects connected USB drives, displays all
available backup files with the most recent pre-selected, and requires the user to type a locale-specific confirmation
word before proceeding.

**Why this priority**: Restore is only useful after backups exist and is inherently a less frequent, higher-risk
operation. It depends on the backup story being functional.

**Independent Test**: Can be fully tested by clicking "Restore" in Settings, selecting a backup from the list, typing
the confirmation word, and verifying the app's data reflects the state captured in that backup file.

**Acceptance Scenarios**:

1. **Given** no USB drive is connected, **When** the user clicks "Restore", **Then** the app displays a clear error
   message stating no removable drive was found.
2. **Given** exactly one USB drive is connected with backups present, **When** the user clicks "Restore", **Then** the
   app shows a list of all available backups found on that drive, with the most recent one pre-selected, and a "Restore"
   button below the list.
3. **Given** the user has selected a backup and clicks "Restore", **When** the confirmation dialog appears, **Then** it
   shows the message "Restoring will replace current data. Type '[CONFIRMATION_WORD]' to complete this operation" with a
   text field and Restore/Cancel buttons (where the confirmation word is locale-specific: "RESTORE" in English, "
   RESTAURAR" in Portuguese).
4. **Given** the confirmation dialog is shown, **When** the user types the exact locale-specific confirmation word and
   clicks "Restore", **Then** the restore operation proceeds; upon completion the application automatically closes and
   restarts so all data is reloaded from the restored state.
5. **Given** the confirmation dialog is shown, **When** the user types an incorrect word and clicks "Restore", **Then**
   the restore operation does not proceed and the user is informed the word does not match.
6. **Given** the confirmation dialog is shown, **When** the user clicks "Cancel", **Then** no restore is performed and
   the user returns to the previous screen.

---

### Edge Cases

- What happens when the USB drive contains an `e-mobiliaria-backup` folder but no valid backup files? The app should
  inform the user that no backups were found on the drive.
- What happens if the USB drive is disconnected during an active backup or restore? The app should detect the
  interruption and display a clear error message; no partial data should be committed on restore.
- What happens if the backup ZIP file fails integrity verification after creation? The app must report a failure message
  and leave the corrupted file on the drive for the user to inspect or delete manually.
- What happens if more than one USB drive is connected when initiating a restore? The app shows the same drive selection
  dialog used during backup, allowing the user to pick which drive to restore from.
- What happens if the `e-mobiliaria-backup` folder does not exist on the selected restore drive? The app informs the
  user that no backups were found on the selected drive.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: The Settings page MUST include a dedicated "Backup/Restore" section containing exactly two actions: Backup
  and Restore.
- **FR-002**: When the user initiates a Backup or Restore, the system MUST automatically detect all removable USB drives
  currently connected to the computer.
- **FR-003**: If no removable USB drive is detected, the system MUST display a clear, user-friendly error message.
- **FR-004**: If exactly one USB drive is detected when initiating a backup, the system MUST display a confirmation
  prompt that identifies the drive by name and system identifier before proceeding.
- **FR-005**: If more than one USB drive is detected, the system MUST display a drive selection dialog listing all
  detected removable drives, allowing the user to select one before proceeding.
- **FR-006**: Backups MUST be stored inside a folder named `e-mobiliaria-backup` located at the root of the selected USB
  drive; the folder MUST be created automatically if it does not exist.
- **FR-007**: Each backup MUST be saved as a ZIP file with a name that includes the creation timestamp (e.g.,
  `backup-2026-06-28-1530.zip`).
- **FR-008**: The backup ZIP file MUST include all application data required to fully restore the application to the
  state at backup time: specifically the H2 database file (`%LOCALAPPDATA%\e-mobiliaria\database\emobiliaria.mv.db`) and
  all payment proof files stored under the proofs directory (`%LOCALAPPDATA%\e-mobiliaria\proofs\`).
- **FR-009**: After saving the backup file, the system MUST verify its integrity by opening the ZIP and reading all
  entries in full; if any entry throws a read error the backup is considered failed.
- **FR-010**: Upon successful backup, the system MUST display a success message to the user.
- **FR-011**: If the target USB drive has insufficient free space to store the new backup, the system MUST display a
  specific error message informing the user of the space issue and instructing them that old backups must be removed
  manually.
- **FR-012**: For all other backup errors, the system MUST display a descriptive error message clearly indicating what
  went wrong.
- **FR-013**: When initiating a restore with exactly one USB drive detected, the system MUST display a list of all
  backup files found in the `e-mobiliaria-backup` folder on that drive, ordered with the most recent backup
  pre-selected.
- **FR-014**: The restore selection screen MUST include a "Restore" button positioned below the backup file list.
- **FR-015**: When the user clicks "Restore" on the selection screen, the system MUST display a confirmation dialog
  that: (a) warns the user that the operation will replace all current data, (b) requires the user to type a
  locale-specific confirmation word ("RESTORE" in English, "RESTAURAR" in Portuguese), (c) provides a text input field,
  and (d) offers "Restore" and "Cancel" buttons.
- **FR-016**: The restore operation MUST proceed only when the user types the exact locale-specific confirmation word
  and clicks "Restore".
- **FR-017**: If the user types an incorrect confirmation word, the system MUST prevent the restore and notify the user
  that the word does not match.
- **FR-018**: If the user clicks "Cancel" on the confirmation dialog, the system MUST abort the restore and return the
  user to the previous screen without modifying any data.
- **FR-021**: While a restore operation is in progress, the system MUST display a non-closeable modal dialog showing a "
  Restoring…" message with a spinning progress indicator; no other user interaction with the application is possible
  during this time.
- **FR-022**: While a backup operation is in progress, the system MUST display a non-closeable modal dialog showing a "
  Backing up…" message with a spinning progress indicator; no other user interaction with the application is possible
  during this time.
- **FR-019**: Old backup files on USB drives MUST NOT be automatically deleted by the application; management of old
  backups is the user's responsibility.
- **FR-020**: After a successful restore, the application MUST automatically close and relaunch itself so that all
  in-memory state and database connections are reloaded from the restored data.

### Key Entities

- **Backup File**: A timestamped ZIP archive stored in `e-mobiliaria-backup` on a USB drive's root, containing a
  complete snapshot of all application data.
- **Removable Drive**: A USB storage device detected by the operating system as removable at the time the Backup or
  Restore action is initiated.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Users can complete a full backup operation — from clicking "Backup" to seeing a success confirmation — in
  under 2 minutes for a typical dataset.
- **SC-002**: 100% of backup files produced are valid and can be used to fully restore the application to the state at
  backup time.
- **SC-003**: Users are never left without feedback; every error scenario (no drive, insufficient space, integrity
  failure, disconnection) produces a specific and actionable message.
- **SC-004**: Users can complete a full restore — from clicking "Restore" to the application reflecting the restored
  state — without requiring any technical knowledge beyond following on-screen prompts.
- **SC-005**: No accidental restores occur; the locale-specific typed confirmation word is required and validated before
  any data replacement takes place.

## Clarifications

### Session 2026-06-28

- Q: After a successful restore completes, how should the application behave? → A: Automatically close and restart the
  app after a successful restore.
- Q: What specific files/paths constitute "all application data" for the backup? → A: H2 database file and the payment
  proof files.
- Q: How should backup integrity be verified after creation? → A: Open the ZIP and read all entries in full; fail if any
  entry throws an error.
- Q: What UI feedback is shown during the restore operation? → A: Non-closeable modal dialog showing "Restoring…" with a
  spinning indicator.
- Q: What UI feedback is shown during the backup operation? → A: Non-closeable modal dialog showing "Backing up…" with a
  spinning indicator.

## Assumptions

- The application runs on a single-user Windows desktop; detecting removable USB drives uses the operating system's
  native drive enumeration.
- "All application data" means the H2 database file (`%LOCALAPPDATA%\e-mobiliaria\database\emobiliaria.mv.db`) and all
  payment proof image files stored under `%LOCALAPPDATA%\e-mobiliaria\proofs\`; no external resources such as OS
  settings or log files are included.
- When multiple USB drives are connected during restore initiation, the same drive selection dialog used for backup is
  shown.
- If the `e-mobiliaria-backup` folder is absent on the selected drive during restore, the app treats this as "no backups
  found" and informs the user accordingly.
- Backup files are not encrypted; the backup is intended for personal use by the same user on the same or equivalent
  machine.
- The application's locale (language setting) is already accessible to determine the correct confirmation word for the
  restore dialog; no new locale infrastructure is required.
- The app does not set a maximum number of backups; all files in the `e-mobiliaria-backup` folder are listed during
  restore.
