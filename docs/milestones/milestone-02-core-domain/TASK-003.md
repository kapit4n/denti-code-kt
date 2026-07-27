# TASK-003: Treatment CRUD Completion

## Objective

Transform the Procedure Types catalog and Treatments (performed actions) into fully manageable CRUD modules with edit, delete/archive, search, status management, and duplicate detection.

## Changes

### Database (`Tables.kt`)
- Added `isArchived` column to `ProcedureTypesTable` (default `false`)
- Schema auto-migration via `SchemaUtils.createMissingTablesAndColumns()`

### Models (`Models.kt`)
- Added `ProcedureTypeUpdateRequest` data class (identical fields to `ProcedureTypeRegisterRequest`)
- `ProcedureTypeRow` extended with `isArchived` field (default `false`)

### Repository (`DentiRepository.kt`)

**Procedure Types — 8 new/updated methods:**
- **Updated** `listProcedureTypes()` — now filters out archived (`isArchived = false`), maps `isArchived`
- **Added** `listAllProcedureTypesIncludingArchived()` — for admin views
- **Added** `findProcedureTypeById(id)` — lookup by ID
- **Added** `findProcedureTypeByName(name, excludeId?)` — duplicate name detection
- **Added** `searchProcedureTypes(query)` — DB-level search by name, description, category
- **Added** `updateProcedureType(id, request)` — update procedure type fields
- **Added** `archiveProcedureType(id)` — soft delete via `isArchived = true`
- **Added** `restoreProcedureType(id)` — restore via `isArchived = false`
- **Added** `hardDeleteProcedureType(id)` — permanent delete with referential integrity checks (rejects if treatments/appointments exist)

**Treatments — 4 new methods:**
- **Added** `findTreatmentById(id)` — lookup performed action by ID
- **Added** `searchTreatments(query)` — in-memory search by patient, procedure, doctor, notes
- **Added** `updateTreatmentStatus(id, status)` — standalone status update
- **Added** `updateTreatmentNotes(id, notes)` — standalone notes update
- **Added** `deleteTreatment(id)` — deletes treatment and unlinks from appointment

### UI — ProceduresScreen (`ProceduresScreen.kt`)
- **Added** search field for filtering procedure types by name, description, category
- **Added** `ProcedureTypeCard` — each card now has an actions menu (⋯) with Edit/Archive/Restore/Delete
- **Added** `ProcedureTypeFormDialog` — unified create/edit dialog (reused for both operations)
- **Added** `ProcedureTypeDeleteDialog` — confirmation dialog with permanent delete warning
- **Added** `ProcedureTypeActionsDropdown` — dropdown menu with contextual actions
- **Added** duplicate name detection on create and edit
- Registration dialog now uses `ProcedureTypeFormDialog` (renamed from `RegisterProcedureDialog`)

### UI — Treatment List (`TreatmentListComponents.kt`)
- **Added** `showActionsColumn` parameter to `TreatmentsTable`, `TreatmentsTableHeader`, `TreatmentTableRow`
- **Added** `onStatusChange` and `onDelete` optional callbacks to `TreatmentsTable`
- Actions column shows dropdown menu with "Cambiar estado" and "Eliminar" options
- Backward-compatible: existing callers without action callbacks remain unchanged

### UI — Treatment Status Dialog (new in `ProceduresScreen.kt`)
- `TreatmentStatusEditDialog` — dropdown-based dialog to change treatment status (PLANNED/IN_PROGRESS/COMPLETED/CANCELLED)
- `TreatmentDeleteDialog` — confirmation dialog with patient name and treatment label

### UI — Treatment Delete Flow
- Deletes the `PerformedActionsTable` row and unlinks the procedure type from the associated `AppointmentsTable` row

## Files Modified

| File | Change |
|------|--------|
| `data/Tables.kt` | Added `isArchived` column to `ProcedureTypesTable` |
| `data/Models.kt` | Added `ProcedureTypeUpdateRequest`, `isArchived` to `ProcedureTypeRow` |
| `data/DentiRepository.kt` | 9 new methods + 1 updated (`listProcedureTypes`) |
| `ui/ProceduresScreen.kt` | Full rewrite: search, edit/delete dialogs, archive/restore, treatment status/delete |
| `ui/treatments/TreatmentListComponents.kt` | Added actions column, status change + delete callbacks |

## Build Status

✅ BUILD SUCCESSFUL (0 errors, only pre-existing deprecation warnings)

## Acceptance Criteria

- ✓ Edit procedure type dialog opens from card actions menu
- ✓ Pre-filled with current procedure type data
- ✓ Validation: required field (name), duplicate name check
- ✓ Save persists to database, UI refreshes
- ✓ Archive procedure type (soft delete)
- ✓ Restore archived procedure type
- ✓ Hard delete procedure type with confirmation dialog
- ✓ Referential integrity: cannot delete if treatments/appointments reference it
- ✓ Search procedure types by name, description, category
- ✓ Treatment status change from treatments table
- ✓ Treatment deletion with confirmation
- ✓ Treatment deletion unlinks procedure from appointment
- ✓ Duplicate detection on create and edit
- ✓ Snackbar feedback for all operations
- ✓ Loading/busy states on all async operations

## CRUD Completeness After TASK-003

| Entity | Create | Read | Update | Delete |
|--------|--------|------|--------|--------|
| Procedure Types | ✅ | ✅ | ✅ | ✅ Soft + Hard |
| Treatments | ✅ | ✅ | ✅ Status/Notes | ✅ |
