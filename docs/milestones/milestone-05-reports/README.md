# Milestone 5 — Reports

## Goal

Implement a dedicated reporting module: revenue, appointments, doctors, patients, treatments and inventory analytics with charts, KPIs, filters, exports and printing.

## Features (accumulated)

- TASK-001: Reports screen with date-range filter (7 días / 30 días / Este mes / 3 meses / Todo or custom Desde/Hasta), revenue + appointment KPIs, charts (ingresos por día, citas por día, ingresos por método), appointments-by-status breakdown, top procedures, and multi-section CSV export — all from real repository reads.
- TASK-002: doctor & patient analytics — top doctors by revenue (payments attributed via appointment/action doctor), patient acquisition trend (new patients per month), no-show rate KPI, plus INGRESO POR DOCTOR and PACIENTES NUEVOS POR MES CSV sections.
- TASK-003: treatment & inventory analytics — revenue by procedure category (donut + legend), revenue vs catalog price (catalog value, total charged, discount amount + %), and a stock movement report (movements, units in/out, per-type breakdown), plus INGRESO POR CATEGORÍA and MOVIMIENTOS DE STOCK CSV sections.
- TASK-004: report filters & saved ranges — last used date range is persisted across runs (`~/.denti-code-kt/reports-settings.properties`), custom named presets (save / apply / delete from the chip row), and keyboard shortcuts (Alt+1…5 quick ranges, Alt+Mayús+1…9 presets).
- TASK-005: PDF/HTML report printing — the **«HTML / Imprimir»** button exports the full report as a print-ready, self-contained HTML document (KPI grid, all tables, proportional CSS bars, `@media print` styling, clinic identity) and opens it in the browser for printing or "Save as PDF".

## Deliverable

Business intelligence dashboard.

## Tasks

| Task | Description | Status |
|------|-------------|--------|
| TASK-001 | Reports screen — revenue & appointment KPIs (date range, charts, CSV) | DONE |
| TASK-002 | Doctor & patient analytics (top doctors, patient acquisition, no-shows) | DONE |
| TASK-003 | Treatment & inventory analytics (categories, catalog vs charged, stock movements) | DONE |
| TASK-004 | Report filters & saved ranges (persistence, presets, shortcuts) | DONE |
| TASK-005 | PDF/HTML report printing | DONE |

## Status

**COMPLETE** — all 5 tasks done. The Reports screen now covers revenue/appointment/doctor/patient/treatment/inventory analytics with date-range filters, saved presets, keyboard shortcuts, multi-section CSV export and a print-ready HTML report (browser print / save-as-PDF).
