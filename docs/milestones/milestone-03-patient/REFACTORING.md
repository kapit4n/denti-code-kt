# Milestone 3 — Refactoring Tracker

Tracks the code-quality/refactoring work that must be finished as part of the Patient Workspace milestone. It is expected to span TASK-004 (physical documents) and TASK-005 (workspace review & polish); nothing here is a new feature, it exists so later tickets keep improving the structure until the list is empty.

Last updated: 2026-08-04 (after TASK-004 commit)

---

## Rule

Never attempt a large refactor in the same change as a feature unless the change touches the same file. Follow the repo rule from `docs/roadmap/PROJECT_ROADMAP.md`: small, single-purpose iterations.

## Current file inventory (after TASK-003)

| File | Lines | Concern | Status |
|------|-------|---------|--------|
| `data/DentiRepository.kt` | ~3153 | All data access | ⚠️ large — split only opportunistically (low priority) |
| `ui/PatientDetailWindow.kt` | ~1299 | Window orchestration + 9 inline dialogs | 🔴 refactor target |
| `ui/patientdetail/PatientClinicalComponents.kt` | ~1275 | All workspace panels/cards/menus | 🔴 refactor target |
| `ui/patientdetail/PatientClinicalDialogs.kt` | ~910 | 8 clinical dialogs + delete confirm | 🟡 refactor when touched |
| `ui/patientdetail/PatientDetailComponents.kt` | ~761 | Header/KPIs/payments/quick actions | 🟡 refactor when touched |
| `ui/patientdetail/PatientDetailDialogs.kt` | ~619 | Visit/payment/treatment/edit dialogs | 🟡 refactor when touched |
| `ui/patientdetail/PatientClinicalUiModels.kt` | ~597 | UI models + timeline + state | OK |
| `export/*.kt` | ~760 (4 files) | Export service, receipts, ficha, document store | OK (extracted in TASK-003/004) |
| `ui/payments/*.kt` | ~1500 (5 files) | Payments screen | OK (already split by concern) |

## Refactoring worklog

### DONE — TASK-003
- **Extracted the export stack** into a new `export/` package (`ExportService.kt`, `Receipts.kt`, `PatientSummaryExport.kt`) so receipt/ficha/CSV rendering no longer lives inside UI files.
- **Wired `QuickActionsFooter` / `ClinicalSummaryPanel` callbacks** that were previously declared-but-dead (removed the `onMoreActions` no-op, fixed the Overview missing-argument compile break).

### DONE — TASK-004
- **Normalized the receipt type usage** — `PatientDetailWindow` now imports `PatientDetailPaymentUi` instead of the fully-qualified `com.denticode.kt.ui.patientdetail.PatientDetailPaymentUi`.
- **`PatientDocumentDialog` gained `patientId`** and the physical-file picker; `DeleteClinicalTarget` now carries an optional `filePath` (small, task-local shape change — no full split yet).

### PENDING — assigned to TASK-005
1. **Split `PatientDetailWindow.kt`** (~1299 lines)
   - Extract the 9+ inline dialog composables into `PatientDetailDialogs.kt` (or a new `PatientDetailExportActions.kt` for the receipt/export handlers).
   - Extract a private `rememberClinicalActions(repo, patient, messenger, refreshNonce)`-style state holder so the composable body shrinks to layout + wiring.
2. **Split `PatientClinicalComponents.kt`** (~1263 lines)
   - Suggested split: `ClinicalPanels.kt` (history/documents/notes), `ClinicalPlansPanel.kt` (treatment plans), `ClinicalSummaryPanel.kt` (overview summary), `ClinicalShared.kt` (tab bar + section shell + cards/menus).
3. **Split `PatientClinicalDialogs.kt`** (~910 lines)
   - Suggested split by entity: `ClinicalMedicalDialogs.kt`, `ClinicalDocumentsDialogs.kt`, `ClinicalPlansDialogs.kt`, `ClinicalDeleteDialog.kt`.
4. **Low priority:** `DentiRepository.kt` grows monotonically; split only if a future task already rewrites a section (do NOT do a standalone mega-split).

## Definition of done (all items above)
- No file in the patient workspace exceeds ~800 lines.
- `PatientDetailWindow.kt` only orchestrates (state + wiring); all dialogs live in `*.Dialogs.kt`.
- `PatientClinicalComponents.kt` split into focused panel files.
- A `make build` (or `./gradlew build`) passes with 0 errors after each refactor.
