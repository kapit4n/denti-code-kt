# Next Task

## Milestone 5 — Reports

**Objective**: Implement a dedicated reporting module (business intelligence dashboard) covering appointments, revenue, doctors, patients, treatments and inventory, with charts, KPIs, filters, exports and printing.

## TASK-001, TASK-002, TASK-003 & TASK-004 — DONE

- TASK-001: Reports screen with revenue & appointment KPIs — date-range filter, KPI row, line/bar/donut charts, appointments-by-status, top procedures, multi-section CSV export.
- TASK-002: doctor & patient analytics — top doctors by revenue (payments attributed via appointment/action doctor), monthly patient acquisition, no-show rate KPI + CSV sections.
- TASK-003: treatment & inventory analytics — revenue by procedure category (donut + legend), revenue vs catalog price (catalog value vs charged per performed action, discount %), stock movement report (entries/exits per period by movement type) + INGRESO POR CATEGORÍA and MOVIMIENTOS DE STOCK CSV sections.
- TASK-004: report filters & saved ranges — last used date range persisted across runs (`~/.denti-code-kt/reports-settings.properties`), named presets (save/apply/delete via «Guardar rango» chip), keyboard shortcuts (Alt+1…5 quick ranges, Alt+Mayús+1…9 presets).
- See `docs/milestones/milestone-05-reports/TASK-001.md` … `TASK-004.md`.

## TASK-005 (provisional)

PDF/HTML report printing: render the full report (KPIs + charts + tables) to a printable document and print/save from the Reports screen.

> Previous: **Milestone 4 — Inventory Management** COMPLETE. **M5 TASK-001 + TASK-002 + TASK-003 + TASK-004 DONE** (`docs/milestones/milestone-05-reports/`).
