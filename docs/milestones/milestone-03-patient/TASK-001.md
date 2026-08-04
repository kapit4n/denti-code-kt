# TASK-001: Patient Clinical Workspace

## Objective

Transform the patient detail view into a complete clinical workspace organized by tabs: Resumen, Historial clínico, Cronología, Citas, Pagos, Documentos, Notas, Recetas y Seguimientos. Add full data layer (tables, models, repository, demo seed) and CRUD dialogs for medical history, dental history, documents, notes, prescriptions and follow-ups.

## Changes

### Database (`Tables.kt`)
- **Added** `PatientMedicalHistoryTable` — `patientId` (FK), `recordType`, `description`, `recordedAt` (ISO date), `doctorId` (FK, nullable), `isActive`, `notes`, `createdAtEpochMs`
- **Added** `PatientDentalHistoryTable` — `patientId` (FK), `toothNumber`, `toothQuadrant`, `diagnosis`, `treatmentPerformed`, `procedureTypeId` (FK, nullable), `recordedAt`, `doctorId` (FK, nullable), `notes`, `createdAtEpochMs`
- **Added** `PatientDocumentsTable` — `patientId` (FK), `title`, `category`, `fileName`, `filePath`, `mimeType`, `fileSize`, `notes`, `uploadedAtEpochMs`
- **Added** `PatientNotesTable` — `patientId` (FK), `body`, `authorLabel`, `isPinned`, `createdAtEpochMs`
- **Added** `PrescriptionsTable` — `patientId` (FK), `medicine`, `dosage`, `frequency`, `instructions`, `prescribedAt`, `doctorId` (FK, nullable), `appointmentId` (FK, nullable), `status`, `createdAtEpochMs`
- **Added** `FollowUpsTable` — `patientId` (FK), `dueDate`, `notes`, `status`, `appointmentId` (FK, nullable), `createdAtEpochMs`
- Foreign keys reference `PatientsTable` (CASCADE), `DoctorsTable`/`ProcedureTypesTable`/`AppointmentsTable` (SET NULL)
- Registered all 6 tables in `Database.kt` via `SchemaUtils.createMissingTablesAndColumns()`

### Models (`Models.kt`)
- **Added** `MedicalRecordType` enum (ALLERGY/CONDITION/SURGERY/MEDICATION) with `.labelEs` + `medicalRecordTypeOptions()`
- **Added** `DocumentCategory` enum (RADIOGRAPH/PHOTO/PDF/CONSENT/TREATMENT_DOC/OTHER) with `.labelEs` + `documentCategoryOptions()`
- **Added** `PrescriptionStatus` (ACTIVE/COMPLETED/CANCELLED) and `FollowUpStatus` (PENDING/COMPLETED/CANCELLED) with `.labelEs` + `*Options()`
- **Added** row models `PatientMedicalRecord`, `PatientDentalRecord`, `PatientDocument`, `PatientNote`, `Prescription`, `FollowUp`
- **Added** register/update request classes (`MedicalRecordRegisterRequest`, `MedicalRecordUpdateRequest`, `DentalRecordRegisterRequest`, `DentalRecordUpdateRequest`, `PatientDocumentRegisterRequest`, `PatientNoteRegisterRequest`, `PrescriptionRegisterRequest`, `FollowUpRegisterRequest`)
- **Added** `PatientClinicalProfile` aggregate — one repository call loads the full clinical file

### Repository (`DentiRepository.kt`)
- **Added** `loadPatientClinicalProfile(patientId)` — aggregate loader
- **Added** medical history: `listMedicalHistoryForPatient`, `registerMedicalRecord`, `updateMedicalRecord`, `deleteMedicalRecord`
- **Added** dental history: `listDentalHistoryForPatient`, `registerDentalRecord`, `updateDentalRecord`, `deleteDentalRecord`
- **Added** documents: `listDocumentsForPatient`, `registerPatientDocument`, `deletePatientDocument`
- **Added** notes: `listPatientNotes`, `addPatientNote`, `togglePinPatientNote`, `deletePatientNote`
- **Added** prescriptions: `listPrescriptionsForPatient`, `registerPrescription`, `updatePrescriptionStatus`, `deletePrescription`
- **Added** follow-ups: `listFollowUpsForPatient`, `registerFollowUp`, `updateFollowUpStatus`, `deleteFollowUp`
- **Added** private helpers `doctorNameById`, `procedureNameById`, `list*Internal` mappers

### Seeder
- **Created** `ClinicalWorkspaceSeeder.kt` — deterministic demo data (`Random(777)`) with medical/dental records, notes, prescriptions, follow-ups and document metadata linked to seeded patients/doctors/procedures
- **Updated** `DemoDataSeeder.kt` — calls `ClinicalWorkspaceSeeder.seed(config, patients, doctors, procedures)`

