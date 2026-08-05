# TASK-001: Reports screen — revenue & appointment KPIs

## Objective

Replace the Reports `PlaceholderScreen` with a real reporting screen showing revenue and appointment KPIs over a **date range**, computed entirely from real repository reads, reusing the existing enterprise chart components, plus a working CSV export.

## Motivation

The app had a dedicated Reports route but it was a stub ("Esta sección está en preparación"). Milestone 5 turns it into a business-intelligence dashboard; this first slice delivers the foundation: a filterable date-range report on revenue + scheduling that will be extended in later tasks (doctors/patients/treatments/inventory reports, printing).

## What was added

### Data layer
- **`data/Models.kt`** — report models: `ReportsOverview` (start/end, totals, counts, daily series, method breakdown, status breakdown, top procedures) plus `RevenueDayPoint`, `AppointmentDayPoint`, `RevenueByMethodSlice`, `AppointmentStatusSlice`, `TopProcedureRow`.
- **`data/DentiRepository.kt`** — `reportsOverview(startDate, endDate)` reads the real tables in one `transaction {}`:
  - Revenue by day and by payment method (from `payments`, `paid_at` ISO-day parsing, normalized start/end order).
  - Appointments by day and by status (from `appointments`, `scheduled_at`).
  - New patients in range (from `patients.created_at` epoch range).
  - Top 5 billed procedures by revenue (payments → `procedure_types` name, count + income).
  - Private `parseReportDay()` helper (ISO date-time or plain date → `LocalDate`).

### UI
- **`ui/reports/ReportsScreen.kt`** — hosts date-range state (default: last 30 days), loads via `LaunchedEffect(start, end)` on `Dispatchers.IO`, loading indicator, export action.
- **`ui/reports/ReportsContent.kt`** — page header, quick-range chips (7 días / 30 días / Este mes / 3 meses / Todo), two `AppDatePickerField` (Desde/Hasta, past-or-today selectable), KPI row (`MetricCard`): ingreso total, citas, nuevos pacientes, promedio por pago; charts: `EnterpriseLineChart` (ingresos por día), `EnterpriseBarChart` (citas por día), `EnterpriseDonutChart` (ingresos por método) with method legend, plus "Citas por estado" progress bars and "Procedimientos top" list. Empty states handled per section.
- **`ui/layout/AppShell.kt`** — `ScreenRoute.Reports` now renders `ReportsScreen(repo)` instead of the placeholder.

### Export
- **`export/ReportsExport.kt`** — `renderReportsCsv(overview)` produces a `;`-separated multi-section CSV (RESUMEN, INGRESO POR DÍA, INGRESO POR MÉTODO, CITAS POR ESTADO, PROCEDIMIENTOS TOP); locale-safe money (`Locale.US`), Excel-compatible field quoting. Exported via the native save dialog in `ReportsScreen`.

## Components added / updated

| Component | Status |
|-----------|--------|
| `ReportsScreen`, `ReportsContent` | Added |
| `renderReportsCsv` | Added |
| `reportsOverview()` repo method + report models | Added |
| `AppShell` route wiring | Updated |

## Reused components

`EnterpriseLineChart`, `EnterpriseBarChart`, `EnterpriseDonutChart`, `MetricCard`, `AppCard`, `AppOutlinedButton`, `AppDatePickerField`, `FilterChip`, `LoadingIndicator`, `ExportService` — no chart or layout duplication.

## Verification

- Runtime probe (temporary, removed after use): `reportsOverview` over 2000-01-01→today returned real data (revenue 47 650.72 Bs, 210 payments, 300 appointments, 150 new patients, daily series of 9714 days, 5 methods, 4 statuses, top 5 procedures) and `renderReportsCsv` produced all five sections — **ALL REPORT CHECKS OK**.
- Smoke-run with the Reports route booted directly: rendered 75 s with no exceptions (no `No transaction in context`, no chart/layout crashes).
- ✅ `./gradlew build` — 0 errors.

## Screenshots Required

- Reports screen (KPIs + charts) once a capture tool is available (SCREENSHOTS.md).

## Next Task

TASK-002 (provisional): extend reports to doctor/patient dimensions (top doctors by revenue, patient acquisition trend, appointment no-show rate) with the same date-range filter and CSV export.
