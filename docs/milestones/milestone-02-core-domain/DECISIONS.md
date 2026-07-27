# Milestone 2 — Decisions

## TASK-003: Treatment CRUD Completion

### isArchived vs isActive for procedure types
- **Decision:** Added `isArchived` column alongside existing `isActive` column
- **Rationale:** `isActive` controls catalog visibility (active vs inactive treatments), while `isArchived` controls soft-delete (hidden from all views). Matches Patient/Doctor pattern. `listProcedureTypes()` filters archived by default.

### Standalone treatment CRUD
- **Decision:** Added standalone `updateTreatmentStatus()`, `updateTreatmentNotes()`, and `deleteTreatment()` methods instead of only relying on `syncTreatmentForAppointment()`
- **Rationale:** Users need to update treatment status and delete treatments independently of the parent appointment. `syncTreatmentForAppointment()` is private and tightly coupled to appointment CRUD.

### Treatment delete unlinks from appointment
- **Decision:** `deleteTreatment()` removes the `PerformedActionsTable` row AND nullifies `procedureTypeId` on the associated appointment
- **Rationale:** Prevents orphaned references; the appointment still exists but without a treatment link.

### Referential integrity on hard delete
- **Decision:** `hardDeleteProcedureType()` checks for existing treatments and appointments before allowing deletion
- **Rationale:** Prevents data corruption; users should archive instead of delete if references exist.

### Unified form dialog for create/edit
- **Decision:** Single `ProcedureTypeFormDialog` used for both registration and editing (matching Patient/Doctor pattern)
- **Rationale:** Eliminates form duplication; initial values differ but form fields are identical.

### Treatment status edit via dropdown
- **Decision:** `TreatmentStatusEditDialog` uses `AppDropdownField` for status selection
- **Rationale:** Consistent with existing status selection patterns in appointment and treatment registration dialogs.

### In-memory search for treatments
- **Decision:** `searchTreatments()` uses in-memory filtering on the full treatment list rather than DB-level search
- **Rationale:** The `PerformedActionsTable` doesn't have a direct join for patient/doctor names in a single query; the existing `listTreatmentsInternal()` already loads all data. DB-level search would require a complex multi-table JOIN. Performance is acceptable for typical clinic volumes (<5000 treatments).

## TASK-001: Patient Edit

### Edit dialog placement
- **Decision:** Place `PatientEditDialog` as a private function in `PatientDetailWindow.kt`
- **Rationale:** The edit dialog is only used from PatientDetailWindow. Placing it inline keeps the dialog close to its caller and avoids creating a separate file for a single-use composable.
- **Alternative considered:** Creating a dedicated `PatientDetailDialogs.kt` file — rejected because the dialog is specific to the detail window.

### Duplicate detection
- **Decision:** Case-insensitive first+last name check, excluding current patient ID
- **Rationale:** Prevents accidentally creating duplicate entries when editing a patient's name to match an existing one
- **Implementation:** `findPatientByFullName()` in repository using `lowerCase()` SQL function

### Date parsing fallback
- **Decision:** On parse failure of existing `dateOfBirth` string, default to 25 years ago
- **Rationale:** Handles edge cases where the stored date format might be unexpected without crashing the dialog

## TASK-002: Doctor Management Module

### Schema extension
- **Decision:** Add new columns to existing `DoctorsTable` rather than creating a new table for extended info
- **Rationale:** Keeps the doctor data in a single table (simple queried, no JOIN needed), matching the Patient module pattern
- **All new columns nullable with defaults** to avoid breaking existing seed data

### Soft delete via `isArchived`
- **Decision:** Matching the Patient module, use `isArchived` bool for soft delete
- **Rationale:** Consistency across modules; `listDoctors()` filters archived by default
- **`loadDoctorDirectory()` also filters archived** so the directory only shows active doctors

### Working schedule storage
- **Decision:** Store `workingDays` and `workingHours` as simple strings
- **Rationale:** Simple implementation for v1; future iterations can parse into structured types
- **Format:** Days as comma-separated (e.g., "LUN,MAR,MIE,JUE,VIE"), hours as "HH:MM-HH:MM"

### Form Dialog reuse
- **Decision:** Both registration and edit use the same `DoctorFormDialog` private composable
- **Rationale:** Eliminates duplication; the form is the same in both cases (matching Patient pattern)

### Actions menu placement
- **Decision:** Archive/delete options in the "Más acciones" dropdown (matching Patient module)
- **Rationale:** Consistency and visual cleanliness; destructive actions are hidden behind a menu

### Duplicate detection
- **Decision:** Both name and license number checked
- **Rationale:** Name duplicates prevent confusion; license number duplication is a regulatory requirement
- **License check uses `findDoctorByLicense()`** with optional excludeId for edit scenarios

### Vacation vs. isActive
- **Decision:** Existing convention preserved — vacation stored as `office_room = "VACATION"`, not via `isActive`
- **Rationale:** Backward compatibility with existing data and `DoctorListStatus.VACATION` enum

### Consultory FK
- **Decision:** `consultoryId` references existing `ConsultoriesTable`
- **Rationale:** Enables future consultory management without schema changes; FK with SET NULL on delete
