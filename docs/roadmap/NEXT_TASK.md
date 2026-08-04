# Next Task

## Milestone 3 — Patient Workspace

## TASK-005: Clinical Workspace Review & Polish

**Objective**: Review and polish the Patient Clinical Workspace — and finish the remaining refactoring items tracked in `docs/milestones/milestone-03-patient/REFACTORING.md` (each below is a small, single-purpose change per the repo's iteration rule).

**Feature/polish items**:
- Interaction review of the clinical workspace (empty states, loading, snackbar feedback, disabled states)
- Consistency review (spacing, typography, currency `Bs`, labels in Spanish)
- Any follow-ups surfaced by TASK-004 (physical document flows)

**Also finishes (see `REFACTORING.md`)**:
1. Split `PatientDetailWindow.kt` (~1299 lines): extract the 9+ inline dialog composables into `PatientDetailDialogs.kt` and a `PatientDetailExportActions.kt`-style holder so the window only orchestrates
2. Split `PatientClinicalComponents.kt` (~1275 lines) into `ClinicalPanels.kt` / `ClinicalPlansPanel.kt` / `ClinicalSummaryPanel.kt` / `ClinicalShared.kt`
3. Split `PatientClinicalDialogs.kt` (~910 lines) by entity (`ClinicalMedicalDialogs.kt`, `ClinicalDocumentsDialogs.kt`, `ClinicalPlansDialogs.kt`, `ClinicalDeleteDialog.kt`)
4. Low priority: opportunistic split of `DentiRepository.kt` only if a future task already rewrites a section

**Definition of done**: no patient-workspace file exceeds ~800 lines, `PatientDetailWindow.kt` only orchestrates, and `./gradlew build` passes after each step.