### UI Models (`PatientClinicalUiModels.kt`)
- **Added** `PatientWorkspaceTab` enum (9 tabs with Spanish labels)
- **Added** UI models + `toUi()` mappers for all 6 clinical entities (with colors, labels, file size, follow-up due state)
- **Added** `PatientTimelineEntry`/`TimelineEntryKind` + `buildPatientTimeline(...)` — merges appointments, treatments, payments and all clinical entries sorted by timestamp
- **Added** `PatientWorkspaceUiState` aggregate, `buildPatientWorkspaceUiState(...)`, `ClinicalSummaryCounts`/`buildClinicalSummaryCounts(...)`

### UI Components (`PatientClinicalComponents.kt`)
- **Added** `WorkspaceTabBar` — horizontally scrollable pill tabs with count badges
- **Added** `ClinicalSection` — reusable card shell with title + action button
- **Added** `ClinicalHistoryPanel` (medical + dental sections with summary chips), `ClinicalSummaryPanel`, `RecordActionsMenu`
- **Added** `TimelinePanel` with per-kind icons and "Ver" jump buttons
- **Added** `DocumentsPanel`, `NotesPanel` (pinned-first), `PrescriptionsPanel` (status menu), `FollowUpsPanel` (due-state badges + status menu)

### UI Dialogs (`PatientClinicalDialogs.kt`)
- **Added** `MedicalRecordDialog` (create/edit), `DentalRecordDialog` (create/edit), `PatientNoteDialog`, `PrescriptionDialog`, `FollowUpDialog` (links to appointments), `PatientDocumentDialog` (metadata; file picker deferred)
- **Added** `ClinicalDeleteKind` / `DeleteClinicalTarget` / `DeleteClinicalConfirmDialog` — single confirmation dialog for all 6 entity types

### Window (`PatientDetailWindow.kt`)
- **Added** clinical profile loading in the existing `LaunchedEffect`
- **Built** `PatientWorkspaceUiState` (KPIs, payments summary, timeline) from repo data
- **Wired** all panels' callbacks to repository operations with `refreshNonce` + snackbar feedback
- **Wired** `ModernPatientDetailContent` to the new workspace state and tab-driven layout

### Content (`ModernPatientDetailContent.kt`)
- **Rewrote** as a tab-driven workspace: header + `WorkspaceTabBar` + per-tab panel + quick actions footer
- `focusSection = CLINICAL_HISTORY` now selects the Historial clínico tab directly
- Overview tab shows `ClinicalSummaryPanel` + 6-entry timeline preview

## Files Modified

| File | Change |
|------|--------|
| `data/Tables.kt` | Added 6 clinical tables |
| `data/Database.kt` | Registered new tables in migration |
| `data/Models.kt` | Added 4 enums + 6 row models + 8 request classes + profile aggregate |
| `data/DentiRepository.kt` | Added 25 clinical methods + 2 helpers |
| `data/seeders/ClinicalWorkspaceSeeder.kt` | New seeder (demo clinical data) |
| `data/seeders/DemoDataSeeder.kt` | Registered clinical seeder |
| `ui/patientdetail/PatientClinicalUiModels.kt` | New file (tabs, UI models, timeline, state) |
| `ui/patientdetail/PatientClinicalComponents.kt` | New file (tabs, panels, cards, menus) |
| `ui/patientdetail/PatientClinicalDialogs.kt` | New file (6 entity dialogs + delete confirm) |
| `ui/patientdetail/ModernPatientDetailContent.kt` | Rewritten as tab-driven workspace |
| `ui/PatientDetailWindow.kt` | Clinical profile loading + full action wiring |

## Build Status

✅ BUILD SUCCESSFUL (0 errors)

## Acceptance Criteria

- ✓ Patient detail opens as a tabbed clinical workspace
- ✓ Medical and dental history CRUD works and persists
- ✓ Documents, notes, prescriptions and follow-ups are manageable
- ✓ Timeline merges all entity types, sorted by date, with jump-to-section
- ✓ Follow-ups show due state (overdue/today/upcoming/done)
- ✓ Prescriptions/follow-ups support inline status changes
- ✓ Notes support pinning
- ✓ Delete flows use confirmation dialogs with busy state
- ✓ All actions refresh the UI and show snackbar feedback
- ✓ Demo data seeds a realistic clinical file per patient
- ✓ Full build passes

## Next Task

TASK-002 in `docs/roadmap/NEXT_TASK.md` — follow-up documentation kept in this milestone folder.
