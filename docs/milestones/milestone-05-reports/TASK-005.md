# TASK-005: Reports — PDF/HTML report printing

## Objective

Make the full report printable from the Reports screen: **export a self-contained, print-ready HTML document** containing the entire overview (KPI summary + tables + charts) that opens in the system browser, where it can be printed to a physical printer or saved as PDF from the browser's print dialog. No third-party PDF dependency (project constraint), so "print/save as PDF" is delivered through the browser's standard flow.

## What was added

### Export (`export/ReportsHtmlExport.kt`)
- New `renderReportsHtml(overview, clinicName, clinicCity, clinicCountry)` — dependency-free, self-contained HTML document (inline `<style>`, no external assets) mirroring every section of the on-screen report:
  - **Resumen**: KPI grid — ingreso total, pagos registrados, citas, nuevos pacientes, tasa de inasistencia, valor de catálogo, total cobrado y descuento.
  - **Ingresos por día**: full table (Fecha | Ingreso) for the selected range.
  - **Ingresos por método / doctor / categoría**: value rows with proportional CSS bars.
  - **Citas por estado**: bars using the same status colour map as the UI (`COMPLETED`, `CONFIRMED`, `SCHEDULED`, `IN_PROGRESS`, `RESCHEDULED`, `CANCELLED`, `NO_SHOW`).
  - **Pacientes nuevos por mes**: monthly bar rows.
  - **Procedimientos top**: table (procedimiento | cantidad | ingreso).
  - **Ingresos vs catálogo**: catálogo/cobrado/descuento + tasa de descuento table.
  - **Movimientos de stock**: totals line + per-type bars + per-type detail table.
- Print-ready CSS: A4-friendly sheet layout, compact typography, proportional `.track`/`.fill` bars and an `@media print` rule; header shows the clinic identity (`ExportService.clinicName/clinicCity/clinicCountry`) and the generated date.
- All data strings are HTML-escaped (user-provided names, categories, procedure names).

### UI (`ui/reports/ReportsScreen.kt`, `ui/reports/ReportsContent.kt`)
- New `exportHtmlReport()` in the screen: save dialog defaulting to `reporte-<fecha>.html` → writes the rendered HTML on the IO dispatcher → success messenger → `ExportService.openFile(file)` to open it in the browser (mirrors the existing patient-ficha HTML export flow).
- **ReportsHeader** now renders two actions: **«HTML / Imprimir»** (`Icons.Default.Print`) and **«Exportar CSV»** (existing).

## Components added / updated

| Component | Status |
|-----------|--------|
| `renderReportsHtml` (+ HTML helpers) in `export/ReportsHtmlExport.kt` | Added |
| `ReportsScreen.exportHtmlReport()` | Added |
| `ReportsHeader` second action «HTML / Imprimir» | Updated |

## Verification

- Runtime probe (temporary, removed): rendered a synthetic `ReportsOverview` and asserted 19 markers — doctype, titles, clinic identity, formatted money (`Bs. 12,345.67`), all section headers, `@media print`, `.kpi`, `.track` bars, `<table>`, HTML-escaped procedure name (`Limpieza &lt;b&gt;dental&lt;/b&gt;`) and discount row — **ALL_REPORT5_CHECKS_OK**.
- Smoke-run booted directly into Reports: 75 s, no exceptions.
- ✅ `./gradlew build` — 0 errors.

## Next Task

TASK-006 (provisional): real PDF export (vector graphics via `java.awt.print` into a `.pdf` file) as an alternative to HTML-in-browser printing, or dependency-free SVG chart embedding in the HTML report.
