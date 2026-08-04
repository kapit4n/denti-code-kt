# TASK-003: Receipts and Patient Summary Export

## Objective

Continue the Patient Clinical Workspace with printable receipts and patient summary export: a receipt preview/save flow for every registered payment, a full clinical + financial patient ficha export, a payments CSV export, and overview summary enhancements (real pending balance chip + export access).

## Changes

### Export module (new package `export/`)
- **Created `ExportService.kt`** — clinic identity from `DemoDataConfig` (`ClinicName Sonrisa`, Cochabamba, Bolivia, Bs), native `FileDialog` save picker running on the Swing EDT (`Dispatchers.Swing`), UTF-8 file writing (mkdirs on parent), `Desktop.open` helper, and `money()` formatter
- **Created `Receipts.kt`** — `ReceiptData` value object + `renderReceiptText()` fixed-width plain-text receipt (clinic header, receipt number, date, patient, detail, method, amount, status, note, closing line)
- **Created `PatientSummaryExport.kt`**
  - `PatientSummaryBundle` — aggregates patient + appointments + treatments + payments + medical/dental history + documents + notes + prescriptions + follow-ups + treatment plans, with `totalBillable` / `totalPaid` / `pendingBalance` computed
  - `renderPatientSummaryHtml()` — self-contained HTML ficha (print-friendly CSS, per-section tables/lists, financial summary)
  - `renderPatientSummaryText()` — condensed plain-text resumen
  - `PaymentExportRow` + `renderPaymentsCsv()` — payments CSV with `;` separator (Excel-es compatible), headers in Spanish

### Receipts (payments screen)
- **`ModernPaymentsContent.kt`** — "Ver recibo" now opens `PaymentReceiptDialog`; "Guardar archivo" writes the receipt via `renderReceiptText` through a native save dialog and opens the file. Export toolbar action is now a **real CSV export** (replaces the stub) with busy-state handling and messenger feedback
- **`PaymentListComponents.kt`** — `PaymentActionMenu` "Ver recibo" wired through `PaymentTable`; KPIs/footer currency `€` → `Bs`
- **`PaymentsUiModels.kt`** — added `PaymentUiModel.toReceiptData()`; amount labels in `Bs`
- **`PaymentReceiptDialog.kt`** (new) — monospaced-style receipt preview with save/cancel, busy + disabled states

### Patient summary export (patient detail)
- **`PatientDetailWindow.kt`** — builds `PatientSummaryBundle` from the loaded clinical profile; `exportFicha()` saves the HTML ficha, `printSummary()` saves the TXT resumen (both via native save dialog + open + snackbar feedback); `onViewReceipt` wired to a `PaymentReceiptDialog` for the Pagos tab
- **`PatientDetailComponents.kt`** — `PaymentItemCard` gets a "Ver recibo" action; `QuickActionsFooter`'s "Exportar ficha" / "Imprimir resumen" menu items now dispatch to the real actions (were no-ops)
- **`PatientDetailUiModels.kt`** — added `PatientDetailPaymentUi.toReceiptData(patientName, patientId)`
- **`ModernPatientDetailContent.kt`** — PAYMENTS tab forwards `onViewReceipt`; Overview passes `pendingBalance` into `ClinicalSummaryPanel`; QuickActionsFooter wired to `onExportFicha` / `onPrintSummary`

### Overview summary enhancements
- **`PatientClinicalComponents.kt`** — `ClinicalSummaryPanel` now shows the real pending balance chip (previously the parameter was declared but never passed, so the overview didn't compile) and adds an "Exportar ficha" header button
- Overview tab now reads: treatment-status chips, active prescriptions/follow-ups/plans, and the real pending balance (treatments − payments, floor 0)

### Interaction review
- Receipt, ficha export, resumen export and CSV export all run on `Dispatchers.IO` with the save picker on the EDT; busy flags disable re-entry and dialogs show cancellation messages
- `ExportService` fixes the `Dispatchers.Swing` unresolved reference by importing the `kotlinx.coroutines.swing.Swing` extension

## Files Modified

| File | Change |
|------|--------|
| `export/ExportService.kt` | New file (clinic identity, native save dialog, file writing, open) |
| `export/Receipts.kt` | New file (receipt data + plain-text renderer) |
| `export/PatientSummaryExport.kt` | New file (bundle, HTML ficha, TXT resumen, payments CSV) |
| `ui/payments/PaymentReceiptDialog.kt` | New file (receipt preview dialog) |
| `ui/payments/ModernPaymentsContent.kt` | Receipt dialog + real CSV export wiring |
| `ui/payments/PaymentListComponents.kt` | "Ver recibo" action + Bs currency in KPIs/footer |
| `ui/payments/PaymentsUiModels.kt` | `toReceiptData()` + Bs labels |
| `ui/PatientDetailWindow.kt` | Receipt dialog + ficha HTML export + resumen TXT export wiring |
| `ui/patientdetail/ModernPatientDetailContent.kt` | Overview pending balance, receipt wiring, export callbacks |
| `ui/patientdetail/PatientDetailComponents.kt` | PaymentItemCard "Ver recibo" + QuickActionsFooter export actions |
| `ui/patientdetail/PatientDetailUiModels.kt` | `toReceiptData()` mapper |
| `ui/patientdetail/PatientClinicalComponents.kt` | ClinicalSummaryPanel pending-balance chip + "Exportar ficha" button |

## Build Status

✅ BUILD SUCCESSFUL (0 errors) — `./gradlew build` + app smoke-run

## Acceptance Criteria

- ✓ "Ver recibo" opens a receipt preview from both the Payments screen and the patient detail Pagos tab
- ✓ Receipts can be saved as plain-text files via a native save dialog and opened afterwards
- ✓ "Exportar ficha" saves a complete clinical + financial HTML ficha
- ✓ "Imprimir resumen" saves a condensed text resumen (printable)
- ✓ Payments screen export produces a real CSV file (no longer a stub)
- ✓ Overview shows the real pending balance and an "Exportar ficha" shortcut
- ✓ All export flows show busy state and snackbar feedback (success / cancel / error)
- ✓ Full build passes

## Next Task

TASK-004 in `docs/roadmap/NEXT_TASK.md` — follow-up documentation kept in this milestone folder.
