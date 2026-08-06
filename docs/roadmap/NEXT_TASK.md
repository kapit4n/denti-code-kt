# Next Task

## Milestone 5 — Reports

**Objective**: Implement a dedicated reporting module (business intelligence dashboard) covering appointments, revenue, doctors, patients, treatments and inventory, with charts, KPIs, filters, exports and printing.

## Milestone 5 — COMPLETE

- TASK-001: Reports screen with revenue & appointment KPIs — date-range filter, KPI row, line/bar/donut charts, appointments-by-status, top procedures, multi-section CSV export.
- TASK-002: doctor & patient analytics — top doctors by revenue (payments attributed via appointment/action doctor), monthly patient acquisition, no-show rate KPI + CSV sections.
- TASK-003: treatment & inventory analytics — revenue by procedure category (donut + legend), revenue vs catalog price (catalog value vs charged per performed action, discount %), stock movement report (entries/exits per period by movement type) + INGRESO POR CATEGORÍA and MOVIMIENTOS DE STOCK CSV sections.
- TASK-004: report filters & saved ranges — last used date range persisted across runs (`~/.denti-code-kt/reports-settings.properties`), named presets (save/apply/delete via «Guardar rango» chip), keyboard shortcuts (Alt+1…5 quick ranges, Alt+Mayús+1…9 presets).
- TASK-005: PDF/HTML report printing — full print-ready HTML report (KPIs + tables + CSS bars) exported from the Reports screen and opened in the browser for print / save-as-PDF, plus the existing CSV export.
- See `docs/milestones/milestone-05-reports/TASK-001.md` … `TASK-005.md`.

## TASK-006 (provisional)

Real PDF export (vector graphics via `java.awt.print` into a `.pdf` file) as an alternative to HTML-in-browser printing, or dependency-free SVG chart embedding in the HTML report.

> Previous: **Milestone 4 — Inventory Management** COMPLETE. **Milestone 5 — Reports COMPLETE** (`docs/milestones/milestone-05-reports/`).
