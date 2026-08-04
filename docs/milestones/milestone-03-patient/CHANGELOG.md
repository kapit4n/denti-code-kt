# Milestone 3 — Changelog

All notable changes to the Patient Workspace milestone.

## TASK-003 — Receipts and Patient Summary Export

### Export module (new `export/` package)
- **`ExportService.kt`** — clinic identity (Bs/Cochabamba), native `FileDialog` save picker on the Swing EDT, UTF-8 file writing, `Desktop.open`
- **`Receipts.kt`** — `ReceiptData` + `renderReceiptText()` plain-text receipt
- **`PatientSummaryExport.kt`** — `PatientSummaryBundle`, `renderPatientSummaryHtml()`, `renderPatientSummaryText()`, `PaymentExportRow` + `renderPaymentsCsv()`

### Receipts (payments screen)
- **`PaymentReceiptDialog.kt`** (new) — receipt preview with save/cancel
- **`ModernPaymentsContent.kt`** — "Ver recibo" opens the dialog; export toolbar now writes a real CSV file
- **`PaymentListComponents.kt`** — "Ver recibo" action wired; `€` → `Bs` in KPIs/footer
- **`PaymentsUiModels.kt`** — `toReceiptData()` mapper

### Patient summary export (patient detail)
- **`PatientDetailWindow.kt`** — ficha HTML export, resumen TXT export, receipt dialog for Pagos tab
- **`PatientDetailComponents.kt`** — `PaymentItemCard` "Ver recibo"; QuickActionsFooter export/print wired (no longer no-ops)
- **`PatientDetailUiModels.kt`** — `toReceiptData(patientName, patientId)`
- **`ModernPatientDetailContent.kt`** — overview pending balance + export callbacks + receipt wiring

### Overview enhancements
- **`PatientClinicalComponents.kt`** — `ClinicalSummaryPanel` real pending-balance chip + "Exportar ficha" button

### Build
- ✅ BUILD SUCCESSFUL (0 errors)

## TASK-002 — Treatment Plans & Pending Balance

### Database (`Tables.kt`)
- **Added** `TreatmentPlansTable` and `TreatmentPlanPhasesTable` (status + sortOrder + estimatedCost)
- Registered in `Database.kt` migration

### Models (`Models.kt`)
- **Added** `TreatmentPlanStatus` and `TreatmentPlanPhaseStatus` enums
- **Added** 2 row models + 4 request classes

### Repository (`DentiRepository.kt`)
- **Real pending balance** in `loadPatientDirectory` (treatments − payments, floor 0)
- **Currency** `€` → `Bs` in treatments/payments UI and repo labels
- **Added** 12 plan/phase methods incl. `recomputeTreatmentPlanCost` and auto-complete on last phase

### Seeders
- **Created** `TreatmentPlansSeeder.kt` — deterministic demo plans/phases
- **Updated** `DemoDataSeeder.kt`

### UI — Updated
- `PatientClinicalUiModels.kt` — plan tab, UI models, timeline entries, `activePlans`
- `PatientClinicalComponents.kt` — `TreatmentPlanPanel` + summary chip + timeline mappings
- `PatientClinicalDialogs.kt` — plan/phase dialogs + delete kinds
- `ModernPatientDetailContent.kt` — plan tab + 8 callbacks
- `PatientDetailWindow.kt` — plan loading + dialogs + delete dispatch

### Build
- ✅ BUILD SUCCESSFUL (0 errors)

## TASK-001 — Patient Clinical Workspace

### Database (`Tables.kt`)
- **Added** `PatientMedicalHistoryTable`, `PatientDentalHistoryTable`, `PatientDocumentsTable`, `PatientNotesTable`, `PrescriptionsTable`, `FollowUpsTable`
- Registered in `Database.kt` migration

### Models (`Models.kt`)
- **Added** `MedicalRecordType`, `DocumentCategory`, `PrescriptionStatus`, `FollowUpStatus` enums
- **Added** 6 row models + 8 request classes + `PatientClinicalProfile` aggregate

### Repository (`DentiRepository.kt`)
- **Added** `loadPatientClinicalProfile()` and 25 CRUD/status methods for the 6 clinical entities

### Seeders
- **Created** `ClinicalWorkspaceSeeder.kt` — demo medical/dental records, notes, prescriptions, follow-ups, document metadata
- **Updated** `DemoDataSeeder.kt`

### UI — New files
- `PatientClinicalUiModels.kt` — tabs, UI models, timeline builder, workspace state
- `PatientClinicalComponents.kt` — tab bar, panels, cards, menus
- `PatientClinicalDialogs.kt` — 6 entity dialogs + unified delete confirmation

### UI — Updated
- `ModernPatientDetailContent.kt` — rewritten as tab-driven workspace
- `PatientDetailWindow.kt` — clinical profile loading + full action wiring

### Build
- ✅ BUILD SUCCESSFUL (0 errors)
