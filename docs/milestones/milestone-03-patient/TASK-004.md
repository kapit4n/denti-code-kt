# TASK-004: Physical Document File Management

## Objective

Give patient documents real physical file management: a native file picker that copies the selected file into a local app-managed store, opening real files from the Documentos tab, physical file deletion when a document is deleted, and richer document list badges (type, metadata-only marker). Also lands the first refactoring item from `REFACTORING.md` (normalized receipt type).

## Changes

### Storage layer (new)
- **`export/DocumentStore.kt`** (new) — physical store for patient documents:
  - Files are copied to `~/.denti-code-kt/documents/<patientId>/<epochMs>_<name>` (100 % offline, collision-safe)
  - `save(patientId, file)` copies and returns the stored file
  - `delete(path)` only deletes files inside the store (guards against arbitrary paths)
  - `guessMimeType()` via `Files.probeContentType` + extension fallback table, `extensionLabel()` for list badges
- **`ExportService.pickOpenFile()`** — native `FileDialog` LOAD on the Swing EDT, mirrors `pickSaveFile`

### Patient document dialog
- **`PatientClinicalDialogs.kt`** — `PatientDocumentDialog` now takes `patientId`:
  - New "Seleccionar archivo" button (native picker) that copies the file to the store and auto-fills title, fileName, fileSize and MIME type
  - Shows whether a file is attached (with size) or metadata-only; local error message if the copy fails
  - Pending copied files are cleaned up if the user cancels without registering
  - Replaced the "carga física en una fase posterior" placeholder text

### Documentos tab (real open + delete)
- **`PatientDetailWindow.kt`**
  - `onOpenDocument` now opens the physical file with the OS default viewer (`Desktop.open`), warns when the file is missing or the document has no file attached
  - Document delete confirmation includes `filePath`; confirming a document delete also removes the physical file (`DocumentStore.delete`) before removing the metadata row
- **`PatientClinicalComponents.kt`** — `DocumentCard` now shows an extension type badge and a "Solo metadatos" badge when no physical file is linked

### Refactoring (from `REFACTORING.md`)
- Normalized the fully-qualified `com.denticode.kt.ui.patientdetail.PatientDetailPaymentUi` state type in `PatientDetailWindow.kt` to a plain import
- `DeleteClinicalTarget` gained an optional `filePath` field for the document delete flow

## Files Modified

| File | Change |
|------|--------|
| `export/DocumentStore.kt` | New file (physical store, save/delete/guards, MIME guessing, extension labels) |
| `export/ExportService.kt` | Added `pickOpenFile()` |
| `ui/patientdetail/PatientClinicalDialogs.kt` | `PatientDocumentDialog` file picker + auto metadata + cleanup; `DeleteClinicalTarget.filePath` |
| `ui/PatientDetailWindow.kt` | Real open/delete of physical files; `patientId` passed to dialog; normalized receipt type import |
| `ui/patientdetail/PatientClinicalComponents.kt` | `DocumentCard` type + "Solo metadatos" badges |

## Build Status

✅ BUILD SUCCESSFUL (0 errors) — `./gradlew build` + app smoke-run (60 s, no crash)

## Acceptance Criteria

- ✓ "Subir documento" lets the user pick a real file which is copied to `~/.denti-code-kt/documents/<patientId>/`
- ✓ Title, file name, size and MIME type auto-fill from the selected file (still editable)
- ✓ The Documentos tab shows the extension type badge and marks metadata-only documents
- ✓ "Abrir" opens the physical file with the OS viewer; missing/no-file cases show clear messages
- ✓ Deleting a document removes its physical file from the store (and only files inside the store)
- ✓ Canceling the dialog after picking a file cleans up the copied file
- ✓ Receipt state type normalized (refactoring item 4 of `REFACTORING.md`)
- ✓ Full build passes

## Next Task

TASK-005 in `docs/roadmap/NEXT_TASK.md` — Clinical Workspace Review & Polish; remaining `REFACTORING.md` items (file splits) stay tracked there.
