# Milestone 2 — Changelog

All notable changes to the Core Domain Completion milestone.

## TASK-004 — Treatment Management (categories, filters, sorting, favorites)

### Database (`Tables.kt`)
- **Added** `TreatmentCategoriesTable` — `name`, `icon`, `color`, `sortOrder`, `isActive`, `isArchived`, timestamps
- **Extended** `ProcedureTypesTable` — 7 new columns: `categoryId` (FK), `currency`, `color`, `icon`, `isFavorite`, `notes`, timestamps

### Models (`Models.kt`)
- **Added** `TreatmentCategory`, `CategoryRegisterRequest`, `CategoryUpdateRequest`
- **Extended** `ProcedureTypeRow`, `ProcedureTypeRegisterRequest`, `ProcedureTypeUpdateRequest` with 7 new fields

### Repository (`DentiRepository.kt`)
- **Extracted** `procedureTypeFromRow()` and `categoryFromRow()` DRY helpers
- **Added** 10 category CRUD methods (list, find, register, update, archive, restore, hard delete, name check)
- **Added** `toggleFavoriteProcedureType(id)`
- **Updated** all procedure methods for new columns

### Seeders
- **Created** `TreatmentCategoriesSeeder.kt` — 11 default categories with icons/colors
- **Updated** `ProceduresSeeder.kt` — links categories, sets currency/timestamps
- **Updated** `DemoDataSeeder.kt` — category seeding orchestration

### UI — ProceduresScreen.kt (full rewrite)
- **Added** `SearchAndFiltersBar` — text search, category/status/favorite filters, sort with ASC/DESC
- **Added** `ProcedureTypeCard` — color dot, favorite star, category chip, action dropdown
- **Updated** `ProcedureTypeFormDialog` — category dropdown, color palette, currency, notes, icon
- **Added** `CategoryManagerDialog` — list/create/edit/archive/restore/delete categories
- **Added** `CategoryFormDialog` — name, icon, color palette, sortOrder, isActive
- **Added** `CategoryDeleteDialog` — referential integrity warning

### Utility (`Format.kt`)
- **Added** `parseHexColor()` — Compose Desktop-safe hex color parser

### Build
- ✅ BUILD SUCCESSFUL (0 errors)

## TASK-003 — Treatment CRUD Completion

### Database (`Tables.kt`)
- **Added** `isArchived` column to `ProcedureTypesTable` (default `false`)
- Schema auto-migration via `SchemaUtils.createMissingTablesAndColumns()`

### Models (`Models.kt`)
- **Added** `ProcedureTypeUpdateRequest` data class
- `ProcedureTypeRow` extended with `isArchived` field

### Repository (`DentiRepository.kt`)
- **Updated** `listProcedureTypes()` — filters out archived, maps `isArchived`
- **Added** `listAllProcedureTypesIncludingArchived()` — for admin views
- **Added** `findProcedureTypeById(id)` — lookup by ID
- **Added** `findProcedureTypeByName(name, excludeId?)` — duplicate name detection
- **Added** `searchProcedureTypes(query)` — DB-level search by name, description, category
- **Added** `updateProcedureType(id, request)` — update procedure type fields
- **Added** `archiveProcedureType(id)` — soft delete via `isArchived = true`
- **Added** `restoreProcedureType(id)` — restore via `isArchived = false`
- **Added** `hardDeleteProcedureType(id)` — permanent delete with referential integrity checks
- **Added** `findTreatmentById(id)` — lookup performed action by ID
- **Added** `searchTreatments(query)` — search by patient, procedure, doctor, notes
- **Added** `updateTreatmentStatus(id, status)` — standalone status update
- **Added** `updateTreatmentNotes(id, notes)` — standalone notes update
- **Added** `deleteTreatment(id)` — deletes treatment and unlinks from appointment

### UI — ProceduresScreen
- **Added** search field for filtering procedure types
- **Added** `ProcedureTypeCard` with actions menu (Edit/Archive/Restore/Delete)
- **Added** `ProcedureTypeFormDialog` — unified create/edit dialog
- **Added** `ProcedureTypeDeleteDialog` — confirmation with permanent delete warning
- **Added** `ProcedureTypeActionsDropdown` — contextual actions
- Duplicate name detection on create and edit

### UI — Treatment List (`TreatmentListComponents.kt`)
- **Added** `showActionsColumn`, `onStatusChange`, `onDelete` parameters to `TreatmentsTable`
- Actions column with dropdown menu for status change and delete
- Backward-compatible with existing callers

### UI — Treatment Dialogs (in `ProceduresScreen.kt`)
- `TreatmentStatusEditDialog` — change treatment status via dropdown
- `TreatmentDeleteDialog` — confirmation with patient name and treatment label

### Validation
- Duplicate name detection on create/edit (`findProcedureTypeByName`)
- Referential integrity: cannot hard-delete procedure type if treatments/appointments reference it
- Required field: name

### Build
- ✅ BUILD SUCCESSFUL (0 errors, only pre-existing deprecation warnings)

## TASK-002 — Doctor Management Module

### Database (`Tables.kt`)
- **Added** `address`, `workingDays`, `workingHours`, `consultoryId` (FK → ConsultoriesTable), `notes`, `isArchived`, `createdAtEpochMs`, `updatedAtEpochMs` to `DoctorsTable`
- Schema auto-migration via `SchemaUtils.createMissingTablesAndColumns()`

