# TASK-002: Doctor Management Module

## Objective

Complete Doctor CRUD with full validation, search, filters, sorting, delete with confirmation, archive/restore, schedule visualization, and duplicate detection. Reuse every possible pattern from the Patient module (Milestone 1).

## Architecture Decisions

- **Doctor model** extended with new fields rather than breaking existing code; all new fields have defaults (`null`, `false`)
- **Soft delete** via `isArchived` column (matching Patient pattern); `listDoctors()` filters out archived by default
- **Duplicate detection** at repository level via `findDoctorByFullName()` and `findDoctorByLicense()` (matching Patient's `findPatientByFullName()`)
- **Search** implemented both at DB level (`searchDoctors()`) and in-memory filter (`buildDoctorsUiState`)
- **Schedule** stored as simple strings (`workingDays`, `workingHours`) for future structured parsing
- **Consultory** is a FK reference to `ConsultoriesTable` (existing table), allowing future consultory management

## Changes

### Database (`Tables.kt`)
- 8 new columns added to `DoctorsTable`

### Models (`Models.kt`)
- `Doctor` data class: 6 new properties with defaults
- `DoctorRegistrationRequest` / `DoctorUpdateRequest`: 5 new optional fields each

### Repository (`DentiRepository.kt`)
- 6 new methods: `searchDoctors()`, `findDoctorByLicense()`, `findDoctorByFullName()`, `archiveDoctor()`, `restoreDoctor()`, `hardDeleteDoctor()`
- 3 updated methods: `doctorFromRow()`, `registerDoctor()`, `updateDoctor()`, `loadDoctorDirectory()`
- `loadDoctorDirectory()` now filters out archived doctors

### UI Components Reused
- `AppSurfaceDialog`, `AppButton`, `AppOutlinedButton` (from `ui/components/`)
- `AppTextField`, `AppTextArea` (from `ui/components/inputs/`)
- `PatientAvatar` (from `ui/appointments/`)
- `PatientDetailMetricCard` (from `ui/patientdetail/`)
- `PatientsPremiumPalette`, `AppElevations`, `AppShapes`, `AppSpacing`, `AppTypography` (from `ui/theme/`)
- `LoadingIndicator` (from `ui/components/feedback/`)

### New UI Components
- `DoctorDeleteDialog` — confirmation dialog with permanent delete
- `DoctorFormDialog` — extended with address, workingDays, workingHours, notes fields

### Updated UI Components
- `DoctorActionsMenu` — added archive/delete options
- `DoctorRow` / `DoctorsTable` — wired archive/delete callbacks
- `ModernDoctorsContent` — wired archive/delete callbacks
- `ModernDoctorDetailContent` — added archive/restore/delete buttons, schedule section, notes section, address display
- `DoctorDetailWindow` — wired archive/restore/delete with refreshNonce + snackbar

## Files Modified

| File | Change |
|------|--------|
| `data/Tables.kt` | Added 8 columns to `DoctorsTable` |
| `data/Models.kt` | Updated `Doctor`, `DoctorRegistrationRequest`, `DoctorUpdateRequest` |
| `data/DentiRepository.kt` | Updated doctor methods + 6 new methods |
| `ui/doctors/DoctorDialogs.kt` | Extended form fields + `DoctorDeleteDialog` |
| `ui/doctors/DoctorListComponents.kt` | Archive/delete in actions + row wiring |
| `ui/doctors/DoctorsUiModels.kt` | `licenseNumber` + `address` in `DoctorUiModel`, search by license |
| `ui/doctors/ModernDoctorsContent.kt` | Archive/delete callbacks |
| `ui/DoctorsScreen.kt` | Archive/restore/delete flows + duplicate detection |
| `ui/DoctorDetailWindow.kt` | Archive/restore/delete + refresh + messages |
| `ui/doctordetail/ModernDoctorDetailContent.kt` | Archive/delete buttons, schedule, notes, address |

## Build Status

✅ BUILD SUCCESSFUL (0 errors, only pre-existing deprecation warnings)

## Acceptance Criteria

- ✓ Create doctor with full information (including address, schedule, notes)
- ✓ Edit doctor with duplicate name + license detection
- ✓ Delete doctor with confirmation dialog (permanent)
- ✓ Archive/restore doctor (soft delete)
- ✓ Search by name, email, phone, license, specialization
- ✓ Filter by specialty and status
- ✓ Sort by name, appointment count
- ✓ Loading state on initial load
- ✓ Error state with snackbar feedback
- ✓ Empty state when no doctors match filters
- ✓ Schedule visualization (working days/hours) in detail view
- ✓ Address and notes display in detail view
- ✓ Validation: required fields (name, last name, email), duplicate detection
- ✓ Business rules: cannot deactivate doctor with future appointments

## Components Reused from Milestone 1
- `AppSurfaceDialog` (for all dialogs)
- `AppButton` / `AppOutlinedButton`
- `AppTextField` / `AppTextArea`
- `PatientAvatar` (for doctor avatars)
- `PatientDetailMetricCard` (for KPI cards)
- `PatientsPremiumPalette` (color tokens)
- `AppElevations`, `AppShapes`, `AppSpacing`, `AppTypography`
- `LoadingIndicator`

## Business Rules
- `listDoctors()` excludes archived doctors
- `setDoctorActive()` checks for future appointments before deactivating
- `hardDeleteDoctor()` is permanent — no soft delete alternative
- Duplicate names and license numbers are checked before create and edit
- Vacation status stored as `office_room = "VACATION"` (existing convention)

## Future Improvements
- Structured schedule parsing (working days as enum list, hours as time range)
- Consultory management integration
- Doctor availability calendar view
- Experience years based on hire date rather than estimation
- `userId` → doctor linking
- Bulk archive/delete
- Export doctor directory
