# Next Task

## Milestone 5 — Reports

**Objective**: Implement a dedicated reporting module (business intelligence dashboard) covering appointments, revenue, doctors, patients, treatments and inventory, with charts, KPIs, filters, exports and printing.

## TASK-001 & TASK-002 — DONE

- TASK-001: Reports screen with revenue & appointment KPIs — date-range filter, KPI row, line/bar/donut charts, appointments-by-status, top procedures, multi-section CSV export.
- TASK-002: doctor & patient analytics — top doctors by revenue (payments attributed via appointment/action doctor), monthly patient acquisition, no-show rate KPI + CSV sections.
- See `docs/milestones/milestone-05-reports/TASK-001.md` and `TASK-002.md`.

## TASK-003 (provisional)

Treatment & inventory analytics on the same Reports screen with the existing date-range filter and chart components:
- **Top procedures by category** (group `topProcedures` by `procedure_types.category`, plus a per-category revenue split).
- **Revenue vs catalog price** — sum of `standard_price` vs actual charged per performed action (`total_price`), to measure discount/negotiation.
- **Stock movement report** — entries/exits per product per period from inventory movements (PURCHASE/TREATMENT_CONSUMPTION/MANUAL_ADJUSTMENT/…), e.g. units moved and value per period.

Reuse `EnterpriseBarChart`/`EnterpriseDonutChart`/`MetricCard`; add new CSV sections (e.g. INGRESO POR CATEGORÍA, MOVIMIENTOS DE STOCK).

**Definition of done**: new KPIs computed from real repository reads, charts + CSV sections rendered, `./gradlew build` passes with 0 errors, and the app smoke-runs.

> Previous: **Milestone 4 — Inventory Management** COMPLETE. **M5 TASK-001 + TASK-002 DONE** (`docs/milestones/milestone-05-reports/`).