### Models (`Models.kt`)
- `Doctor` data class: added `address`, `workingDays`, `workingHours`, `consultoryId`, `notes`, `isArchived`, `createdAtEpochMs`, `updatedAtEpochMs`
- `DoctorRegistrationRequest` / `DoctorUpdateRequest`: added same fields

### Repository (`DentiRepository.kt`)
- **Updated** `doctorFromRow()` — maps all new columns
- **Updated** `registerDoctor()` — inserts new columns, `createdAtEpochMs`
- **Updated** `updateDoctor()` — updates new columns, `updatedAtEpochMs`
- **Updated** `loadDoctorDirectory()` — filters out archived doctors (`isArchived = false`)
- **Added** `searchDoctors(query)` — DB-level search by name, email, phone, license, specialization
- **Added** `findDoctorByLicense(license, excludeId?)` — duplicate license detection
- **Added** `findDoctorByFullName(first, last, excludeId?)` — duplicate name detection
- **Added** `archiveDoctor(doctorId)` / `restoreDoctor(doctorId)` — soft-delete/restore
- **Added** `hardDeleteDoctor(doctorId)` — permanent delete
- **Added** `listAllDoctorsIncludingArchived()` — for admin views

### UI List (`DoctorListComponents.kt`)
- `DoctorActionsMenu`: added "Archivar" and "Eliminar" options with colored labels
- `DoctorRow` / `DoctorsTable`: wired archive/delete callbacks through component chain
- `DoctorUiModel`: added `licenseNumber` and `address` fields
- Search filter now includes `licenseNumber`

### UI Dialogs (`DoctorDialogs.kt`)
- `DoctorFormDialog`: added `address`, `workingDays`, `workingHours`, `notes` fields
- `DoctorDeleteDialog`: new confirmation dialog with permanent delete
- Registration/Edit: duplicate name + license checks before save

### UI Detail (`ModernDoctorDetailContent.kt`)
- **Added** `onArchive`, `onDelete`, `isArchived` parameters
- **Added** Address display in profile header
- **Added** Schedule section (working days/hours) when available
- **Added** Notes section when present
- **Added** Archive/Restore and Delete buttons in Actions section

### Screens
- `DoctorsScreen.kt`: wired archive/restore/delete flows with confirmation dialogs
- `DoctorDetailWindow.kt`: wired archive/restore/delete with `refreshNonce` and snackbar feedback

### Validation
- Duplicate name detection on create and edit (`findDoctorByFullName`)
- Duplicate license number detection on create and edit (`findDoctorByLicense`)
- Required fields: firstName, lastName, email
- Business rules preserved: active toggle checks future appointments

### Build
- ✅ BUILD SUCCESSFUL (0 errors, only pre-existing deprecation warnings)

## TASK-001 — Patient CRUD Completion

### Database
- **Added** `documentNumber`, `isArchived`, `updatedAtEpochMs` columns to `PatientsTable`
- Schema auto-migration via `SchemaUtils.createMissingTablesAndColumns()`

### Repository (`DentiRepository.kt`)
- **Added** `archivePatient(patientId)` — sets `isArchived = 1`
- **Added** `restorePatient(patientId)` — sets `isArchived = 0`
- **Added** `hardDeletePatient(patientId)` — permanent delete
- **Added** `findPatientById(patientId)`, `findPatientByFullName()`, `searchPatients(query)`
- **Updated** `loadPatientDirectory()` — filters out archived patients by default, maps `documentNumber`
- **Updated** `registerPatient()` — maps `documentNumber`, `isArchived = false`, epoch timestamps
- **Updated** `updatePatient()` — maps `documentNumber`, updates `updatedAtEpochMs`
- **Fixed** fake pending balance set to `0.0` (TODO: real calculation)
- **Added** `patientFromRow()` private mapper for `Patient` row mapping

### Models (`Models.kt`)
- `Patient` now includes `documentNumber`, `gender`, `address`, `isArchived`, `updatedAtEpochMs`, computed `age`
- `PatientRegistrationRequest` and `PatientUpdateRequest` include `documentNumber`

### Dialogs
- `PatientEditDialog` — pre-filled form with all fields (firstName, lastName, birthDate, documentNumber, phone, email, medicalSummary), duplicate name check, loading/success/error states
- `PatientDeleteDialog` — confirmation dialog with permanent delete, loading state, auto-closes on success
- `ClientRegistrationDialog` — added `documentNumber` field

### Quick Actions Footer
- **Added** "Archivar paciente" / "Restaurar paciente" in More Actions dropdown
- **Added** "Eliminar paciente" in More Actions dropdown
- Archive/restore triggers `refreshNonce` to reload detail data
- Hard delete closes the detail window after success

### Search & Filters
- `buildPatientsUiState()` now searches by `documentNumber` in addition to name, phone, email
- Patient table row shows document number as secondary info: `"Doc: {documentNumber}"`

### Duplicate Detection
- Registration: duplicate name check before `registerPatient()`
- Edit: duplicate name check before `updatePatient()`, excludes current patient ID

## Tasks remaining for TASK-001
- `documentNumber` uniqueness enforcement at DB layer (future)
- Real pending balance calculation in `loadPatientDirectory()`

