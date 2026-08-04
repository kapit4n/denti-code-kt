# Next Task

## Milestone 5 — Reports

**Objective**: Implement a dedicated reporting module (business intelligence dashboard) covering appointments, revenue, doctors, patients, treatments and inventory, with charts, KPIs, filters, exports and printing.

## TASK-001 (provisional)

Design and scaffold the Reports screen — confirm scope against `docs/roadmap/PROJECT_ROADMAP.md` (Milestone 5) and current `docs/STATUS.md` before implementing. Expected first slice: replace the Reports `PlaceholderScreen` with a real screen showing revenue + appointment KPIs (date range, filterable), reusing the existing `EnterpriseBarChart`/chart components and repository reads.

**Definition of done** (per milestone): reports generated from real repository reads, filters + exports working, `./gradlew build` passes with 0 errors, and the app smoke-runs.

> Previous milestone: **Milestone 4 — Inventory Management** is COMPLETE — TASK-005 audit done (`docs/milestones/milestone-04-inventory/TASK-005.md`).
