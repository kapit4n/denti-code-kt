# TASK-002: Treatment Plans & Pending Balance

## Objective

Add a real pending-balance computation derived from treatments vs. payments (replacing the stub formula and the directory `0.0 // TODO`), and introduce treatment plans: a per-patient plan with phases, estimated cost and a status workflow, surfaced as a new workspace tab plus an overview summary chip.

## Changes

### Database (`Tables.kt`)
- **Added** `TreatmentPlansTable` — `patientId` (FK CASCADE), `title`, `description`, `status` (default DRAFT), `estimatedCost` (double, maintained as SUM of phase costs), `createdAtEpochMs`
- **Added** `TreatmentPlanPhasesTable` — `planId` (FK CASCADE), `name`, `description`, `estimatedCost`, `status` (default PENDING), `sortOrder`, `createdAtEpochMs`
- Registered both tables in `Database.kt` via `SchemaUtils.createMissingTablesAndColumns()`

### Models (`Models.kt`)
- **Added** `TreatmentPlanStatus` enum (DRAFT/ACTIVE/COMPLETED/CANCELLED) with `.labelEs` + `treatmentPlanStatusOptions()` and `.fromDb`
- **Added** `TreatmentPlanPhaseStatus` enum (PENDING/IN_PROGRESS/COMPLETED/CANCELLED) with `.labelEs` + `treatmentPlanPhaseStatusOptions()`
- **Added** row models `TreatmentPlanPhase`, `TreatmentPlan` (with `completedPhases`, `phaseProgress` computed)
- **Added** register/update request classes (`TreatmentPlanRegisterRequest`, `TreatmentPlanUpdateRequest`, `TreatmentPlanPhaseRegisterRequest`, `TreatmentPlanPhaseUpdateRequest`)

### Repository (`DentiRepository.kt`)
- **Pending balance now real**: `loadPatientDirectory` computes `billableByPatient` (SUM of non-CANCELLED treatments from `listTreatmentsInternal`) minus `paidByPatient` (SUM of `PaymentsTable.amount` grouped by patient), `coerceAtLeast(0.0)`. Replaces the `0.0 // TODO` stub in the directory.
- **Currency**: replaced `€` literals with `Bs` in `listTreatmentPaymentOptions` labels and the payment audit detail.
- **Added** plans: `listTreatmentPlansForPatient` (join phases, order by `sortOrder ASC, id ASC`, group by plan, plans ordered by `createdAtEpochMs DESC`), `registerTreatmentPlan`, `updateTreatmentPlan`, `updateTreatmentPlanStatus`, `deleteTreatmentPlan`
- **Added** phases: `addTreatmentPlanPhase` (next `sortOrder`, then recompute cost), `updateTreatmentPlanPhase` (recompute cost), `updateTreatmentPlanPhaseStatus` (auto-completes parent plan when all phases are COMPLETED), `deleteTreatmentPlanPhase` (recompute cost)
- **Added** private `recomputeTreatmentPlanCost(planId)` — sums phase costs into `treatment_plans.estimated_cost`
- **Added** `ResultRow.toTreatmentPlan` / `toTreatmentPlanPhase` extension mappers

### Seeder
- **Created** `TreatmentPlansSeeder.kt` — deterministic demo data (`Random(4242)`), ~50% of patients get 1–2 plans × 2–4 phases, plan cost = sum of phases
- **Updated** `DemoDataSeeder.kt` — calls `TreatmentPlansSeeder.seed(config, patients)` after `ClinicalWorkspaceSeeder`

### UI Models (`PatientClinicalUiModels.kt`)
- **Added** `TREATMENT_PLAN("Plan de tratamiento")` tab to `PatientWorkspaceTab` (10 tabs total)
- **Added** `TimelineEntryKind.TREATMENT_PLAN` + timeline entries (namespace `100_000_000L + plan.id`, subtitle `«status.labelEs» · N fases`, amount = estimated cost)
- **Added** `PatientTreatmentPlanUi` / `PatientTreatmentPlanPhaseUi` + `toUi()` mappers + status color helpers
- **Extended** `PatientWorkspaceUiState` with `treatmentPlans` and computed `activePlans`; `buildPatientWorkspaceUiState` / `buildPatientTimeline` gained plan params

