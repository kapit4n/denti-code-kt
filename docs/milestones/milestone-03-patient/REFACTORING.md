# Milestone 3 — Refactoring Tracker

Tracks the code-quality/refactoring work that must be finished as part of the Patient Workspace milestone. It is expected to span TASK-004 (physical documents) and TASK-005 (workspace review & polish); nothing here is a new feature, it exists so later tickets keep improving the structure until the list is empty.

Last updated: 2026-08-04 (after TASK-005 commit)

---

## Rule

Never attempt a large refactor in the same change as a feature unless the change touches the same file. Follow the repo rule from `docs/roadmap/PROJECT_ROADMAP.md`: small, single-purpose iterations.

## Current file inventory (after TASK-005)

| File | Lines | Concern | Status |
|------|-------|---------|--------|
| `data/DentiRepository.kt` | ~3153 | All data access | ⚠️ large — split only opportunistically (low priority) |
| `ui/patientdetail/PatientDetailComponents.kt` | ~750 | Header/KPIs/payments/quick actions | OK |
| `ui/patientdetail/PatientDetailDialogs.kt` | ~619 | Visit/payment/treatment/edit dialogs | OK |
| `ui/patientdetail/PatientClinicalUiModels.kt` | ~597 | UI models + timeline + state | OK |
| `ui/patientdetail/PatientDetailDialogsHost.kt` | ~458 | All detail-window dialog rendering | OK |
| `ui/patientdetail/PatientDetailWindowActions.kt` | ~472 | Window state holder + view callbacks | OK |
| `ui/patientdetail/PatientDetailSubmitActions.kt` | ~424 | Window submit/export/delete extensions | OK |
| `ui/PatientDetailWindow.kt` | ~116 | Window orchestration only | OK |
| `ui/patientdetail/Clinical*.kt` | 9 files | Workspace panels/shell | OK |
| `ui/patientdetail/Clinical*Dialogs.kt` | 5 files | Clinical entity dialogs | OK |
| `export/*.kt` | ~760 (4 files) | Export service, receipts, ficha, document store | OK |
| `ui/payments/*.kt` | ~1500 (5 files) | Payments screen | OK (already split by concern) |

## Refactoring worklog

### DONE — TASK-003
- **Extracted the export stack** into a new `export/` package (`ExportService.kt`, `Receipts.kt`, `PatientSummaryExport.kt`) so receipt/ficha/CSV rendering no longer lives inside UI files.
- **Wired `QuickActionsFooter` / `ClinicalSummaryPanel` callbacks** that were previously declared-but-dead (removed the `onMoreActions` no-op, fixed the Overview missing-argument compile break).

### DONE — TASK-004
- **Normalized the receipt type usage** — `PatientDetailWindow` now imports `PatientDetailPaymentUi` instead of the fully-qualified `com.denticode.kt.ui.patientdetail.PatientDetailPaymentUi`.
- **`PatientDocumentDialog` gained `patientId`** and the physical-file picker; `DeleteClinicalTarget` now carries an optional `filePath` (small, task-local shape change — no full split yet).

### DONE — TASK-005 (all remaining items)
1. **Split `PatientDetailWindow.kt`** (1299 → 116 lines):
   - `PatientDetailWindowActions.kt` — state-holder class (all `mutableStateOf` state, `load()`, derived values, workspace builder, 33 `on*` callbacks).
   - `PatientDetailSubmitActions.kt` — 17 extension functions (export/print + all dialog submit handlers + delete/receipt/edit/hard-delete).
   - `PatientDetailDialogsHost.kt` — renders all 14 dialogs; `PatientDeleteDialog`/`PatientEditDialog` moved here from the window.
   - Window body now only: remember holder, `LaunchedEffect` load, `ModernPatientDetailContent` wiring, dialog host.
2. **Split `PatientClinicalComponents.kt`** (~1268 → 9 files): `ClinicalShared.kt`, `ClinicalSummaryPanel.kt`, `ClinicalHistoryPanel.kt`, `ClinicalTimelinePanel.kt`, `ClinicalDocumentsPanel.kt`, `ClinicalNotesPanel.kt`, `ClinicalPrescriptionsPanel.kt`, `ClinicalFollowUpsPanel.kt`, `ClinicalPlansPanel.kt`. `ClinicalSummaryChip` visibility → `internal` (shared with history panel).
3. **Split `PatientClinicalDialogs.kt`** (~1011 → 5 files): `ClinicalDeleteDialog.kt`, `ClinicalMedicalDialogs.kt`, `ClinicalPrescriptionsDialogs.kt`, `ClinicalDocumentsDialogs.kt`, `ClinicalPlansDialogs.kt`.
4. **Low priority:** `DentiRepository.kt` grows monotonically; split only if a future task already rewrites a section (do NOT do a standalone mega-split).

## Definition of done (all items above)

✅ Met:
- No file in the patient workspace exceeds ~800 lines.
- `PatientDetailWindow.kt` only orchestrates (state + wiring); all dialogs live in `*.Dialogs.kt` / `*DialogsHost.kt`.
- `PatientClinicalComponents.kt` / `PatientClinicalDialogs.kt` split into focused files.
- `./gradlew build` passes with 0 errors after each refactor.

Remaining: `DentiRepository.kt` (opportunistic only — intentionally not split).
