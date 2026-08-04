# Milestone 3 — Changelog

All notable changes to the Patient Workspace milestone.

## TASK-005 — Clinical Workspace Review & Polish

### Refactoring (all `REFACTORING.md` items done)
- **`PatientDetailWindow.kt` (1299 → 116 lines)** — now pure orchestration. State/actions extracted to new `patientdetail/PatientDetailWindowActions.kt` (472), submit/export extensions to `PatientDetailSubmitActions.kt` (424), and all 14 dialog renderings (incl. `PatientEditDialog`/`PatientDeleteDialog`) to `PatientDetailDialogsHost.kt` (458).
- **`PatientClinicalComponents.kt` (~1275 → deleted)** — split into 9 files: `ClinicalShared.kt`, `ClinicalSummaryPanel.kt`, `ClinicalHistoryPanel.kt`, `ClinicalTimelinePanel.kt`, `ClinicalDocumentsPanel.kt`, `ClinicalNotesPanel.kt`, `ClinicalPrescriptionsPanel.kt`, `ClinicalFollowUpsPanel.kt`, `ClinicalPlansPanel.kt`. `ClinicalSummaryChip` → `internal`.
- **`PatientClinicalDialogs.kt` (~910 → deleted)** — split into `ClinicalDeleteDialog.kt`, `ClinicalMedicalDialogs.kt`, `ClinicalPrescriptionsDialogs.kt`, `ClinicalDocumentsDialogs.kt`, `ClinicalPlansDialogs.kt`.
- Largest patient-workspace file is now `PatientDetailComponents.kt` (~750).

### Polish pass
- **Currency consistency** — `Bs` everywhere in the workspace: header "Saldo pendiente" KPI, payment summary metrics, payment rows (`PatientDetailUiModels.kt`), timeline amounts (`PatientClinicalUiModels.kt`).
- **Dead code removed** — `TreatmentsPanel` (no call sites) and its unused imports.
- **No-op actions removed** — appointment cards no longer show a dead "Ver detalle" menu; `DetailActionMenuButton` primary action is now optional (payments card offers only "Ver recibo").

### Build
- ✅ BUILD SUCCESSFUL (0 errors) — after the refactor and after the polish edits; app smoke-run OK.

## TASK-004 — Physical Document File Management

### Storage layer (new)
- **`export/DocumentStore.kt`** (new) — physical store under `~/.denti-code-kt/documents/<patientId>/`; `save()` copies with a timestamp prefix, `delete()` only touches files inside the store, MIME guessing via `probeContentType` + extension table, `extensionLabel()` for badges
- **`ExportService.pickOpenFile()`** — native `FileDialog` LOAD on the Swing EDT

### Document dialog
- **`PatientClinicalDialogs.kt`** — `PatientDocumentDialog(patientId, …)`: "Seleccionar archivo" copies the file to the store and auto-fills title/fileName/size/MIME; attached-vs-metadata-only label; cleanup of pending files on cancel; removed the "fase posterior" placeholder text

### Documentos tab
- **`PatientDetailWindow.kt`** — `onOpenDocument` opens the real file (`Desktop.open`) with clear missing-file/no-file messages; document delete confirmation now carries `filePath` and removes the physical file before the metadata row
- **`PatientClinicalComponents.kt`** — `DocumentCard` extension-type badge + "Solo metadatos" badge

### Refactoring
- **`REFACTORING.md` item 4 done** — normalized fully-qualified `PatientDetailPaymentUi` state type in `PatientDetailWindow.kt`
- `DeleteClinicalTarget` gained optional `filePath`

### Build
- ✅ BUILD SUCCESSFUL (0 errors)

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
