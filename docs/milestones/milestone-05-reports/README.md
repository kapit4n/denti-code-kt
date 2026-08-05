# Milestone 5 — Reports

## Goal

Implement a dedicated reporting module: revenue, appointments, doctors, patients, treatments and inventory analytics with charts, KPIs, filters, exports and printing.

## Features (accumulated)

- TASK-001: Reports screen with date-range filter (7 días / 30 días / Este mes / 3 meses / Todo or custom Desde/Hasta), revenue + appointment KPIs, charts (ingresos por día, citas por día, ingresos por método), appointments-by-status breakdown, top procedures, and multi-section CSV export — all from real repository reads.
- TASK-002: doctor & patient analytics — top doctors by revenue (payments attributed via appointment/action doctor), patient acquisition trend (new patients per month), no-show rate KPI, plus INGRESO POR DOCTOR and PACIENTES NUEVOS POR MES CSV sections.
- TASK-003: treatment & inventory analytics — revenue by procedure category (donut + legend), revenue vs catalog price (catalog value, total charged, discount amount + %), and a stock movement report (movements, units in/out, per-type breakdown), plus INGRESO POR CATEGORÍA and MOVIMIENTOS DE STOCK CSV sections.

## Deliverable

Business intelligence dashboard.

## Tasks

| Task | Description | Status |
|------|-------------|--------|
| TASK-001 | Reports screen — revenue & appointment KPIs (date range, charts, CSV) | DONE |
| TASK-002 | Doctor & patient analytics (top doctors, patient acquisition, no-shows) | DONE |
| TASK-003 | Treatment & inventory analytics (categories, catalog vs charged, stock movements) | DONE |
| TASK-004 | Report filters & saved ranges | PENDING |
| TASK-005 | PDF/HTML report printing | PENDING |

## Status

**IN PROGRESS** — TASK-003 done: the Reports route now also shows revenue by procedure category, a catalog-vs-charged comparison and the stock movement report, all computed from real database reads and exported to CSV.
