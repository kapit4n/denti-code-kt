# Milestone 5 — Changelog

All notable changes to the Reports milestone.

## TASK-001 — Reports screen: revenue & appointment KPIs

### Data layer (`data/Models.kt`, `data/DentiRepository.kt`)
- Added report models: `ReportsOverview`, `RevenueDayPoint`, `AppointmentDayPoint`, `RevenueByMethodSlice`, `AppointmentStatusSlice`, `TopProcedureRow`
- Added `reportsOverview(startDate, endDate)` — revenue by day/method, appointments by day/status, new patients, top 5 billed procedures; all read in one transaction from `payments`, `appointments`, `patients`, `procedure_types`

### UI (`ui/reports/ReportsScreen.kt`, `ui/reports/ReportsContent.kt`, `ui/layout/AppShell.kt`)
- Reports route now renders a real screen (was a placeholder stub)
- Date-range filter: quick chips (7 días / 30 días / Este mes / 3 meses / Todo) + custom Desde/Hasta `AppDatePickerField`
- KPI row (MetricCard): ingreso total, citas, nuevos pacientes, promedio por pago
- Charts (reused enterprise components): line = ingresos por día, bar = citas por día, donut = ingresos por método with legend; appointments-by-status bars; top procedures list; per-section empty states

### Export (`export/ReportsExport.kt`)
- `renderReportsCsv` — multi-section `;`-separated CSV (resumen, ingreso por día/método, citas por estado, procedimientos top); locale-safe money, Excel-compatible quoting; saved via native dialog

### Verification
- Temporary data-layer probe: real totals over 2000→today (47 650.72 Bs / 210 pagos / 300 citas / 150 pacientes nuevos), series and CSV sections correct — **ALL REPORT CHECKS OK**
- Smoke-run booted directly into Reports: 75 s, no exceptions
- ✅ BUILD SUCCESSFUL (0 errors)
