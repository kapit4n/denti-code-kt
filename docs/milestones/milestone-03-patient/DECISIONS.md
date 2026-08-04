# Milestone 3 — Decisions

## TASK-003: Receipts and Patient Summary Export

### No third-party export libraries
- **Decision:** Exports are plain UTF-8 text/HTML/CSV produced with the JDK stdlib (`ExportService` + `FileDialog` + `Desktop.open`)
- **Rationale:** The project intentionally has no Apache POI/iText/OpenCSV dependencies; receipts, fichas and CSVs print fine from the system viewer, and adding PDF tooling is deferred to a later milestone.

### Receipt format is plain text
- **Decision:** `renderReceiptText()` produces a fixed-width text receipt; the dialog preview mirrors it
- **Rationale:** Guarantees identical output when previewed, saved or printed; no layout engine needed.

### Patient ficha export is HTML (print-friendly)
- **Decision:** `renderPatientSummaryHtml()` produces a self-contained HTML file with print CSS
- **Rationale:** Opens in any browser and prints cleanly; includes the full clinical + financial picture in one artifact. The `PatientSummaryBundle` mirrors the data the workspace already loads (no new DB queries).

### Save dialogs run on the EDT, file I/O off it
- **Decision:** `pickSaveFile()` uses `Dispatchers.Swing`; writes use `Dispatchers.IO`
- **Rationale:** Native `FileDialog` must live on the AWT event thread; blocking writes must not.

### Pending balance on the overview
- **Decision:** The Overview `ClinicalSummaryPanel` shows the real pending balance chip and an "Exportar ficha" button
- **Rationale:** The balance was already computed by `PaymentSummaryUiModel`; surfacing it on the first tab makes the ledger visible without navigating to Pagos.

## TASK-002: Treatment Plans & Pending Balance

### Real pending balance (treatments − payments)
- **Decision:** `loadPatientDirectory` computes balance as SUM(non-CANCELLED treatments) − SUM(payments), floored at 0
- **Rationale:** Replaces the fake formula and the directory `0.0 // TODO`; the ledger is now a derived value from data the clinic already enters (treatments and payments), avoiding a mutable balance column that can drift.

### Plans store cost as a maintained sum
- **Decision:** `treatment_plans.estimated_cost` is kept equal to the SUM of its phases via `recomputeTreatmentPlanCost()` after every phase add/update/delete
- **Rationale:** A single denormalized column makes listing, summary chips and timeline cheap while staying correct by construction; recompute is local to the plan.

### Auto-complete of the parent plan
- **Decision:** `updateTreatmentPlanPhaseStatus` completes the parent plan when all its phases become COMPLETED
- **Rationale:** Matches the clinical workflow — finishing the last phase finishes the plan — without requiring a separate user action.

### Plan/phase status enums
- **Decision:** Plan: DRAFT/ACTIVE/COMPLETED/CANCELLED. Phase: PENDING/IN_PROGRESS/COMPLETED/CANCELLED
- **Rationale:** Plans have a pre-treatment lifecycle; phases track execution. Status stored as string keys with `labelEs` + `fromDb` mapping, consistent with prior enums.

### Currency `Bs`
- **Decision:** Standardized `Bs` (bolivian boliviano) in treatments/payments UI and repo labels, replacing stray `€`
- **Rationale:** The app is a Bolivian clinic; mixing currencies in labels is confusing. Inventory screens already used `Bs`.

### Plan timeline namespace
- **Decision:** Treatment plans enter the timeline with id namespace `100_000_000L + plan.id`
- **Rationale:** The existing in-memory merge namespaces each kind (10M/20M/…); plans follow the pattern so ids never collide.

## TASK-001: Patient Clinical Workspace

### Tabbed workspace vs. single long scroll
- **Decision:** Replaced the single scrollable detail view with a tabbed workspace (`PatientWorkspaceTab`) while keeping the patient header + quick actions always visible
- **Rationale:** The 9 content areas would make one long scroll impractical at 920x720. Tabs give fast access while preserving the header context and footer actions.

### 6 new tables vs. columns on PatientsTable
- **Decision:** Separate tables per entity (medical history, dental history, documents, notes, prescriptions, follow-ups)
- **Rationale:** Each entity has a distinct lifecycle and list semantics (multiple records per patient). Adding all of them as columns on `PatientsTable` would be unmaintainable and would break single-value assumptions.

### `PatientClinicalProfile` aggregate loader
- **Decision:** One repository call (`loadPatientClinicalProfile`) returns all clinical lists
- **Rationale:** The detail window needs the whole clinical file at once; a single transactional read avoids N+1 round trips and keeps the composable wiring simple.

### Register request reused for update flows
- **Decision:** Dialogs emit register-style requests; the window maps to update requests for edit mode
- **Rationale:** Field sets are identical; avoids duplicating two full dialog variants per entity.

### Date-only storage (ISO `yyyy-MM-dd`)
- **Decision:** `recordedAt`, `prescribedAt`, `dueDate` stored as ISO date strings, consistent with existing date columns
- **Rationale:** Clinical records and prescriptions are day-granular; follow-ups are day-granular. Matches `PatientsTable.dateOfBirth` convention and keeps timeline grouping simple.

### Document file picker deferred
- **Decision:** `PatientDocumentDialog` records metadata (title, category, fileName, fileSize, notes); physical file selection is a later phase
- **Rationale:** The file-management phase (TASK-004) owns real file I/O and storage paths; seeding metadata now proves the workflow without depending on a file dialog.

### Documents are metadata-only for now
- **Decision:** Seeder inserts fake file paths; "Abrir" is enabled only when a real path exists
- **Rationale:** Demonstrates the documents tab end-to-end while clearly separating concerns from the future file-management task.

### Timeline as an in-memory merge
- **Decision:** `buildPatientTimeline()` merges all entity lists in memory, sorted by timestamp
- **Rationale:** Clinic volumes are small; avoids complex UNION SQL across 9 tables. Ids are namespaced by kind (10M/20M/…) so entity ids don't collide.

### Unified delete confirmation
- **Decision:** Single `DeleteClinicalConfirmDialog` driven by a `DeleteClinicalTarget` (kind + id)
- **Rationale:** Six near-identical confirmation dialogs would be duplication; the target object keeps the dialog generic while the window dispatches to the right repository call.

### Clinical seed data
- **Decision:** `ClinicalWorkspaceSeeder` uses deterministic `Random(777)` and requires patients, doctors and procedures
- **Rationale:** Deterministic data makes manual QA reproducible; the seed runs through the existing `seedIfEmpty` orchestration and is skipped when related tables are empty.
