# Next Task

## Milestone 5 — Reports

**Objective**: Implement a dedicated reporting module (business intelligence dashboard) covering appointments, revenue, doctors, patients, treatments and inventory, with charts, KPIs, filters, exports and printing.

## TASK-001 — DONE

Reports screen with revenue & appointment KPIs: date-range filter (chips 7 días / 30 días / Este mes / 3 meses / Todo + custom Desde/Hasta), KPI row (ingreso total, citas, nuevos pacientes, promedio por pago), line/bar/donut charts, appointments-by-status, top-5 procedures, and multi-section CSV export — all from real `reportsOverview()` reads. See `docs/milestones/milestone-05-reports/TASK-001.md`.

## TASK-002 (provisional)

Doctor & patient analytics on the same Reports screen using the existing date-range filter and chart components: **top doctors by revenue** (from `payments` joined to appointments→doctors), **patient acquisition trend** (new patients per month from `patients.created_at`), and **appointment no-show rate** (NO_SHOW / total per period). Reuse `EnterpriseBarChart`/`EnterpriseLineChart`/`EnterpriseDonutChart` and `MetricCard`; add the new sections to the CSV export (e.g. INGRESO POR DOCTOR, PACIENTES NUEVOS POR PERÍODO).

**Definition of done**: new KPIs computed from real repository reads, charts + CSV sections rendered, `./gradlew build` passes with 0 errors, and the app smoke-runs.

> Previous: **Milestone 4 — Inventory Management** COMPLETE. **M5 TASK-001 DONE** (`docs/milestones/milestone-05-reports/TASK-001.md`).
