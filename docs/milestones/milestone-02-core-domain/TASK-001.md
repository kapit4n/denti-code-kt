# TASK-001: Patient CRUD Completion

## Objective

Transform the Patient module into a production-ready CRUD module with full database schema, repository methods, UI dialogs, and validation.

## Changes

### Database (`Tables.kt`)
- Added `documentNumber`, `isArchived`, `updatedAtEpochMs` columns to `PatientsTable`
- Schema auto-migration via `SchemaUtils.createMissingTablesAndColumns()`

### Models (`Models.kt`)
- `Patient` data class now includes `documentNumber`, `gender`, `address`, `isArchived`, `updatedAtEpochMs`, computed `age` property
- `PatientRegistrationRequest` and `PatientUpdateRequest` include `documentNumber`

### Repository (`DentiRepository.kt`)
- **Updated** `loadPatientDirectory()` — filters out archived patients, maps `documentNumber`
- **Updated** `registerPatient()` / `updatePatient()` — with new columns
- **Added** `archivePatient(patientId)` — soft delete via `isArchived = true`
- **Added** `restorePatient(patientId)` — restore via `isArchived = false`
- **Added** `hardDeletePatient(patientId)` — permanent delete
- **Added** `findPatientById(patientId)` — lookup by ID
- **Added** `findPatientByFullName()` — duplicate name detection with optional excludeId
- **Added** `searchPatients(query)` — DB-level search by name, phone, email, documentNumber
- **Added** `patientFromRow()` private mapper for consistent row mapping
- **Fixed** fake pending balance set to `0.0` with TODO

### UI Dialogs
- `ClientRegistrationDialog` in `PatientsScreen.kt` — added `documentNumber` field
- `PatientEditDialog` in `PatientDetailWindow.kt` — pre-filled form with all fields, duplicate name check, loading/success/error states
- `PatientDeleteDialog` in `PatientDetailWindow.kt` — confirmation dialog with permanent delete, auto-closes on success

### Quick Actions
- **Added** "Archivar paciente" / "Restaurar paciente" in More Actions dropdown
- **Added** "Eliminar paciente" in More Actions dropdown
- Archive/restore triggers `refreshNonce` to reload detail data
- Hard delete closes the detail window after success

### Search & Filters
- `buildPatientsUiState()` now searches by `documentNumber` in addition to name, phone, email
- Patient table row shows document number as secondary info: `Doc: {documentNumber}`

### Duplicate Detection
- Registration: duplicate name check before `registerPatient()`
- Edit: duplicate name check before `updatePatient()`, excludes current patient ID

## Files Modified

| File | Change |
|------|--------|
| `data/Tables.kt` | Added 3 columns to `PatientsTable` |
| `data/Models.kt` | Extended `Patient`, `PatientRegistrationRequest`, `PatientUpdateRequest` |
| `data/DentiRepository.kt` | 6 new methods + 3 updated |
| `ui/PatientsScreen.kt` | Added `documentNumber` field + duplicate detection |
| `ui/PatientDetailWindow.kt` | Added delete dialog + archive/restore wiring |
| `ui/patientdetail/ModernPatientDetailContent.kt` | Added archive/delete callbacks |
| `ui/patientdetail/PatientDetailComponents.kt` | Archive/restore/delete in QuickActionsFooter |
| `ui/patients/PatientsUiModels.kt` | Search by documentNumber |
| `ui/patients/PatientListComponents.kt` | Document number display in table rows |

## Build Status

✅ BUILD SUCCESSFUL (0 errors)

## Acceptance Criteria

- ✓ Edit patient dialog opens from detail view Quick Actions
- ✓ Pre-filled with current patient data
- ✓ Validation: required fields (firstName, lastName, contactPhone)
- ✓ Duplicate detection: alerts if same name exists
- ✓ Save persists to database
- ✓ UI refreshes after save (via refreshNonce)
- ✓ Success/error feedback via snackbar
- ✓ Archive/restore patient from detail view
- ✓ Permanent delete with confirmation dialog
- ✓ Search by document number
- ✓ Document number shown in patient list