### UI Components (`PatientClinicalComponents.kt`)
- **Added** `TreatmentPlanPanel` — plan cards (status badge, progress bar, phase list with per-phase status menus), buttons for new plan / edit / add phase / delete
- **Added** plan/phase status dropdown menus (DRAFT→ACTIVE→COMPLETED workflow, auto-complete)
- **Extended** `ClinicalSummaryPanel` with a "Planes activos" chip
- **Extended** timeline icon/tab mapping for `TREATMENT_PLAN`

### UI Dialogs (`PatientClinicalDialogs.kt`)
- **Added** `TreatmentPlanDialog` (create/edit: title, description, status)
- **Added** `TreatmentPlanPhaseDialog` (create/edit: name, description, estimated cost in Bs with decimal keyboard, status)
- **Extended** `ClinicalDeleteKind` with `TREATMENT_PLAN` and `TREATMENT_PLAN_PHASE`

### Content & Window
- **`ModernPatientDetailContent.kt`** — treatment plan tab with badge count, panel rendering, 8 new callbacks, overview chip wired to `activePlans`
- **`PatientDetailWindow.kt`** — loads `listTreatmentPlansForPatient`, builds plans into workspace state + timeline, dialog states/busy/errors, delete dispatch (`deleteTreatmentPlan` / `deleteTreatmentPlanPhase`), status-change handlers with `refreshNonce` + messenger feedback

### UI Currency fixes
- `ui/treatments/TreatmentFormFields.kt`, `TreatmentListComponents.kt`, `ui/payments/PaymentsUiModels.kt`, `PaymentListComponents.kt` — `€` → `Bs`

## Files Modified

| File | Change |
|------|--------|
| `data/Tables.kt` | Added 2 treatment-plan tables |
| `data/Database.kt` | Registered new tables in migration |
| `data/Models.kt` | Added 2 enums + 2 row models + 4 request classes |
| `data/DentiRepository.kt` | Real pending balance + Bs currency + 12 plan/phase methods |
| `data/seeders/TreatmentPlansSeeder.kt` | New seeder (demo treatment plans) |
| `data/seeders/DemoDataSeeder.kt` | Registered treatment-plans seeder |
| `ui/patientdetail/PatientClinicalUiModels.kt` | Plan tab, UI models, timeline entries, workspace state |
| `ui/patientdetail/PatientClinicalComponents.kt` | TreatmentPlanPanel + summary chip + timeline mappings |
| `ui/patientdetail/PatientClinicalDialogs.kt` | Plan/phase dialogs + delete kinds |
| `ui/patientdetail/ModernPatientDetailContent.kt` | Plan tab + callbacks + overview chip |
| `ui/PatientDetailWindow.kt` | Plan loading + dialogs + delete dispatch |
| `ui/treatments/TreatmentFormFields.kt`, `TreatmentListComponents.kt` | Bs currency |
| `ui/payments/PaymentsUiModels.kt`, `PaymentListComponents.kt` | Bs currency |

## Build Status

✅ BUILD SUCCESSFUL (0 errors)

## Acceptance Criteria

- ✓ Patient directory shows a real pending balance (treatments − payments, floor 0)
- ✓ New "Plan de tratamiento" tab lists plans with phases and progress
- ✓ Plans/phases support create, edit, delete and status changes
- ✓ Auto-completing all phases completes the parent plan
- ✓ Plan estimated cost always equals the sum of its phase costs
- ✓ Timeline shows treatment plans with jump-to-tab
- ✓ Overview summary shows active plan count
- ✓ Currency consistently shown as Bs across treatments/payments
- ✓ Demo data seeds realistic treatment plans
- ✓ Full build passes

## Next Task

TASK-003 in `docs/roadmap/NEXT_TASK.md` — follow-up documentation kept in this milestone folder.
