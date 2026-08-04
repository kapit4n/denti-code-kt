# TASK-005: Clinical Workspace Review & Polish

## Objective

Finish the refactoring tracked in `REFACTORING.md` (split `PatientDetailWindow.kt`, `PatientClinicalComponents.kt` and `PatientClinicalDialogs.kt` so the window only orchestrates and no patient-workspace file exceeds ~800 lines), then do an interaction/consistency polish pass over the clinical workspace (currency `Bs`, empty states, dead/no-op actions).

## Changes

### Refactoring (all `REFACTORING.md` items now DONE)

- **`PatientDetailWindow.kt` (1299 → 116 lines)** — the window is now pure orchestration (state holder + `ModernPatientDetailContent` wiring + dialog host). Logic moved to:
  - **`PatientDetailWindowActions.kt`** (new) — state-holder class with all `mutableStateOf` state, `load()`/`refresh()`, derived values (`activeDoctors`, `treatmentLinkOptions`, enabled flags), workspace-UI builder, and the 33 `on*` callbacks.
  - **`PatientDetailSubmitActions.kt`** (new) — 17 extension functions on the state holder (`exportFicha`, `printSummary`, `submitVisit`…`submitPhase`, `confirmDeleteClinical`, `saveReceipt`, `editPatient`, `hardDeletePatient`).
  - **`PatientDetailDialogsHost.kt`** (new) — renders all 14 dialogs by reading the holder state and delegating to submit actions; `PatientDeleteDialog` and `PatientEditDialog` moved here from the window.
- **`PatientClinicalComponents.kt` (~1268 → 9 files)** — split by concern into `ClinicalShared.kt` (tab bar + section shell + record menu), `ClinicalSummaryPanel.kt`, `ClinicalHistoryPanel.kt`, `ClinicalTimelinePanel.kt`, `ClinicalDocumentsPanel.kt`, `ClinicalNotesPanel.kt`, `ClinicalPrescriptionsPanel.kt`, `ClinicalFollowUpsPanel.kt`, `ClinicalPlansPanel.kt`; `ClinicalSummaryChip` made `internal` (shared with the history panel).
- **`PatientClinicalDialogs.kt` (~1011 → 5 files)** — split by entity into `ClinicalDeleteDialog.kt`, `ClinicalMedicalDialogs.kt`, `ClinicalPrescriptionsDialogs.kt`, `ClinicalDocumentsDialogs.kt`, `ClinicalPlansDialogs.kt`; removed a stale cross-package import.

### Polish pass

- **Currency consistency** — money is now displayed as `Bs …` everywhere in the patient-detail workspace: header KPI "Saldo pendiente", payment summary metrics, payment row amounts, and timeline amounts (treatments/payments/plans). Previously these mixed bare `formatMoney` with `Bs`-prefixed chips.
- **Dead code removed** — `TreatmentsPanel` (no call sites) and its unused `PatientTreatmentRow`/`TreatmentsTable` imports.
- **No-op actions removed** — appointment cards no longer show a "Ver detalle" menu that did nothing; `DetailActionMenuButton` primary action is now optional so the payments card only offers "Ver recibo".

## Files Modified

| File | Change |
|------|--------|
| `ui/PatientDetailWindow.kt` | Rewritten: orchestrates only (116 lines) |
| `ui/patientdetail/PatientDetailWindowActions.kt` | New — state holder + view callbacks (472 lines) |
| `ui/patientdetail/PatientDetailSubmitActions.kt` | New — submit/export/delete extension functions (424 lines) |
| `ui/patientdetail/PatientDetailDialogsHost.kt` | New — dialog host + moved `PatientDeleteDialog`/`PatientEditDialog` (458 lines) |
| `ui/patientdetail/PatientClinicalComponents.kt` | Deleted — split into 9 panel files |
| `ui/patientdetail/PatientClinicalDialogs.kt` | Deleted — split into 5 dialog files |
| `ui/patientdetail/ClinicalShared.kt` … `ClinicalPlansPanel.kt` | New panel/component files |
| `ui/patientdetail/ClinicalDeleteDialog.kt` … `ClinicalPlansDialogs.kt` | New dialog files |
| `ui/patientdetail/PatientDetailComponents.kt` | Currency `Bs`, removed dead `TreatmentsPanel` + no-op menu, optional primary action |
| `ui/patientdetail/PatientDetailUiModels.kt` | Payment row `amountLabel` → `Bs …` |
| `ui/patientdetail/PatientClinicalUiModels.kt` | Timeline amounts → `Bs …` |

## Build Status

✅ BUILD SUCCESSFUL (0 errors) — `./gradlew build` + app smoke-run (75 s, no crash) after the refactor and after the polish edits.

## Acceptance Criteria

- ✓ `PatientDetailWindow.kt` only orchestrates (116 lines); all dialogs live in `*.Dialogs.kt` / `*DialogsHost.kt`
- ✓ No patient-workspace file exceeds ~800 lines (largest is `PatientDetailComponents.kt` at ~750)
- ✓ `PatientClinicalComponents.kt` and `PatientClinicalDialogs.kt` no longer exist
- ✓ Money displays in the workspace are consistently `Bs …`
- ✓ Dead `TreatmentsPanel` and no-op "Ver detalle" actions removed
- ✓ `./gradlew build` passes after each refactor step and after polish

## Next Task

Milestone 4 — Inventory Management (see `docs/roadmap/NEXT_TASK.md`). `REFACTORING.md` list is empty; `DentiRepository.kt` stays a low-priority opportunistic split only.
